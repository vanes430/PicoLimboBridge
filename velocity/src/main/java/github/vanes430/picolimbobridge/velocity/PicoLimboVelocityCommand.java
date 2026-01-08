package github.vanes430.picolimbobridge.velocity;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.command.CommandSource;
import github.vanes430.picolimbobridge.common.BridgeConstants;
import github.vanes430.picolimbobridge.common.PicoLimboManager;
import github.vanes430.picolimbobridge.common.PluginUpdater;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PicoLimboVelocityCommand implements SimpleCommand {

    private final PicoLimboManager manager;
    private final PluginUpdater updater;

    public PicoLimboVelocityCommand(PicoLimboManager manager, PluginUpdater updater) {
        this.manager = manager;
        this.updater = updater;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length < 1) {
            sendMessage(source, "Usage: /picolimbo <start [port]|stop|reinstall [force]|update>");
            return;
        }

        String sub = args[0].toLowerCase();
        if (sub.equals("start")) {
            Integer port = null;
            if (args.length > 1) {
                try {
                    port = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    sendMessage(source, "Invalid port number.");
                    return;
                }
            }

            sendMessage(source, "Starting PicoLimbo" + (port != null ? " on port " + port : "") + "...");
            Integer finalPort = port;
            CompletableFuture.runAsync(() -> manager.start(finalPort)).thenRun(() -> 
                sendMessage(source, "PicoLimbo start process completed (check console).")
            );
        } else if (sub.equals("stop")) {
            sendMessage(source, "Stopping PicoLimbo...");
            manager.stop();
        } else if (sub.equals("reinstall")) {
            boolean force = args.length > 1 && args[1].equalsIgnoreCase("force");
            sendMessage(source, "Reinstalling PicoLimbo" + (force ? " (forcing clean)" : "") + "...");
            CompletableFuture.runAsync(() -> manager.reinstall(force)).thenRun(() -> 
                sendMessage(source, "PicoLimbo reinstall completed.")
            );
        } else if (sub.equals("update")) {
            CompletableFuture.runAsync(updater::checkAndUpdate);
        } else {
             sendMessage(source, "Usage: /picolimbo <start [port]|stop|reinstall [force]|update>");
        }
    }

    private void sendMessage(CommandSource source, String message) {
        source.sendMessage(LegacyComponentSerializer.legacySection().deserialize(BridgeConstants.PREFIX + message));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return List.of("start", "stop", "reinstall", "update");
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("picolimbobridge.admin");
    }
}
