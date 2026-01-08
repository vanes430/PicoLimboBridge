package github.vanes430.picolimbobridge.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import github.vanes430.picolimbobridge.common.BridgeConstants;
import github.vanes430.picolimbobridge.common.PicoLimboManager;
import github.vanes430.picolimbobridge.common.PicoLogger;
import github.vanes430.picolimbobridge.common.PlatformUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

@Plugin(
    id = "picolimbobridge",
    name = "PicoLimboBridge",
    version = "1.0.0-SNAPSHOT",
    description = "A bridge plugin for PicoLimbo on Velocity",
    authors = {"vanes430"}
)
public class PicoLimboBridgeVelocity {

    private final ProxyServer server;
    private final PicoLogger logger;
    private PicoLimboManager limboManager;

    @Inject
    public PicoLimboBridgeVelocity(ProxyServer server) {
        this.server = server;
        this.logger = new VelocityPicoLogger(server);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("§aPicoLimboBridge Velocity enabled!");
        logger.info("§7Using channel: §f" + BridgeConstants.CHANNEL);
        logger.info("§7Platform: §f" + PlatformUtils.getOS() + " (" + PlatformUtils.getArch() + ")");

        // Use 'picolimbo' folder in server root
        limboManager = new PicoLimboManager(Paths.get("picolimbo"), logger);
        server.getCommandManager().register("picolimbo", new PicoLimboVelocityCommand(limboManager));
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (limboManager != null) {
            limboManager.stop();
        }
    }
}
