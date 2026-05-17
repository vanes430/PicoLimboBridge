package github.vanes430.picolimbobridge.paper;

import github.vanes430.picolimbobridge.common.BridgeConstants;
import github.vanes430.picolimbobridge.common.MessageManager;
import github.vanes430.picolimbobridge.common.PicoLimboManager;
import github.vanes430.picolimbobridge.common.PluginUpdater;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PaperCommandManager implements CommandExecutor {

    private final PicoLimboManager manager;
    private final PluginUpdater updater;

    public PaperCommandManager(PicoLimboManager manager, PluginUpdater updater) {
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
                    sendMessage(sender, MessageManager.get("invalid-port"));
                    return true;
                }
            }
            
            if (port != null) {
                sendMessage(sender, MessageManager.get("starting-picolimbo-port", port));
            } else {
                sendMessage(sender, MessageManager.get("starting-picolimbo"));
            }

            Integer finalPort = port;
            new Thread(() -> {
                manager.start(finalPort);
                sendMessage(sender, MessageManager.get("start-completed"));
            }).start();
            return true;
        } else if (sub.equals("stop")) {
            sendMessage(sender, MessageManager.get("stopping-picolimbo"));
            manager.stop();
            return true;
        } else if (sub.equals("reinstall")) {
            boolean force = args.length > 1 && args[1].equalsIgnoreCase("force");
            if (force) {
                sendMessage(sender, MessageManager.get("reinstalling-picolimbo-force"));
            } else {
                sendMessage(sender, MessageManager.get("reinstalling-picolimbo"));
            }
            new Thread(() -> {
                manager.reinstall(force);
                sendMessage(sender, MessageManager.get("reinstall-completed"));
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
