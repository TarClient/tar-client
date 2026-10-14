package dev.tarclient.config;

import java.util.Locale;

/** Platform-independent HSV picker. Stored colors remain RGB or alpha-first ARGB hex. */
public final class ColorPickerModel {
    private final boolean alphaAllowed;
    private double hue,saturation,value,alpha=1;
    public ColorPickerModel(String initial,boolean alphaAllowed){this.alphaAllowed=alphaAllowed;if(!setHex(initial))setHex(alphaAllowed?"FFFFFFFF":"FFFFFF");}
    public double hue(){return hue;}
    public double saturation(){return saturation;}
    public double value(){return value;}
    public double alpha(){return alpha;}
    public void hue(double h){hue=clamp(h);}
    public void saturationValue(double s,double v){saturation=clamp(s);value=clamp(v);}
    public void alpha(double a){alpha=alphaAllowed?clamp(a):1;}
    private static double clamp(double v){return Double.isFinite(v)?Math.clamp(v,0,1):0;}
    public static boolean valid(String text,boolean alphaAllowed){
        return text!=null&&text.trim().matches(alphaAllowed?"#?(?:[0-9a-fA-F]{6}|[0-9a-fA-F]{8})":"#?[0-9a-fA-F]{6}");
    }
    public boolean setHex(String text){
        if(!valid(text,alphaAllowed))return false;
        String hex=text.trim().replace("#","");long parsed=Long.parseLong(hex,16);
        alpha=alphaAllowed&&hex.length()==8?((parsed>>>24)&255)/255.0:1;
        double r=((parsed>>>16)&255)/255.0,g=((parsed>>>8)&255)/255.0,b=(parsed&255)/255.0;
        double max=Math.max(r,Math.max(g,b)),min=Math.min(r,Math.min(g,b)),delta=max-min;
        value=max;saturation=max==0?0:delta/max;
        // Keep hue when selecting gray/black, so dragging back into color feels natural.
        if(delta!=0){double h=max==r?(g-b)/delta:max==g?(b-r)/delta+2:(r-g)/delta+4;hue=(h/6+1)%1;}
        return true;
    }
    public int argb(){return ((int)Math.round(alpha*255)<<24)|rgb(hue,saturation,value);}
    public String hex(){return String.format(Locale.ROOT,alphaAllowed?"%08X":"%06X",alphaAllowed?argb():argb()&0xFFFFFF);}
    public static int rgb(double h,double s,double v){
        double hue=clamp(h)*6,sat=clamp(s),val=clamp(v),c=val*sat,x=c*(1-Math.abs(hue%2-1)),m=val-c;
        double r=0,g=0,b=0;
        switch((int)hue%6){case 0:r=c;g=x;break;case 1:r=x;g=c;break;case 2:g=c;b=x;break;case 3:g=x;b=c;break;case 4:r=x;b=c;break;case 5:r=c;b=x;break;}
        return ((int)Math.round((r+m)*255)<<16)|((int)Math.round((g+m)*255)<<8)|(int)Math.round((b+m)*255);
    }
}
