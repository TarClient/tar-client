package dev.tarclient.launcher;

import com.google.gson.*;
import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/** Stable releases only. Never replaces the running app or changes Windows protection. */
public final class LauncherUpdater {
    static final String LATEST="https://api.github.com/repos/TarClient/tar-client/releases/latest";
    static final long MAX_BYTES=250L*1024*1024;
    record Update(String version,URI url,long size,String sha256) {}
    interface Transport { String latest() throws Exception; void download(Update update,Path output,Consumer<String> progress) throws Exception; }
    private final Transport transport;
    public LauncherUpdater(){this(new GitHub());}
    LauncherUpdater(Transport transport){this.transport=transport;}

    static int compare(String a,String b) throws IOException {
        if(!a.matches("[0-9]{1,6}\\.[0-9]{1,6}\\.[0-9]{1,6}")||!b.matches("[0-9]{1,6}\\.[0-9]{1,6}\\.[0-9]{1,6}"))throw new IOException("Unsupported release version");
        String[] x=a.split("\\."),y=b.split("\\.");
        for(int i=0;i<3;i++){int c=Integer.compare(Integer.parseInt(x[i]),Integer.parseInt(y[i]));if(c!=0)return c;}return 0;
    }
    static Update select(String json,String current) throws IOException {
        try {
            JsonObject release=JsonParser.parseString(json).getAsJsonObject();
            if(release.get("draft").getAsBoolean()||release.get("prerelease").getAsBoolean())return null;
            String tag=release.get("tag_name").getAsString();
            if(!tag.startsWith("v"))throw new IOException("Unsupported release tag");
            String version=tag.substring(1);if(compare(version,current)<=0)return null;
            String name="TarClient-"+version+".exe";Update found=null;
            for(JsonElement item:release.getAsJsonArray("assets")){
                JsonObject asset=item.getAsJsonObject();if(!name.equals(asset.get("name").getAsString()))continue;
                if(found!=null)throw new IOException("Duplicate update asset");
                URI url=URI.create(asset.get("browser_download_url").getAsString());
                String expected="https://github.com/TarClient/tar-client/releases/download/"+tag+"/"+name;
                if(!expected.equals(url.toString()))throw new IOException("Unexpected update download location");
                long size=asset.get("size").getAsLong();String digest=asset.get("digest").getAsString();
                if(size<=0||size>MAX_BYTES||!digest.matches("sha256:[a-fA-F0-9]{64}")||!"uploaded".equals(asset.get("state").getAsString()))throw new IOException("Missing or invalid update integrity information");
                found=new Update(version,url,size,digest.substring(7));
            }
            if(found==null)throw new IOException("The stable release has no Windows update package");return found;
        }catch(IOException e){throw e;}catch(RuntimeException e){throw new IOException("Invalid update metadata",e);}
    }
    Path prepare(String current,Path data,Consumer<String> progress) throws Exception {
        progress.accept("Checking for stable launcher updates...");
        String metadata=transport.latest();if(metadata==null)return null;
        Update update=select(metadata,current);if(update==null)return null;
        Path root=data.resolve("updates");Files.createDirectories(root);
        Path staging=Files.createTempDirectory(root,"update-");Path file=staging.resolve("TarClient-"+update.version()+".exe");
        try {
            transport.download(update,file,progress);
            if(Files.size(file)!=update.size()||!Net.hash(file,"SHA-256").equalsIgnoreCase(update.sha256()))throw new IOException("Update checksum failed; your current installation is unchanged");
            // Preserve Internet-origin marking rather than attempting to bypass Windows checks.
            if(System.getProperty("os.name").startsWith("Windows")){
                var attributes=Files.getFileAttributeView(file,java.nio.file.attribute.UserDefinedFileAttributeView.class);
                if(attributes==null)throw new IOException("Cannot preserve the downloaded file's Internet-origin mark");
                attributes.write("Zone.Identifier",java.nio.charset.StandardCharsets.UTF_8.encode("[ZoneTransfer]\r\nZoneId=3\r\nHostUrl="+update.url()+"\r\n"));
            }
            progress.accept("Verified Tar Client "+update.version()+". Installing and restarting...");return file;
        }catch(Exception e){Files.deleteIfExists(file);throw e;}
    }
    static boolean supported(){return System.getProperty("os.name").startsWith("Windows")&&!System.getProperty("jpackage.app-path","").isBlank()&&System.getProperty("tar.data")==null&&System.getProperty("tar.instance")==null;}
    static Process install(Path file) throws IOException {
        return new ProcessBuilder(file.toString(),"--update-from",Long.toString(ProcessHandle.current().pid())).directory(file.getParent().toFile()).start();
    }
    private static final class GitHub implements Transport {
        private final HttpClient http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).followRedirects(HttpClient.Redirect.NEVER).build();
        private HttpResponse<InputStream> get(URI uri) throws Exception {
            for(int redirects=0;redirects<6;redirects++){
                if(!"https".equals(uri.getScheme())||uri.getUserInfo()!=null||uri.getPort()!=-1||!Set.of("api.github.com","github.com","release-assets.githubusercontent.com","objects.githubusercontent.com").contains(uri.getHost()))throw new IOException("Untrusted update redirect");
                var request=HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(12)).header("User-Agent","TarClient-Updater/"+TarLauncher.VERSION).GET().build();
                var response=http.send(request,HttpResponse.BodyHandlers.ofInputStream());
                if(response.statusCode()/100!=3)return response;
                response.body().close();uri=uri.resolve(response.headers().firstValue("location").orElseThrow(()->new IOException("Missing download redirect")));
            }throw new IOException("Too many update redirects");
        }
        private void copy(InputStream source,OutputStream target,long limit,int seconds,Consumer<Long> count) throws Exception {
            var timer=Executors.newSingleThreadScheduledExecutor(r->{var t=new Thread(r,"update-timeout");t.setDaemon(true);return t;});
            var timeout=timer.schedule(()->{try{source.close();}catch(IOException ignored){}},seconds,TimeUnit.SECONDS);
            long deadline=System.nanoTime()+Duration.ofSeconds(seconds).toNanos();
            try(source){byte[] buffer=new byte[65536];long total=0;int n;
                while((n=source.read(buffer))!=-1){if(Thread.currentThread().isInterrupted())throw new InterruptedException();if(System.nanoTime()>deadline)throw new IOException("Update download timed out");total+=n;if(total>limit)throw new IOException("Update response is too large");target.write(buffer,0,n);count.accept(total);}
            }finally{timeout.cancel(false);timer.shutdownNow();}
        }
        public String latest() throws Exception {
            var response=get(URI.create(LATEST));
            if(response.statusCode()==404){response.body().close();return null;}
            if(response.statusCode()!=200){response.body().close();throw new IOException("GitHub update check returned HTTP "+response.statusCode());}
            var bytes=new ByteArrayOutputStream();copy(response.body(),bytes,1024*1024,12,n->{});return bytes.toString(java.nio.charset.StandardCharsets.UTF_8);
        }
        public void download(Update update,Path output,Consumer<String> progress) throws Exception {
            var response=get(update.url());if(response.statusCode()!=200){response.body().close();throw new IOException("Update download returned HTTP "+response.statusCode());}
            try(var stream=Files.newOutputStream(output,StandardOpenOption.CREATE_NEW)){
                int[] last={-1};copy(response.body(),stream,update.size(),180,n->{int percent=(int)(n*100/update.size());if(percent!=last[0]){last[0]=percent;progress.accept("Downloading Tar Client "+update.version()+": "+percent+"%");}});
            }finally{response.body().close();}
        }
    }
}
