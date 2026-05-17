package github.vanes430.picolimbobridge.paper;

import github.vanes430.picolimbobridge.common.BridgeConstants;
import github.vanes430.picolimbobridge.common.MessageManager;
import github.vanes430.picolimbobridge.common.PicoLimboManager;
import github.vanes430.picolimbobridge.common.PlatformUtils;
import github.vanes430.picolimbobridge.common.PluginUpdater;
import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PaperBridge extends JavaPlugin {

    private PicoLimboManager limboManager;

    @Override
    public void onLoad() {
        BukkitLibraryManager libraryManager = new BukkitLibraryManager(this);
        libraryManager.addMavenCentral();
        
        Library compress = Library.builder()
            .groupId("org{}apache{}commons")
            .artifactId("commons-compress")
            .version("1.26.0")
            .relocate("org{}apache{}commons{}compress", "github.vanes430.picolimbobridge.paper.libs.compress")
            .build();
            
        libraryManager.loadLibrary(compress);
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveDefaultMessages();
        loadMessages();
        
        boolean autoStart = getConfig().getBoolean("autostart-enabled", false);

        getServer().getConsoleSender().sendMessage(BridgeConstants.PREFIX + MessageManager.get("paper-enabled"));
        getServer().getConsoleSender().sendMessage(BridgeConstants.PREFIX + MessageManager.get("using-channel", BridgeConstants.CHANNEL));
        getServer().getConsoleSender().sendMessage(BridgeConstants.PREFIX + MessageManager.get("platform-info", PlatformUtils.getOS(), PlatformUtils.getArch()));

        PaperLogger logger = new PaperLogger();
        limboManager = new PicoLimboManager(Paths.get("picolimbo"), logger);
        
        PluginUpdater updater = new PluginUpdater(logger, getFile().toPath(), "paper");
        
        getCommand("picolimbo").setExecutor(new PaperCommandManager(limboManager, updater));

        if (autoStart) {
            Path serverConfig = Paths.get("picolimbo", "server.toml");
            if (Files.exists(serverConfig)) {
                getServer().getConsoleSender().sendMessage(BridgeConstants.PREFIX + MessageManager.get("auto-starting"));
                new Thread(() -> limboManager.start(null)).start();
            } else {
                getServer().getConsoleSender().sendMessage(BridgeConstants.PREFIX + MessageManager.get("auto-start-ignored-no-config"));
            }
        }
    }

    private void saveDefaultMessages() {
        File file = new File(getDataFolder(), "messages.yml");
        if (!file.exists()) {
            saveResource("messages.yml", false);
        }
    }

    private void loadMessages() {
        File file = new File(getDataFolder(), "messages.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        MessageManager.loadMessages(config.getValues(false));
    }

    @Override
    public void onDisable() {
        if (limboManager != null) {
            limboManager.stop();
        }
        getServer().getConsoleSender().sendMessage(BridgeConstants.PREFIX + MessageManager.get("paper-disabled"));
    }
}
