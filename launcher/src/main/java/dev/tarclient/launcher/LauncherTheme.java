package dev.tarclient.launcher;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import dev.tarclient.config.ThemeColors;

/** Original vector artwork and shared native desktop components. */
final class LauncherTheme {
    static Color BG,CARD,LINE,ACCENT,TEXT,MUTED;
    static boolean dark=true;
    static {configure(true,ThemeColors.DEFAULT_ACCENT,ThemeColors.DEFAULT_BACKGROUND);}
    static void configure(boolean enabled,String accent,String background){
        dark=enabled;BG=new Color(dark?ThemeColors.parse(background,ThemeColors.DEFAULT_BACKGROUND):0xF3F3F5);
        ACCENT=new Color(ThemeColors.parse(accent,ThemeColors.DEFAULT_ACCENT));
        Color ink=new Color(ThemeColors.foreground(BG.getRGB()));
        CARD=mix(BG,ink,0.045);LINE=mix(BG,ink,0.18);TEXT=mix(BG,ink,0.94);MUTED=mix(BG,ink,0.68);
    }
    static Color mix(Color a,Color b,double amount){return new Color(ThemeColors.blend(a.getRGB(),b.getRGB(),amount));}
    static Color surface(){return mix(BG,TEXT,0.02);}
    static Color button(){return mix(BG,TEXT,0.085);}
    static Color accentText(){return new Color(ThemeColors.foreground(ACCENT.getRGB()));}
    static JLabel logo(){try{var url=LauncherTheme.class.getResource("/branding/tar-logo.png");if(url!=null){var image=javax.imageio.ImageIO.read(url);var label=new JLabel(new ImageIcon(image.getScaledInstance(42,42,Image.SCALE_SMOOTH)));label.setOpaque(true);label.setBackground(surface());label.setPreferredSize(new Dimension(48,48));return label;}}catch(Exception ignored){}return monogram("T");}
    private LauncherTheme(){}
    static JPanel panel(int padding){
        JPanel panel=new RoundedPanel(CARD);panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(padding,padding,padding,padding));panel.setAlignmentX(0);return panel;
    }
    static class RoundedPanel extends JPanel {
        private final Color fill;
        RoundedPanel(Color fill){this.fill=fill;setOpaque(false);}
        @Override protected void addImpl(Component child,Object constraints,int index){
            if(child instanceof JComponent component)component.setAlignmentX(0);
            super.addImpl(child,constraints,index);
        }
        @Override protected void paintComponent(Graphics graphics){
            Graphics2D g=(Graphics2D)graphics.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(fill);g.fillRoundRect(0,0,getWidth(),getHeight(),22,22);
            g.setColor(LINE);g.drawRoundRect(0,0,getWidth()-1,getHeight()-1,22,22);g.dispose();super.paintComponent(graphics);
        }
    }
    static final class Hero extends JPanel {
        Hero(){setOpaque(false);setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));setBorder(new EmptyBorder(34,32,30,32));}
        @Override protected void paintComponent(Graphics graphics){
            Graphics2D g=(Graphics2D)graphics.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g.setPaint(new GradientPaint(0,0,mix(BG,ACCENT,0.16),getWidth(),getHeight(),CARD));
            g.fillRoundRect(0,0,getWidth(),getHeight(),24,24);g.clip(new java.awt.geom.RoundRectangle2D.Float(0,0,getWidth(),getHeight(),24,24));
            int origin=getWidth()-145;
            g.setColor(new Color(ACCENT.getRed(),ACCENT.getGreen(),ACCENT.getBlue(),12));g.fillOval(origin-130,-100,420,420);
            for(int i=0;i<6;i++){int x=origin+(i%3)*64-65,y=132+(i/3)*73-(i%3)*23;cube(g,x,y,43,48+(i%3)*20,i);}
            g.setColor(new Color(ACCENT.getRed(),ACCENT.getGreen(),ACCENT.getBlue(),25));g.drawLine(origin-150,0,getWidth(),getHeight());
            g.dispose();super.paintComponent(graphics);
        }
        private void cube(Graphics2D g,int x,int y,int size,int depth,int i){
            g.setColor(mix(BG,ACCENT,0.55+i*0.04));g.fillPolygon(new int[]{x,x+size,x,x-size},new int[]{y,y+size/2,y+size,y+size/2},4);
            g.setColor(mix(BG,ACCENT,0.25+i*0.02));g.fillPolygon(new int[]{x-size,x,x,x-size},new int[]{y+size/2,y+size,y+size+depth,y+size/2+depth},4);
            g.setColor(mix(BG,ACCENT,0.15+i*0.02));g.fillPolygon(new int[]{x,x+size,x+size,x},new int[]{y+size,y+size/2,y+size/2+depth,y+size+depth},4);
        }
    }
    static JLabel badge(String text){
        JLabel label=new JLabel(text);label.setFont(new Font("Segoe UI",Font.BOLD,11));label.setForeground(ACCENT);
        label.setOpaque(true);label.setBackground(mix(BG,ACCENT,0.12));label.setBorder(new EmptyBorder(6,10,6,10));return label;
    }
    static JLabel monogram(String text){
        JLabel label=new JLabel(text,SwingConstants.CENTER);label.setFont(new Font("Segoe UI",Font.BOLD,20));label.setForeground(ACCENT);
        label.setOpaque(true);label.setBackground(surface());label.setPreferredSize(new Dimension(48,48));return label;
    }
}
