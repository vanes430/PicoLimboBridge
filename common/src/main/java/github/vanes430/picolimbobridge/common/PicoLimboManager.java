package github.vanes430.picolimbobridge.common;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class PicoLimboManager {
    private final Path rootDir;
    private final PicoLogger logger;
    private final BinaryManager binaryManager;
    private final IntegrityVerifier verifier;
    private Process process;
    private volatile boolean expectedStop = false;

    public PicoLimboManager(Path rootDir, PicoLogger logger) {
        this.rootDir = rootDir;
        this.logger = logger;
        this.binaryManager = new BinaryManager(rootDir, logger);
        this.verifier = new IntegrityVerifier(rootDir, logger, binaryManager);
    }

    public void start(Integer port) {
        if (process != null && process.isAlive()) {
            logger.warning(MessageManager.get("picolimbo-already-running"));
            return;
        }
        try {
            if (!Files.exists(rootDir)) Files.createDirectories(rootDir);
            verifier.verify();
            binaryManager.installIfNeeded();
            if (port != null) handlePort(port);
            Path binary = binaryManager.getBinaryPath();
            if (!Files.exists(binary)) {
                logger.severe(MessageManager.get("binary-not-found", binary.toAbsolutePath()));
                return;
            }
            if (PlatformUtils.getOS() != PlatformUtils.OS.WINDOWS) binary.toFile().setExecutable(true);
            process = new ProcessBuilder(binary.toAbsolutePath().toString()).directory(rootDir.toFile()).start();
            expectedStop = false;
            inheritIO(process.getInputStream()); inheritIO(process.getErrorStream());
            monitorProcess();
            logger.info(MessageManager.get("picolimbo-started", rootDir.toAbsolutePath()));
        } catch (Exception e) {
            logger.severe(MessageManager.get("error-starting", e.getMessage()));
        }
    }

    private void handlePort(int port) throws IOException {
        Path cfg = rootDir.resolve("server.toml");
        if (!Files.exists(cfg)) {
            logger.warning(MessageManager.get("server-toml-not-found-ignore-port"));
            return;
        }
        String content = Files.readString(cfg);
        String updated = content.replaceAll("bind\\s*=\\s*\"([0-9.]+):[0-9]+\"", "bind = \"$1:" + port + "\"");
        if (!content.equals(updated)) {
            Files.writeString(cfg, updated);
            logger.info(MessageManager.get("updated-port", port));
        }
    }

    public void stop() {
        if (process == null || !process.isAlive()) return;
        expectedStop = true;
        logger.info(MessageManager.get("stopping-picolimbo"));
        try {
            if (PlatformUtils.getOS() != PlatformUtils.OS.WINDOWS) {
                try { new ProcessBuilder("kill", "-INT", String.valueOf(process.pid())).start(); } catch (IOException e) {}
            } else process.destroy();
            if (!process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) process.destroyForcibly();
            else logger.info(MessageManager.get("picolimbo-stopped-gracefully"));
        } catch (Exception e) {
            logger.severe(MessageManager.get("error-stopping", e.getMessage()));
            process.destroyForcibly();
        }
    }

    public void reinstall(boolean force) {
        stop();
        logger.info(MessageManager.get("reinstalling-log", force));
        try {
            if (Files.exists(rootDir)) {
                try (var walk = Files.walk(rootDir)) {
                    walk.sorted(java.util.Comparator.reverseOrder()).forEach(p -> {
                        if (p.equals(rootDir) || (!force && p.getFileName().toString().equals("server.toml"))) return;
                        try { Files.delete(p); } catch (IOException e) {}
                    });
                }
            }
            binaryManager.installIfNeeded();
            logger.info(MessageManager.get("reinstall-completed"));
        } catch (IOException e) { logger.severe(MessageManager.get("error-reinstall", e.getMessage())); }
    }

    private void monitorProcess() {
        new Thread(() -> {
            try {
                int code = process.waitFor();
                if (!expectedStop && code != 0) logger.severe(MessageManager.get("picolimbo-crashed", code));
                else logger.info(MessageManager.get("picolimbo-exited-gracefully", code));
            } catch (InterruptedException e) { logger.warning(MessageManager.get("monitor-interrupted")); }
        }, "PicoLimbo-Monitor").start();
    }

    private void inheritIO(InputStream src) {
        new Thread(() -> {
            try (BufferedReader r = new BufferedReader(new InputStreamReader(src))) {
                String line;
                while ((line = r.readLine()) != null) logger.info(MessageManager.get("picolimbo-prefix", line));
            } catch (IOException e) {}
        }).start();
    }

    public void start() { start(null); }
}
