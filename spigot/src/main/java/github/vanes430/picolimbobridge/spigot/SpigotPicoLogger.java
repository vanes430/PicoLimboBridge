package github.vanes430.picolimbobridge.spigot;

import github.vanes430.picolimbobridge.common.BridgeConstants;
import github.vanes430.picolimbobridge.common.PicoLogger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class SpigotPicoLogger implements PicoLogger {

    @Override
    public void info(String message) {
        log(message);
    }

    @Override
    public void warning(String message) {
        log(message);
    }

    @Override
    public void severe(String message) {
        log(message);
    }

    private void log(String message) {
        Bukkit.getConsoleSender().sendMessage(BridgeConstants.PREFIX + message);
    }
}
