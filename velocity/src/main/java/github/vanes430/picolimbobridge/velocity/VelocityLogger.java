package github.vanes430.picolimbobridge.velocity;

import com.velocitypowered.api.proxy.ProxyServer;
import github.vanes430.picolimbobridge.common.BridgeConstants;
import github.vanes430.picolimbobridge.common.PicoLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VelocityLogger implements PicoLogger {

    private final Logger slf4jLogger = LoggerFactory.getLogger("PicoLimboBridge");

    @Override
    public void info(String message) {
        slf4jLogger.info(message);
    }

    @Override
    public void warning(String message) {
        slf4jLogger.warn(message);
    }

    @Override
    public void severe(String message) {
        slf4jLogger.error(message);
    }
}
