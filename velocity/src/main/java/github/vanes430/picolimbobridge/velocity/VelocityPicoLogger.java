package github.vanes430.picolimbobridge.velocity;

import com.velocitypowered.api.proxy.ProxyServer;
import github.vanes430.picolimbobridge.common.BridgeConstants;
import github.vanes430.picolimbobridge.common.PicoLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class VelocityPicoLogger implements PicoLogger {

    private final ProxyServer server;

    public VelocityPicoLogger(ProxyServer server) {
        this.server = server;
    }

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
        Component component = LegacyComponentSerializer.legacySection().deserialize(BridgeConstants.PREFIX + message);
        server.getConsoleCommandSource().sendMessage(component);
    }
}
