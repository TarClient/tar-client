using System;
using System.IO;
using System.IO.Compression;
using System.Reflection;
using System.Diagnostics;
using System.Security.Cryptography;
using System.Threading;
using System.Windows.Forms;

[assembly: AssemblyTitle("Tar Client")]
[assembly: AssemblyCompany("Tarre Industries")]
[assembly: AssemblyProduct("Tar Client")]
[assembly: AssemblyVersion("1.0.2.0")]
internal static class Program
{
    [STAThread]
    private static int Main(string[] args)
    {
        bool installOnly = Array.IndexOf(args, "--install-only") >= 0;
        try
        {
            using (var mutex = new Mutex(false, "Local\\TarClientBootstrap"))
            {
                bool owned = false;
                try
                {
                    try { owned = mutex.WaitOne(TimeSpan.FromMinutes(3)); }
                    catch (AbandonedMutexException) { owned = true; }
                    if (!owned) throw new IOException("Another Tar Client installation is still running.");
                    string app = Install();
                    CreateShortcut(app);
                    if (!installOnly) Process.Start(new ProcessStartInfo(app) { WorkingDirectory = Path.GetDirectoryName(app), UseShellExecute = true });
                }
                finally { if (owned) mutex.ReleaseMutex(); }
            }
            return 0;
        }
        catch (Exception e)
        {
            if (!installOnly) MessageBox.Show("Tar Client could not start.\n\n" + e.Message, "Tar Client", MessageBoxButtons.OK, MessageBoxIcon.Error);
            return 1;
        }
    }
    private static Stream Resource(string name)
    {
        var stream = Assembly.GetExecutingAssembly().GetManifestResourceStream(name);
        if (stream == null) throw new IOException("The launcher download is incomplete. Download Tar Client again.");
        return stream;
    }
    private static string Hash(Stream stream)
    {
        using (var sha = SHA256.Create()) return BitConverter.ToString(sha.ComputeHash(stream)).Replace("-", "").ToLowerInvariant();
    }
    private static void NoLinks(string path)
    {
        for (var current = new DirectoryInfo(path); current != null; current = current.Parent)
            if (current.Exists && (current.Attributes & FileAttributes.ReparsePoint) != 0)
                throw new IOException("The installation folder contains a redirected directory. Choose a standard Windows user profile.");
    }
    private static string Install()
    {
        string expected;
        using (var reader = new StreamReader(Resource("TarPayload.sha256"))) expected = reader.ReadToEnd().Trim().Split(' ')[0].ToLowerInvariant();
        using (var payload = Resource("TarPayload.zip"))
            if (Hash(payload) != expected) throw new IOException("The embedded download failed its integrity check. Download a fresh copy.");
        string root = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "Programs", "Tar Client");
        NoLinks(root);
        Directory.CreateDirectory(root);
        string target = Path.Combine(root, "1.0.2-" + expected.Substring(0, 12));
        string executable = Path.Combine(target, "Tar Client", "Tar Client.exe");
        if (File.Exists(Path.Combine(target, ".complete")) && File.Exists(executable) && File.Exists(Path.Combine(target, "Tar Client", "runtime", "lib", "modules"))) return executable;
        string pending = Path.Combine(root, ".install-" + Guid.NewGuid().ToString("N"));
        Directory.CreateDirectory(pending);
        // Failed staging folders are preserved for diagnosis; no existing installation is deleted.
        using (var payload = Resource("TarPayload.zip"))
        using (var zip = new ZipArchive(payload, ZipArchiveMode.Read))
        {
            long bytes = 0;
            foreach (var entry in zip.Entries)
            {
                string name = entry.FullName.Replace('/', Path.DirectorySeparatorChar);
                if (name.Contains(":") || Path.IsPathRooted(name)) throw new IOException("Invalid path in the package.");
                string destination = Path.GetFullPath(Path.Combine(pending, name));
                if (!destination.StartsWith(pending + Path.DirectorySeparatorChar, StringComparison.OrdinalIgnoreCase)) throw new IOException("Package path escaped its installation folder.");
                bytes += entry.Length;
                if (bytes > 1024L * 1024 * 1024) throw new IOException("The package is unexpectedly large.");
                if (entry.Name.Length == 0) { Directory.CreateDirectory(destination); continue; }
                Directory.CreateDirectory(Path.GetDirectoryName(destination));
                using (var source = entry.Open())
                using (var output = new FileStream(destination, FileMode.CreateNew, FileAccess.Write)) source.CopyTo(output);
            }
        }
        string[] required = { "Tar Client.exe", "app/tar-launcher.jar", "app/Tar Client.cfg", "runtime/bin/java.exe", "runtime/lib/modules" };
        foreach (string file in required)
            if (!File.Exists(Path.Combine(pending, "Tar Client", file.Replace('/', Path.DirectorySeparatorChar)))) throw new IOException("Incomplete package: " + file);
        File.WriteAllText(Path.Combine(pending, ".complete"), expected);
        if (Directory.Exists(target)) target += "-" + Guid.NewGuid().ToString("N");
        Directory.Move(pending, target);
        return Path.Combine(target, "Tar Client", "Tar Client.exe");
    }
    private static void CreateShortcut(string executable)
    {
        string desktop = Environment.GetFolderPath(Environment.SpecialFolder.DesktopDirectory);
        if (String.IsNullOrWhiteSpace(desktop)) throw new IOException("Windows could not find your desktop folder.");
        Directory.CreateDirectory(desktop);
        var shellType = Type.GetTypeFromProgID("WScript.Shell");
        object shell = Activator.CreateInstance(shellType);
        object link = shellType.InvokeMember("CreateShortcut", BindingFlags.InvokeMethod, null, shell, new object[] { Path.Combine(desktop, "Tar Client.lnk") });
        var type = link.GetType();
        type.InvokeMember("TargetPath", BindingFlags.SetProperty, null, link, new object[] { executable });
        type.InvokeMember("WorkingDirectory", BindingFlags.SetProperty, null, link, new object[] { Path.GetDirectoryName(executable) });
        type.InvokeMember("IconLocation", BindingFlags.SetProperty, null, link, new object[] { executable + ",0" });
        type.InvokeMember("Description", BindingFlags.SetProperty, null, link, new object[] { "Tar Client 1.0" });
        type.InvokeMember("Save", BindingFlags.InvokeMethod, null, link, null);
        System.Runtime.InteropServices.Marshal.FinalReleaseComObject(link);
        System.Runtime.InteropServices.Marshal.FinalReleaseComObject(shell);
    }
}
