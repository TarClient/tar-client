package dev.tarclient.mod.mixin;
import net.minecraft.client.particle.*;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import static dev.tarclient.mod.TarClient.CONFIG;
@Mixin(ParticleManager.class)
public abstract class ParticlesMixin {
    @Shadow private <T extends ParticleEffect> Particle createParticle(T effect,double x,double y,double z,double vx,double vy,double vz){throw new AssertionError();}
    @Shadow public abstract void addParticle(Particle particle);
    @Inject(method="addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;",at=@At("HEAD"),cancellable=true)
    private void tar$particles(ParticleEffect effect,double x,double y,double z,double vx,double vy,double vz,CallbackInfoReturnable<Particle> ci){
        if(!CONFIG.on("particles"))return;String id=Registries.PARTICLE_TYPE.getId(effect.getType()).getPath();
        String category=id.contains("crit")||id.equals("enchanted_hit")?"crit":id.contains("effect")?"potion":"other";
        double factor=CONFIG.number("particles","all")*CONFIG.number("particles",category)/10000;
        int copies=(int)factor;if(java.util.concurrent.ThreadLocalRandom.current().nextDouble()<factor-copies)copies++;
        if(copies==0){ci.setReturnValue(null);return;}
        for(int i=1;i<copies;i++){var particle=createParticle(effect,x,y,z,vx,vy,vz);if(particle!=null)addParticle(particle);}
    }
}
