package dev.tarclient.mod.mixin;
import dev.tarclient.mod.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.*;
import net.minecraft.client.session.telemetry.TelemetryManager;
import net.minecraft.client.session.report.*;
import net.minecraft.client.network.SocialInteractionsManager;
import net.minecraft.client.realms.*;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import java.util.concurrent.CompletableFuture;
import org.spongepowered.asm.mixin.*;

@Mixin(MinecraftClient.class)
public abstract class AccountSessionMixin implements AccountSessionAccess {
    @Shadow @Final @Mutable private Session session;
    @Shadow @Final @Mutable private CompletableFuture<ProfileResult> gameProfileFuture;
    @Shadow @Final @Mutable private UserApiService userApiService;
    @Shadow @Final @Mutable private CompletableFuture<UserApiService.UserProperties> userPropertiesFuture;
    @Shadow @Final @Mutable private SocialInteractionsManager socialInteractionsManager;
    @Shadow @Final @Mutable private TelemetryManager telemetryManager;
    @Shadow @Final @Mutable private ProfileKeys profileKeys;
    @Shadow private AbuseReportContext abuseReportContext;
    @Shadow @Final @Mutable private RealmsPeriodicCheckers realmsPeriodicCheckers;
    @Shadow @Final @Mutable private boolean isDemo;
    @Override public void tar$applyAccount(PreparedAccount next){
        var client=(MinecraftClient)(Object)this;
        if(!client.isOnThread()||client.world!=null||client.getNetworkHandler()!=null)throw new IllegalStateException("Leave the world before switching accounts.");
        // Construct replacements first; errors leave the old session and its services intact.
        var social=new SocialInteractionsManager(client,next.api());
        var keys=ProfileKeys.create(next.api(),next.session(),client.runDirectory.toPath());
        var reports=AbuseReportContext.create(ReporterEnvironment.ofIntegratedServer(),next.api());
        var realms=RealmsClientAccess.tar$create(next.session().getSessionId(),next.session().getUsername(),client);
        var checkers=new RealmsPeriodicCheckers(realms);
        var telemetry=new TelemetryManager(client,next.api(),next.session());
        var oldTelemetry=telemetryManager;
        session=next.session();userApiService=next.api();
        gameProfileFuture=CompletableFuture.completedFuture(next.profile());
        userPropertiesFuture=CompletableFuture.completedFuture(next.properties());
        socialInteractionsManager=social;profileKeys=keys;abuseReportContext=reports;
        realmsPeriodicCheckers=checkers;telemetryManager=telemetry;isDemo=false;
        RealmsClientAccess.tar$setInstance(realms);RealmsAvailabilityAccess.tar$clear(null);
        ((SplashSessionAccess)client.getSplashTextLoader()).tar$session(session);
        try{oldTelemetry.close();}catch(Exception ignored){}
    }
}
