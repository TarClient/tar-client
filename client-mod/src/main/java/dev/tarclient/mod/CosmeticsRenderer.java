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
/** Original geometric accessories. All transforms affect rendering only. */
public final class CosmeticsRenderer extends FeatureRenderer<PlayerEntityRenderState,PlayerEntityModel> {
    private static final Identifier WHITE=Identifier.of("tarclient","textures/cosmetics/white.png");
    private static final ModelPart CUBE=part(ModelPartBuilder.create().uv(0,0).cuboid(0,0,0,1,1,1),1,1);
    private static final ModelPart CAPE=part(ModelPartBuilder.create().uv(0,0).cuboid(-5,0,0,10,16,1),64,32);
    private static final RenderLayer SOLID=RenderLayers.entityCutoutNoCull(WHITE),GLOW=RenderLayers.entityTranslucentEmissive(WHITE);
    public CosmeticsRenderer(FeatureRendererContext<PlayerEntityRenderState,PlayerEntityModel> context){super(context);}
    public static void initialize(){LivingEntityFeatureRendererRegistrationCallback.EVENT.register((type,renderer,helper,ctx)->{if(renderer instanceof PlayerEntityRenderer<?> player)helper.register(new CosmeticsRenderer(player));});}
    private static ModelPart part(ModelPartBuilder b,int w,int h){var data=new ModelData();data.getRoot().addChild("shape",b,ModelTransform.NONE);return TexturedModelData.of(data,w,h).createModel().getChild("shape");}
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
            else if(id.equals("demonhorns"))for(int side:new int[]{-1,1}){m.push();m.translate(side*3.2/16,-6.0/16,0);m.multiply(RotationAxis.POSITIVE_Z.rotation(side*.3f));cube(m,q,light,0xFF60222C,-1,-4,-1,2,4,2,false);cube(m,q,light,0xFFE7BA9A,-.65f,-6,-.65f,1.3f,2,1.3f,false);m.pop();}
            else if(id.endsWith("bandana"))bandana(m,q,light,id,time);
            else if(id.equals("basiccap")){cube(m,q,light,0xFF78C9EE,-4.3f,-8.5f,-4.3f,8.6f,2.2f,8.6f,false);cube(m,q,light,0xFFAEEAFF,-4.3f,-6.5f,-7,8.6f,.6f,4,false);}
            else if(id.equals("katana")||id.equals("greatsword"))sword(m,q,light,id);
            else if(id.endsWith("cape")&&!state.isInPose(net.minecraft.entity.EntityPose.GLIDING)){
                m.translate(0,0,2.7/16);m.multiply(RotationAxis.POSITIVE_X.rotation(.12f+(float)Math.sin(time*3)*.04f+Math.min(.4f,state.limbSwingAmplitude*.25f)));
                int frame=Math.floorMod((int)(time*8),16);var texture=Identifier.of("tarclient","textures/cosmetics/"+id+"_"+frame+".png");
                q.submitModelPart(CAPE,m,RenderLayers.entityCutoutNoCull(texture),light,OverlayTexture.DEFAULT_UV,null);
            }
            m.pop();
        }
    }
    private static void wings(MatrixStack m,OrderedRenderCommandQueue q,int light,String id,float t){
        boolean angel=id.equals("angelwings"),demon=id.equals("demonwings"),fairy=id.equals("fairywings");
        int base=angel?0xFFF3F4FF:demon?0xFF591D34:fairy?0xFF9AE5EF:0xFF9E68E7;
        for(int side:new int[]{-1,1}){
            m.push();m.translate(side*2/16f,3/16f,3/16f);m.multiply(RotationAxis.POSITIVE_Y.rotation(side*(.3f+(float)Math.sin(t*(fairy?12:3.5))*(fairy?.5f:.35f))));m.scale(side,1,1);
            if(angel){
                cube(m,q,light,0xFFCBD5ED,0,-2,0,12,2,1,false);
                for(int i=0;i<8;i++){m.push();m.translate((i*1.65)/16,-(float)Math.sin(i*.35)*2/16,0);m.multiply(RotationAxis.POSITIVE_Z.rotation(-.3f+i*.05f));cube(m,q,light,i%2==0?base:0xFFDDE7FA,0,0,0,2,8+i*.5f,.8f,false);m.pop();}
            }else{
                for(int lobe=0;lobe<2;lobe++){
                    m.push();m.translate(0,lobe*5/16f,0);m.multiply(RotationAxis.POSITIVE_Z.rotation(lobe==0?-.5f:.25f));
                    for(int i=0;i<10;i++){float h=(float)Math.sin((i+1)*Math.PI/11)*(lobe==0?9:6);cube(m,q,light,base,i,-h/2,0,1.2f,h,.5f,fairy);}
                    cube(m,q,light,demon?0xFF21101F:0xFFE6CDFD,0,-.4f,-.1f,10,.7f,.7f,false);
                    if(!demon)for(int i=2;i<9;i+=2)cube(m,q,light,0xFFE6D6FF,i,-2,-.2f,.7f,3,.8f,fairy);
                    m.pop();
                }
            }
            m.pop();
        }
    }
    private static void halo(MatrixStack m,OrderedRenderCommandQueue q,String id,float t){
        int color=switch(id){case "purplehalo"->0xFFD79AFF;case "greenhalo"->0xFFA4FF8A;case "bluehalo"->0xFF82CAFF;default->0xFFFFDA75;};
        m.translate(0,(-11+Math.sin(t*2)*.3)/16,0);
        for(int i=0;i<16;i++){m.push();m.multiply(RotationAxis.POSITIVE_Y.rotation((float)(i*Math.PI/8)));cube(m,q,0,color,3.6f,-.25f,-.7f,.65f,.5f,1.5f,true);cube(m,q,0,(color&0xFFFFFF)|0x33000000,3.35f,-.5f,-1,1.15f,1,2,true);m.pop();}
    }
    private static void bandana(MatrixStack m,OrderedRenderCommandQueue q,int light,String id,float t){int base=id.equals("blackbandana")?0xFF121318:id.equals("whitebandana")?0xFFF1F1F5:0xFF8AD4F5;int detail=id.equals("whitebandana")?0xFF191A22:0xFFE4F6FF;cube(m,q,light,base,-4.2f,-3.5f,-4.25f,8.4f,3.4f,8.5f,false);for(int i=0;i<7;i++)cube(m,q,light,detail,-3.4f+i,-2.5f+(i%2)*.6f,-4.32f,.6f,.5f,.1f,id.equals("blackbandana"));m.push();m.translate(0,-1/16f,4.4/16f);m.multiply(RotationAxis.POSITIVE_Z.rotation(.2f+(float)Math.sin(t*3)*.1f));cube(m,q,light,base,-1,0,0,1.5f,4,.6f,false);m.pop();}
    private static void sword(MatrixStack m,OrderedRenderCommandQueue q,int light,String id){boolean big=id.equals("greatsword");m.translate(big?0:5.3/16,big?2/16f:8/16f,big?4/16f:0);m.multiply(RotationAxis.POSITIVE_Z.rotation(big?-.5f:.15f));cube(m,q,light,0xFF252F45,-.6f,-5,0,1.2f,5,1.2f,false);cube(m,q,light,0xFFE3BB67,-3,-.5f,-.5f,6,1,2,false);cube(m,q,light,0xFFC5DCE8,big?-1.3f:-.55f,.5f,0,big?2.6f:1.1f,big?19:14,.8f,false);cube(m,q,light,0xFFF1FAFF,big?1:-.1f,.5f,-.05f,.4f,big?18:13,.9f,false);}
}
