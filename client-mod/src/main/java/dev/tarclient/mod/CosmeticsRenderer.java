package dev.tarclient.mod;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.model.*;
import net.minecraft.client.render.*;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.feature.*;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import dev.tarclient.config.CapeMotion;
/** Illustrated accessories with articulated cloth. All transforms affect rendering only. */
public final class CosmeticsRenderer extends FeatureRenderer<PlayerEntityRenderState,PlayerEntityModel> {
    private static final Identifier WHITE=Identifier.of("tarclient","textures/cosmetics/white.png");
    private static final ModelPart CUBE=part(ModelPartBuilder.create().uv(0,0).cuboid(0,0,0,1,1,1),1,1);
    private static final int CAPE_FRAMES=32;
    private static final ModelPart[] CAPE_STRIPS=new ModelPart[4];
    private static final ModelPart WING_PANEL=part(ModelPartBuilder.create().uv(0,0).cuboid(0,-7,0,4,22,0),8,32);
    private static final ModelPart BANDANA=part(ModelPartBuilder.create().uv(0,0).cuboid(-4,-3.5f,-4,8,3,8,new Dilation(.2f)),32,16);
    private static final ModelPart[] ANGEL_FLIGHT=new ModelPart[4],ANGEL_COVERTS=new ModelPart[4],ANGEL_SHAFTS=new ModelPart[4];
    private static final java.util.Map<String,RenderLayer[]> CAPES=new java.util.HashMap<>();
    private static final java.util.Map<String,RenderLayer[]> WINGS=new java.util.HashMap<>();
    private static final java.util.Map<String,RenderLayer> BANDANAS=new java.util.HashMap<>();
    static {
        for(int i=0;i<4;i++){ANGEL_FLIGHT[i]=angelPanel(i,0);ANGEL_COVERTS[i]=angelPanel(i,1);ANGEL_SHAFTS[i]=angelPanel(i,2);}
        for(int i=0;i<CAPE_STRIPS.length;i++)CAPE_STRIPS[i]=part(ModelPartBuilder.create().uv(0,i*4).cuboid(-5,0,0,10,4,1),64,32);
        for(String id:new String[]{"basiccape","oceancape","lightningcape","blossomcape","nethercape"}){
            var frames=new RenderLayer[id.equals("basiccape")?1:CAPE_FRAMES];
            for(int i=0;i<frames.length;i++)frames[i]=RenderLayers.entityCutoutNoCull(Identifier.of("tarclient","textures/cosmetics/"+id+"_"+i+".png"));
            CAPES.put(id,frames);
        }
        for(String id:new String[]{"angelwings","demonwings","fairywings","butterflywings"}){
            var panels=new RenderLayer[4];
            for(int i=0;i<panels.length;i++)panels[i]=RenderLayers.entityCutoutNoCull(Identifier.of("tarclient","textures/cosmetics/"+id+"_"+i+".png"));
            WINGS.put(id,panels);
        }
        for(String id:new String[]{"blackbandana","whitebandana","basicbandana"})
            BANDANAS.put(id,RenderLayers.entityCutoutNoCull(Identifier.of("tarclient","textures/cosmetics/"+id+".png")));
    }
    private static final RenderLayer SOLID=RenderLayers.entityCutoutNoCull(WHITE),GLOW=RenderLayers.entityTranslucentEmissive(WHITE);
    public CosmeticsRenderer(FeatureRendererContext<PlayerEntityRenderState,PlayerEntityModel> context){super(context);}
    public static void initialize(){LivingEntityFeatureRendererRegistrationCallback.EVENT.register((type,renderer,helper,ctx)->{if(renderer instanceof PlayerEntityRenderer<?> player)helper.register(new CosmeticsRenderer(player));});}
    private static ModelPart part(ModelPartBuilder b,int w,int h){var data=new ModelData();data.getRoot().addChild("shape",b,ModelTransform.NONE);return TexturedModelData.of(data,w,h).createModel().getChild("shape");}
    private static ModelPart angelPanel(int panel,int layer){
        var data=new ModelData();var root=data.getRoot();
        // A rising upper edge joins the four hinges; individual tapered feathers fan below it.
        if(layer==1)root.addChild("leading_edge",ModelPartBuilder.create().uv(0,0).cuboid(0,-.65f,.1f,4.78f,1.3f,.7f),ModelTransform.of(0,3.5f-panel*2.6f,0,0,0,-.5763752f));
        for(int feather=0;feather<3;feather++){
            float x=feather*1.34f,span=panel*4+x;
            float length=11f+7f*(float)Math.sin(Math.PI*span/19f),angle=-.12f-span*.038f;
            var shape=ModelPartBuilder.create().uv(0,0);
            if(layer==2){
                shape.cuboid(-.065f,.5f,.43f,.13f,length*.82f,.065f);
            }else{
                float size=layer==0?length:length*.48f;
                float width=layer==0?2.5f:2.1f,z=layer==0?.05f:.5f,depth=layer==0?.35f:.3f;
                // Overlapping stepped taper gives each feather thickness and a narrow tip.
                float[] taper={.72f,1f,.88f,.67f,.4f,.13f};
                for(int step=0;step<taper.length;step++){
                    float w=width*taper[step],left=Math.max(-w/2,-span);
                    shape.cuboid(left,step*size/6,z,w/2-left,size/6+.06f,depth);
                }
            }
            root.addChild("feather_"+feather,shape,ModelTransform.of(x,3.5f-span*.65f,0,0,0,angle));
        }
        return TexturedModelData.of(data,1,1).createModel();
    }
    private static void cube(MatrixStack m,OrderedRenderCommandQueue q,int light,int color,float x,float y,float z,float w,float h,float d,boolean glow){m.push();m.translate(x/16f,y/16f,z/16f);m.scale(w,h,d);q.submitModelPart(CUBE,m,glow?GLOW:SOLID,glow?LightmapTextureManager.MAX_LIGHT_COORDINATE:light,OverlayTexture.DEFAULT_UV,null,color,null);m.pop();}
    public void render(MatrixStack m,OrderedRenderCommandQueue q,int light,PlayerEntityRenderState state,float yaw,float pitch){
        var look=SocialRuntime.look(state);if(look==null||look.cosmetics().isEmpty()||state.invisible)return;
        var model=getContextModel();model.setAngles(state);float time=state.age/20f;
        for(String id:look.cosmetics()){
            m.push();
            boolean head=id.endsWith("halo")||id.equals("demonhorns")||id.endsWith("bandana")||id.equals("basiccap");
            if(head)model.head.applyTransform(m);else model.body.applyTransform(m);
            if(id.endsWith("wings"))wings(m,q,light,id,time);
            else if(id.endsWith("halo"))halo(m,q,id,time);
            else if(id.equals("demonhorns"))horns(m,q,light);
            else if(id.endsWith("bandana"))bandana(m,q,light,id,time);
            else if(id.equals("basiccap"))cap(m,q,light);
            else if(id.equals("katana")||id.equals("greatsword"))sword(m,q,light,id);
            else if(id.endsWith("cape")&&!state.isInPose(net.minecraft.entity.EntityPose.GLIDING)){
                cape(m,q,light,id,time,state.limbSwingAmplitude);
            }
            m.pop();
        }
    }
    private static void cape(MatrixStack m,OrderedRenderCommandQueue q,int light,String id,float t,float stride){
        var frames=CAPES.get(id);if(frames==null)return;
        var layer=frames[Math.floorMod((int)(t*8),frames.length)];
        int capeLight=id.equals("lightningcape")?LightmapTextureManager.MAX_LIGHT_COORDINATE:light;
        m.translate(0,0,CapeMotion.BACK_OFFSET/16f);
        // Model Y points down. Positive X rotation moves the hem toward +Z, behind the torso.
        m.multiply(RotationAxis.POSITIVE_X.rotation(CapeMotion.rootPitch(stride)));
        for(int i=0;i<CAPE_STRIPS.length;i++){
            m.multiply(RotationAxis.POSITIVE_X.rotation(CapeMotion.hingePitch(i,t)));
            q.submitModelPart(CAPE_STRIPS[i],m,layer,capeLight,OverlayTexture.DEFAULT_UV,null);
            m.translate(0,4/16f,0);
        }
    }
    private static void wings(MatrixStack m,OrderedRenderCommandQueue q,int light,String id,float t){
        var panels=WINGS.get(id);if(panels==null)return;
        boolean fairy=id.equals("fairywings"),butterfly=id.equals("butterflywings"),angel=id.equals("angelwings");
        for(int side:new int[]{-1,1}){
            // Pair the roots near the spine, just behind the jacket, slightly above the old anchor.
            m.push();m.translate(side*2f/16f,1/16f,2.4/16f);
            float beat=t*(fairy?11f:butterfly?5f:2.8f);
            // Keep the whole flap behind the shoulders instead of sweeping through them.
            float spread=butterfly?.5f:fairy?.4f:.32f;
            float flap=butterfly?.4f:fairy?.3f:.22f;
            m.multiply(RotationAxis.POSITIVE_Y.rotation(side*(-spread+(float)Math.sin(beat)*flap)));
            m.multiply(RotationAxis.POSITIVE_Z.rotation(side*(float)Math.sin(beat-.4f)*.035f));
            // Enlarge around the visible root, so it stays attached at the previous height.
            m.translate(0,3.5/16,0);m.scale(side*1.35f,1.25f,1);m.translate(0,-3.5/16,0);
            for(int i=0;i<panels.length;i++){
                // Each hinge inherits the previous hinge: no disconnected wing sections.
                m.multiply(RotationAxis.POSITIVE_Y.rotation(-.035f+(float)Math.sin(beat-i*.3f)*.045f));
                if(angel){
                    q.submitModelPart(ANGEL_FLIGHT[i],m,SOLID,light,OverlayTexture.DEFAULT_UV,null,0xFFE9EDF4,null);
                    q.submitModelPart(ANGEL_SHAFTS[i],m,SOLID,light,OverlayTexture.DEFAULT_UV,null,0xFFBECADA,null);
                    q.submitModelPart(ANGEL_COVERTS[i],m,SOLID,light,OverlayTexture.DEFAULT_UV,null,0xFFFFFCF4,null);
                }else q.submitModelPart(WING_PANEL,m,panels[i],light,OverlayTexture.DEFAULT_UV,null);
                m.translate(4/16f,0,0);
            }
            m.pop();
        }
    }
    private static void horns(MatrixStack m,OrderedRenderCommandQueue q,int light){
        for(int side:new int[]{-1,1}){
            m.push();m.translate(side*3/16f,-7/16f,0);
            for(int i=0;i<7;i++){
                m.multiply(RotationAxis.POSITIVE_Z.rotation(side*(.12f+i*.016f)));
                float w=1.8f-i*.22f;
                int c=i>4?0xFFF1CFB1:i>1?0xFF975660:0xFF462C3B;
                cube(m,q,light,c,-w/2,-1.25f,-w/2,w,1.4f,w,false);
                if(i<5)cube(m,q,light,0xFFBD8D84,-w/2,-.9f,-w/2-.015f,w,.12f,.06f,false);
                m.translate(0,-1.1/16f,0);
            }
            m.pop();
        }
    }
    private static void cap(MatrixStack m,OrderedRenderCommandQueue q,int light){
        cube(m,q,light,0xFF65B5D9,-4.3f,-8.5f,-4.3f,8.6f,2.2f,8.6f,false);
        cube(m,q,light,0xFF91D1E9,-3.9f,-8.9f,-3.9f,7.8f,.45f,7.8f,false);
        cube(m,q,light,0xFFB4E4F2,-.3f,-9.1f,-.3f,.6f,.25f,.6f,false);
        cube(m,q,light,0xFF86CCE6,-4.1f,-6.5f,-6.7f,8.2f,.45f,3.4f,false);
        cube(m,q,light,0xFF4A8DAE,-3.6f,-6.28f,-7.05f,7.2f,.22f,1,false);
        for(int i=0;i<9;i++)cube(m,q,light,0xFFCEEAF3,-3.6f+i*.85f,-6.39f,-6.78f,.25f,.08f,.07f,false);
        cube(m,q,light,0xFFDEEFF6,-.85f,-8,-4.35f,1.7f,1.1f,.08f,false);
        cube(m,q,light,0xFF539BBF,-.2f,-7.8f,-4.4f,.4f,.8f,.08f,false);
    }
    private static void halo(MatrixStack m,OrderedRenderCommandQueue q,String id,float t){
        int color=switch(id){case "purplehalo"->0xFFD79AFF;case "greenhalo"->0xFFA4FF8A;case "bluehalo"->0xFF82CAFF;default->0xFFFFDA75;};
        m.translate(0,(-11+Math.sin(t*2)*.3)/16,0);
        for(int i=0;i<32;i++){m.push();m.multiply(RotationAxis.POSITIVE_Y.rotation((float)(i*Math.PI/16)));cube(m,q,0,color,3.6f,-.18f,-.37f,.38f,.36f,.76f,true);cube(m,q,0,(color&0xFFFFFF)|0x22000000,3.35f,-.4f,-.5f,.9f,.8f,1,true);m.pop();}
    }
    private static void bandana(MatrixStack m,OrderedRenderCommandQueue q,int light,String id,float t){
        var layer=BANDANAS.get(id);if(layer==null)return;
        int base=id.equals("blackbandana")?0xFF20232C:id.equals("whitebandana")?0xFFE7ECEF:0xFF67BADC;
        q.submitModelPart(BANDANA,m,layer,light,OverlayTexture.DEFAULT_UV,null);
        cube(m,q,light,base,-.7f,-2.3f,4.2f,1.4f,1.3f,1,false);
        for(int side:new int[]{-1,1}){
            m.push();m.translate(side*.4/16f,-1/16f,4.5/16f);
            m.multiply(RotationAxis.POSITIVE_Z.rotation(side*.3f+(float)Math.sin(t*3+side)*.09f));
            cube(m,q,light,base,-.5f,0,0,1,3.5f,.3f,false);
            cube(m,q,light,0xFFBCCEDA,-.42f,.4f,-.02f,.1f,2.7f,.04f,false);m.pop();
        }
    }
    private static void sword(MatrixStack m,OrderedRenderCommandQueue q,int light,String id){
        boolean big=id.equals("greatsword");
        m.translate(big?0:5.3/16,big?2/16f:8/16f,big?4/16f:0);
        m.multiply(RotationAxis.POSITIVE_Z.rotation(big?-.5f:.15f));
        cube(m,q,light,0xFF252F45,-.6f,-5,0,1.2f,5,1.2f,false);
        for(int i=0;i<8;i++){
            m.push();m.translate(0,(-4.7f+i*.55f)/16,0);m.multiply(RotationAxis.POSITIVE_Z.rotation(.35f));
            cube(m,q,light,big?0xFF8C6E59:0xFFB8B2A0,-.65f,0,-.04f,1.3f,.17f,.1f,false);m.pop();
        }
        cube(m,q,light,0xFFE3BB67,-.75f,-5.5f,-.1f,1.5f,.65f,1.4f,false);
        cube(m,q,light,0xFFE3BB67,-3,-.5f,-.5f,6,.8f,2,false);
        cube(m,q,light,0xFF547A91,-.5f,-.7f,-.6f,1,1.2f,.18f,false);
        float width=big?2.6f:1.1f,length=big?17:12;
        cube(m,q,light,0xFF9FB7CB,-width/2,.5f,0,width,length,.7f,false);
        cube(m,q,light,0xFFF1FAFF,width/2-.23f,.5f,-.025f,.23f,length,.76f,false);
        cube(m,q,light,0xFF657D98,-width/2,.5f,-.04f,.2f,length,.08f,false);
        if(big)cube(m,q,light,0xFFDAE6ED,-.16f,.8f,-.03f,.32f,length-1,.08f,false);
        for(int i=0;i<5;i++){
            float w=width*(1-i/5f);
            cube(m,q,light,0xFFCCDDE9,-w/2,.5f+length+i*.4f,0,w,.42f,.7f*(1-i/6f),false);
        }
    }
}
