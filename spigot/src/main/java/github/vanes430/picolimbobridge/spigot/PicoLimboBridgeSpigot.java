package github.vanes430.picolimbobridge.spigot;

import github.vanes430.picolimbobridge.common.BridgeConstants;
import github.vanes430.picolimbobridge.common.PicoLimboManager;
import github.vanes430.picolimbobridge.common.PlatformUtils;
import org.bukkit.plugin.java.JavaPlugin;
import java.nio.file.Paths;

public class PicoLimboBridgeSpigot extends JavaPlugin {

    private PicoLimboManager limboManager;

    @Override
    public void onEnable() {
        getServer().getConsoleSender().sendMessage(BridgeConstants.PREFIX + "§aSpigot enabled!");
        getServer().getConsoleSender().sendMessage(BridgeConstants.PREFIX + "§7Using channel: §f" + BridgeConstants.CHANNEL);
        getServer().getConsoleSender().sendMessage(BridgeConstants.PREFIX + "§7Platform: §f" + PlatformUtils.getOS() + " (" + PlatformUtils.getArch() + ")");

        // Use 'picolimbo' folder in server root
        limboManager = new PicoLimboManager(Paths.get("picolimbo"), new SpigotPicoLogger());
        getCommand("picolimbo").setExecutor(new PicoLimboSpigotCommand(limboManager));
    }

    @Override
    public void onDisable() {
        if (limboManager != null) {
            limboManager.stop();
        }
        getServer().getConsoleSender().sendMessage(BridgeConstants.PREFIX + "§cSpigot disabled!");
    }
}
