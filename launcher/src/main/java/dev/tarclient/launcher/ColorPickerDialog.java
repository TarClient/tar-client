package dev.tarclient.launcher;

import dev.tarclient.config.ColorPickerModel;
import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import java.awt.*;
import java.util.Locale;

/** Native HSV gradient picker, including transparency for ARGB module settings. */
final class ColorPickerDialog {
    private ColorPickerDialog() {}
    static JColorChooser createChooser(String initial,String fallback,boolean alpha){
        var model=new ColorPickerModel(ColorPickerModel.valid(initial,alpha)?initial:fallback,alpha);
        var chooser=new JColorChooser(new Color(model.argb(),true));
        for(var panel:chooser.getChooserPanels()){
            panel.setColorTransparencySelectionEnabled(alpha);
            if(panel.getDisplayName().equalsIgnoreCase("HSV"))chooser.setChooserPanels(new AbstractColorChooserPanel[]{panel});
        }
        var preview=new JPanel(){
            @Override protected void paintComponent(Graphics g){
                super.paintComponent(g);
                for(int y=0;y<getHeight();y+=8)for(int x=0;x<getWidth();x+=8){g.setColor((x/8+y/8)%2==0?Color.LIGHT_GRAY:Color.GRAY);g.fillRect(x,y,8,8);}
                g.setColor(chooser.getColor());g.fillRect(0,0,getWidth(),getHeight());
            }
        };
        preview.setPreferredSize(new Dimension(240,36));chooser.setPreviewPanel(preview);
        chooser.getSelectionModel().addChangeListener(e->preview.repaint());
        return chooser;
    }
    static String choose(Component parent,String title,String initial,String fallback,boolean alpha){
        var chooser=createChooser(initial,fallback,alpha);
        String[] result={null};
        JDialog dialog=JColorChooser.createDialog(parent,title,true,chooser,e->{
            int color=chooser.getColor().getRGB();result[0]=String.format(Locale.ROOT,alpha?"%08X":"%06X",alpha?color:color&0xFFFFFF);
        },null);
        dialog.setVisible(true);dialog.dispose();return result[0];
    }
}
