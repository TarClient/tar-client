package dev.tarclient.config;
/** Model-only animation, in radians and model units. Never changes world movement. */
public final class EmotePose {
    public float head,body,leftArm,rightArm,leftLeg,rightLeg,leftRoll,rightRoll,leftYaw,rightYaw;
    public float tilt,turn,lift,lean,roll;
    public static EmotePose at(String id,double seconds){
        var p=new EmotePose();double t=Math.max(0,seconds),s=Math.sin(t*6),c=Math.cos(t*6);
        switch(id){
            case "lean" -> {p.tilt=-.65f*(float)Math.min(1,t);p.leftArm=.15f;p.rightArm=.15f;}
            case "moonwalk" -> {p.leftLeg=(float)s*.5f;p.rightLeg=-p.leftLeg;p.leftArm=-p.leftLeg*.3f;p.rightArm=-p.rightLeg*.3f;p.turn=(float)Math.sin(t*.8)*.2f;p.lift=(float)Math.abs(s)*.035f;}
            case "backflip" -> {double cycle=t%2.2;p.tilt=cycle<1.4?(float)(-Math.PI*2*cycle/1.4):0;p.lift=cycle<1.4?(float)Math.sin(Math.PI*cycle/1.4)*.95f:0;p.leftLeg=p.rightLeg=cycle<1.4?-.8f:0;p.leftArm=p.rightArm=-1.7f;}
            case "ironman" -> {p.lift=.6f+(float)Math.sin(t*3)*.08f;p.leftArm=p.rightArm=.15f;p.leftRoll=-.18f;p.rightRoll=.18f;p.leftLeg=.12f;p.rightLeg=-.12f;}
            case "ratdance" -> {p.leftRoll=-1.2f+(float)s*.3f;p.rightRoll=1.2f-(float)s*.3f;p.leftArm=(float)c;p.rightArm=-p.leftArm;p.leftLeg=(float)s*.45f;p.rightLeg=-p.leftLeg;p.roll=(float)s*.15f;}
            case "worm" -> {p.tilt=(float)Math.PI/2;p.lift=.12f+(float)(s+1)*.12f;p.lean=-.75f;p.body=(float)s*.3f;p.leftLeg=p.rightLeg=(float)c*.6f;p.leftArm=p.rightArm=-1.3f+(float)s*.4f;}
            case "sit" -> {p.lift=-.72f;p.leftLeg=p.rightLeg=-(float)Math.PI/2;p.leftArm=p.rightArm=-.2f;}
            case "lay" -> {p.tilt=-(float)Math.PI/2;p.lean=.05f;p.lift=.05f;}
            case "dab" -> {p.head=.5f;p.leftArm=-1.5f;p.leftRoll=-1.3f;p.rightArm=-1.7f;p.rightRoll=-.8f;}
            case "wave" -> {p.rightArm=-(float)Math.PI;p.rightRoll=.35f+(float)s*.35f;}
            case "animerun" -> {p.tilt=.45f;p.leftArm=p.rightArm=1.15f;p.leftRoll=-.2f;p.rightRoll=.2f;p.leftLeg=(float)Math.sin(t*12)*.9f;p.rightLeg=-p.leftLeg;}
            case "tpose" -> {p.leftRoll=-(float)Math.PI/2;p.rightRoll=(float)Math.PI/2;}
            case "clap" -> {p.leftArm=p.rightArm=-1.35f;p.leftYaw=-(float)(.5+.35*s);p.rightYaw=-p.leftYaw;}
            case "ascend" -> {p.lift=.6f+(float)Math.sin(t*2)*.1f;p.tilt=-.2f;p.head=-.4f;p.leftRoll=-1.2f;p.rightRoll=1.2f;p.turn=(float)t*.15f;}
            case "griddy" -> {p.leftLeg=(float)s*.85f;p.rightLeg=-p.leftLeg;p.leftArm=(float)c*.7f;p.rightArm=-p.leftArm;p.tilt=.15f;p.head=.25f;p.lift=(float)Math.abs(s)*.08f;}
            case "o7" -> {p.rightArm=-2.5f;p.rightRoll=-.5f;p.rightYaw=-.45f;}
        }
        return p;
    }
}
