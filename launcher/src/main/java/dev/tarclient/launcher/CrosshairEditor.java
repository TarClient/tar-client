package dev.tarclient.launcher;

import dev.tarclient.config.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.function.BooleanSupplier;

final class CrosshairEditor {
    static void open(JFrame owner,ClientConfig config,BooleanSupplier editable,Runnable save){
        JDialog dialog=new JDialog(owner,"Draw your crosshair",false);
        JPanel root=new JPanel(new BorderLayout(12,12));root.setBorder(BorderFactory.createEmptyBorder(18,18,18,18));
        String[] pattern={CrosshairPattern.normalize(config.text("crosshair","pattern"))};
        JLabel preview=new JLabel("Preview",SwingConstants.CENTER);
        JPanel canvas=new JPanel(){
            {setPreferredSize(new Dimension(330,330));var mouse=new MouseAdapter(){boolean ink;void paintAt(MouseEvent e){int size=Math.min(getWidth(),getHeight())/15,x=e.getX()/size,y=e.getY()/size;if(e.getX()<0||e.getY()<0)return;pattern[0]=CrosshairPattern.paint(pattern[0],x,y,ink);repaint();}
                public void mousePressed(MouseEvent e){int size=Math.min(getWidth(),getHeight())/15;ink=SwingUtilities.isLeftMouseButton(e)&&!CrosshairPattern.pixel(pattern[0],e.getX()/size,e.getY()/size);paintAt(e);}
                public void mouseDragged(MouseEvent e){paintAt(e);}};addMouseListener(mouse);addMouseMotionListener(mouse);}
            protected void paintComponent(Graphics g){super.paintComponent(g);int cell=Math.min(getWidth(),getHeight())/15;for(int y=0;y<15;y++)for(int x=0;x<15;x++){g.setColor(CrosshairPattern.pixel(pattern[0],x,y)?new Color(config.color("crosshair","color"),true):new Color((x+y)%2==0?0x273240:0x344150));g.fillRect(x*cell,y*cell,cell-1,cell-1);}
                var image=new java.awt.image.BufferedImage(60,60,java.awt.image.BufferedImage.TYPE_INT_ARGB);var p=image.createGraphics();p.setColor(new Color(0x52684C));p.fillRect(0,0,60,60);p.setColor(new Color(config.color("crosshair","color"),true));for(int y=0;y<15;y++)for(int x=0;x<15;x++)if(CrosshairPattern.pixel(pattern[0],x,y))p.fillRect(x*4,y*4,4,4);p.dispose();preview.setIcon(new ImageIcon(image));}
        };
        root.add(new JLabel("Click or drag to draw. Right-click to erase."),BorderLayout.NORTH);root.add(canvas);root.add(preview,BorderLayout.EAST);
        JPanel actions=new JPanel();JButton clear=new JButton("Clear"),preset=new JButton("+ Preset"),apply=new JButton("Save design"),cancel=new JButton("Cancel");
        clear.addActionListener(e->{pattern[0]="0".repeat(225);canvas.repaint();});preset.addActionListener(e->{pattern[0]=CrosshairPattern.vanilla();canvas.repaint();});
        apply.addActionListener(e->{if(!editable.getAsBoolean())return;config.set("crosshair","pattern",pattern[0]);config.set("crosshair","vanilla",false);config.set("crosshair","grid",true);config.set("crosshair","enabled",true);save.run();dialog.dispose();});cancel.addActionListener(e->dialog.dispose());
        for(var button:new JButton[]{clear,preset,apply,cancel})actions.add(button);root.add(actions,BorderLayout.SOUTH);dialog.setContentPane(root);dialog.pack();dialog.setResizable(false);dialog.setLocationRelativeTo(owner);dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);dialog.setVisible(true);
    }
}
