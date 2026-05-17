package github.vanes430.picolimbobridge.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import github.vanes430.picolimbobridge.common.BridgeConstants;
import github.vanes430.picolimbobridge.common.MessageManager;
import github.vanes430.picolimbobridge.common.PicoLimboManager;
import github.vanes430.picolimbobridge.common.PicoLogger;
import github.vanes430.picolimbobridge.common.PlatformUtils;
import github.vanes430.picolimbobridge.common.PluginUpdater;
import net.byteflux.libby.Library;
import net.byteflux.libby.VelocityLibraryManager;
import org.slf4j.Logger;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;
import com.velocitypowered.api.plugin.annotation.DataDirectory;

@Plugin(
    id = "picolimbobridge"
)
public class VelocityBridge {

    private final ProxyServer server;
    private final Logger slf4jLogger;
    private final PicoLogger logger;
    private final Path dataDirectory;
    private PicoLimboManager limboManager;
    private boolean autoStart = false;

    @Inject
    public VelocityBridge(ProxyServer server, Logger slf4jLogger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.slf4jLogger = slf4jLogger;
        this.dataDirectory = dataDirectory;
        this.logger = new VelocityLogger();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        // Load libraries first
        VelocityLibraryManager<VelocityBridge> libraryManager = new VelocityLibraryManager<>(slf4jLogger, dataDirectory, server.getPluginManager(), this);
        libraryManager.addMavenCentral();
        
        Library compress = Library.builder()
            .groupId("org{}apache{}commons")
            .artifactId("commons-compress")
            .version("1.26.0")
            .relocate("org{}apache{}commons{}compress", "github.vanes430.picolimbobridge.velocity.libs.compress")
            .build();
            
        libraryManager.loadLibrary(compress);

        // Then continue with plugin logic
        loadConfig();
        loadMessages();

        logger.info(MessageManager.get("velocity-enabled"));
        logger.info(MessageManager.get("using-channel", BridgeConstants.CHANNEL));
        logger.info(MessageManager.get("platform-info", PlatformUtils.getOS(), PlatformUtils.getArch()));

        limboManager = new PicoLimboManager(Paths.get("picolimbo"), logger);

        Path pluginPath = Paths.get("plugins/picolimbobridge-velocity.jar");
        try {
            pluginPath = Paths.get(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (Exception e) {}

        PluginUpdater updater = new PluginUpdater(logger, pluginPath, "velocity");
        server.getCommandManager().register("picolimbo", new VelocityCommandManager(limboManager, updater));

        if (autoStart) {
            Path serverConfig = Paths.get("picolimbo", "server.toml");
            if (Files.exists(serverConfig)) {
                logger.info(MessageManager.get("auto-starting"));
                new Thread(() -> limboManager.start(null)).start();
            } else {
                logger.info(MessageManager.get("auto-start-ignored-no-config"));
            }
        }
    }

    private void loadConfig() {
        Path configFile = dataDirectory.resolve("config.yml");
        if (!Files.exists(dataDirectory)) {
            try { Files.createDirectories(dataDirectory); } catch (Exception e) {}
        }
        if (!Files.exists(configFile)) {
            try (InputStream in = getClass().getResourceAsStream("/config.yml")) {
                if (in != null) Files.copy(in, configFile);
            } catch (Exception e) {}
        }
        try (InputStream in = Files.newInputStream(configFile)) {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(in);
            if (data != null && data.containsKey("autostart-enabled")) {
                autoStart = (boolean) data.get("autostart-enabled");
            }
        } catch (Exception e) {}
    }

    private void loadMessages() {
        Path messagesFile = dataDirectory.resolve("messages.yml");
        if (!Files.exists(messagesFile)) {
            try (InputStream in = getClass().getResourceAsStream("/messages.yml")) {
                if (in != null) Files.copy(in, messagesFile);
            } catch (Exception e) {}
        }
        try (InputStream in = Files.newInputStream(messagesFile)) {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(in);
            MessageManager.loadMessages(data);
        } catch (Exception e) {}
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (limboManager != null) {
            limboManager.stop();
        }
    }
}
