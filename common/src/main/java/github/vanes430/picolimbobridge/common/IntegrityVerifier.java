package github.vanes430.picolimbobridge.common;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class IntegrityVerifier {
    private static final String HASHES_URL = "https://raw.githubusercontent.com/vanes430/PicoLimboBridge/refs/heads/master/sha256/latest.txt";
    private final Path rootDir;
    private final PicoLogger logger;
    private final BinaryManager binaryManager;

    public IntegrityVerifier(Path rootDir, PicoLogger logger, BinaryManager binaryManager) {
        this.rootDir = rootDir;
        this.logger = logger;
        this.binaryManager = binaryManager;
    }

    public void verify() {
        Path path = binaryManager.getBinaryPath();
        if (!Files.exists(path)) return;
        try {
            logger.info(MessageManager.get("verifying-integrity"));
            String expected = fetchExpectedHash();
            if (expected == null) {
                logger.warning(MessageManager.get("fail-fetch-hash"));
                return;
            }
            String actual = BridgeUtils.calculateSha256(path);
            if (!actual.equalsIgnoreCase(expected)) {
                logger.warning(MessageManager.get("hash-mismatch"));
                Files.deleteIfExists(path);
                Files.deleteIfExists(rootDir.resolve("version.txt"));
                binaryManager.installIfNeeded();
            } else logger.info(MessageManager.get("integrity-verified"));
        } catch (Exception e) {
            logger.warning(MessageManager.get("error-verifying-integrity", e.getMessage()));
        }
    }

    private String fetchExpectedHash() {
        try {
            HttpURLConnection c = (HttpURLConnection) new URL(HASHES_URL).openConnection();
            c.setRequestProperty("User-Agent", "PicoLimboBridge");
            try (BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream(), StandardCharsets.UTF_8))) {
                String line, key = getOsKey() + "-" + getArchKey();
                while ((line = r.readLine()) != null) {
                    if (line.contains("PREVIOUS VERSION")) break;
                    if (line.startsWith(key + ":")) return line.split(":")[1].trim();
                }
            }
        } catch (IOException e) {
            logger.warning(MessageManager.get("fail-fetch-hashes", e.getMessage()));
        }
        return null;
    }

    private String getOsKey() {
        return switch (PlatformUtils.getOS()) {
            case WINDOWS -> "windows"; case LINUX -> "linux"; case MACOS -> "macos"; default -> "unknown";
        };
    }

    private String getArchKey() {
        return switch (PlatformUtils.getArch()) {
            case AMD64 -> "x86_64"; case ARM64 -> "aarch64"; default -> "unknown";
        };
    }
}
