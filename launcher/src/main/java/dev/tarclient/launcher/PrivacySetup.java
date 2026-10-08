package dev.tarclient.launcher;

import javax.swing.*;
import java.awt.*;
import java.nio.charset.StandardCharsets;

final class PrivacySetup {
    static boolean showIfNeeded()throws Exception {
        var privacy=PrivacyPreferences.defaults();if(privacy.hasChoice())return true;
        String policy;
        try(var input=PrivacySetup.class.getResourceAsStream("/privacy/PRIVACY.md")){
            if(input==null)throw new java.io.IOException("The packaged privacy policy is missing. Please download a complete release.");
            policy=new String(input.readAllBytes(),StandardCharsets.UTF_8);
        }
        var panel=new JPanel(new BorderLayout(8,12));
        var text=new JTextArea(policy,18,68);text.setEditable(false);text.setLineWrap(true);text.setWrapStyleWord(true);text.setCaretPosition(0);
        panel.add(new JScrollPane(text),BorderLayout.CENTER);
        var share=new JCheckBox("Share my Minecraft identity for Tar player badges (optional)",true);
        panel.add(share,BorderLayout.SOUTH);
        int choice=JOptionPane.showOptionDialog(null,panel,"Tar Client privacy setup",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE,null,new Object[]{"Continue","Cancel"},"Continue");
        if(choice!=0)return false;
        privacy.save(share.isSelected());return true;
    }
}
