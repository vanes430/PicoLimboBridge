package github.vanes430.picolimbobridge.paper;

import github.vanes430.picolimbobridge.common.BridgeConstants;
import github.vanes430.picolimbobridge.common.PicoLogger;
import org.bukkit.Bukkit;

public class PaperLogger implements PicoLogger {

    @Override
    public void info(String message) {
        Bukkit.getLogger().info(BridgeConstants.PREFIX + message);
    }

    @Override
    public void warning(String message) {
        Bukkit.getLogger().warning(BridgeConstants.PREFIX + message);
    }

    @Override
    public void severe(String message) {
        Bukkit.getLogger().severe(BridgeConstants.PREFIX + message);
    }
}
