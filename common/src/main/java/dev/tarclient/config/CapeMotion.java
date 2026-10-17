package dev.tarclient.config;

/** Cape angles in the player's local model space: positive Z is behind the torso. */
public final class CapeMotion {
    public static final float BACK_OFFSET=3f; // Torso back is Z=2; the jacket adds 0.25.
    private CapeMotion() {}
    public static float rootPitch(float stride){
        float movement=Float.isFinite(stride)?Math.abs(stride):0;
        return .09f+Math.min(.48f,movement*.3f);
    }
    public static float hingePitch(int section,float seconds){
        if(section<0||section>=4)throw new IllegalArgumentException("Cape section");
        float t=Float.isFinite(seconds)?seconds:0;
        return .035f+(float)Math.sin(t*2.5f-section*.65f)*(.012f+section*.006f);
    }
}
