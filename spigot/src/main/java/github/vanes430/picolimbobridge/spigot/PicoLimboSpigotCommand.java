package github.vanes430.picolimbobridge.spigot;

import github.vanes430.picolimbobridge.common.BridgeConstants;
import github.vanes430.picolimbobridge.common.PicoLimboManager;
import github.vanes430.picolimbobridge.common.PluginUpdater;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PicoLimboSpigotCommand implements CommandExecutor {

    private final PicoLimboManager manager;
    private final PluginUpdater updater;

    public PicoLimboSpigotCommand(PicoLimboManager manager, PluginUpdater updater) {
        this.manager = manager;
        this.updater = updater;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            return false;
        }

        String sub = args[0].toLowerCase();
        if (sub.equals("start")) {
            Integer port = null;
            if (args.length > 1) {
                try {
                    port = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    sendMessage(sender, "Invalid port number.");
                    return true;
                }
            }
            
            sendMessage(sender, "Starting PicoLimbo" + (port != null ? " on port " + port : "") + "...");
            Integer finalPort = port;
            // Run async to avoid blocking main thread during download/start
            new Thread(() -> {
                manager.start(finalPort);
                sendMessage(sender, "PicoLimbo start process completed (check console for details).");
            }).start();
            return true;
        } else if (sub.equals("stop")) {
            sendMessage(sender, "Stopping PicoLimbo...");
            manager.stop();
            return true;
        } else if (sub.equals("reinstall")) {
            boolean force = args.length > 1 && args[1].equalsIgnoreCase("force");
            sendMessage(sender, "Reinstalling PicoLimbo" + (force ? " (forcing clean)" : "") + "...");
            new Thread(() -> {
                manager.reinstall(force);
                sendMessage(sender, "PicoLimbo reinstall completed.");
            }).start();
            return true;
        } else if (sub.equals("update")) {
            new Thread(updater::checkAndUpdate).start();
            return true;
        }

        return false;
    }

    private void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(BridgeConstants.PREFIX + message);
    }
}
