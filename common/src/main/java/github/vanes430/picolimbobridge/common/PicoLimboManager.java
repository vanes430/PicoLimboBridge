package github.vanes430.picolimbobridge.common;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.Locale;
import java.util.logging.Logger; // Kept only if needed for other things, but here replacing usage.
// Actually, I should remove unused import if I replace all usages.
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class PicoLimboManager {

    private static final String GITHUB_API_URL = "https://api.github.com/repos/Quozul/PicoLimbo/releases/latest";
    private final Path rootDir;
    private final PicoLogger logger;
    private Process process;
    private volatile boolean expectedStop = false;

    public PicoLimboManager(Path rootDir, PicoLogger logger) {
        this.rootDir = rootDir;
        this.logger = logger;
    }

    public void start(Integer port) {
        if (process != null && process.isAlive()) {
            logger.warning("§cPicoLimbo is already running!");
            return;
        }

        try {
            // Ensure picolimbo directory exists
            if (!Files.exists(rootDir)) {
                Files.createDirectories(rootDir);
            }

            // Install only if binary is missing
            if (!Files.exists(getBinaryPath())) {
                checkForUpdatesAndInstall();
            }

            // Update port if provided and server.toml exists
            if (port != null) {
                Path serverConfig = rootDir.resolve("server.toml");
                if (Files.exists(serverConfig)) {
                    updateServerConfigPort(serverConfig, port);
                } else {
                    logger.warning("§7server.toml not found, ignoring port argument.");
                }
            }

            Path binaryPath = getBinaryPath();
            if (!Files.exists(binaryPath)) {
                logger.severe("§cPicoLimbo binary not found at: " + binaryPath.toAbsolutePath());
                return;
            }

            // Make executable if on Linux/Mac
            if (PlatformUtils.getOS() != PlatformUtils.OS.WINDOWS) {
                binaryPath.toFile().setExecutable(true);
            }

            ProcessBuilder pb = new ProcessBuilder(binaryPath.toAbsolutePath().toString());
            pb.directory(rootDir.toFile());
            
            // Redirect output to logger
            process = pb.start();
            expectedStop = false;
            
            inheritIO(process.getInputStream(), "INFO");
            inheritIO(process.getErrorStream(), "ERROR");
            
            monitorProcess();

            logger.info("§aPicoLimbo started in §f" + rootDir.toAbsolutePath());

        } catch (Exception e) {
            logger.severe("§cError starting PicoLimbo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateServerConfigPort(Path configPath, int port) {
        try {
            String content = Files.readString(configPath);
            // Regex to replace bind = "0.0.0.0:PORT" or similar
            // Assuming default or typical config structure. 
            // We look for 'bind = "IP:PORT"' pattern.
            // Adjust regex as needed based on exact toml format of PicoLimbo
            String newContent = content.replaceAll("bind\\s*=\\s*\"([0-9.]+):[0-9]+\"", "bind = \"$1:" + port + "\"");
            
            if (!content.equals(newContent)) {
                 Files.writeString(configPath, newContent);
                 logger.info("§aUpdated PicoLimbo port to §f" + port);
            }
        } catch (IOException e) {
            logger.severe("§cFailed to update server.toml port: " + e.getMessage());
        }
    }

    public void start() {
        start(null);
    }

    public void stop() {
        if (process != null && process.isAlive()) {
            expectedStop = true;
            logger.info("§eStopping PicoLimbo...");
            
            try {
                boolean signalSent = false;
                
                // Attempt 1: Try SIGINT via 'kill' command (Non-Windows)
                if (PlatformUtils.getOS() != PlatformUtils.OS.WINDOWS) {
                    try {
                        long pid = process.pid();
                        new ProcessBuilder("kill", "-INT", String.valueOf(pid)).start();
                        signalSent = true;
                    } catch (IOException e) {
                        logger.warning("§c'kill' command not found or failed. Falling back to native SIGTERM.");
                    }
                }

                // Attempt 2: Native SIGTERM (Windows or fallback for missing 'kill')
                if (!signalSent) {
                    process.destroy();
                }

                // Wait for graceful exit
                if (!process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    logger.warning("§cPicoLimbo did not stop gracefully. Forcing exit...");
                    process.destroyForcibly();
                } else {
                    logger.info("§aPicoLimbo stopped gracefully.");
                }
            } catch (Exception e) {
                logger.severe("§cError stopping PicoLimbo: " + e.getMessage());
                process.destroyForcibly();
            }
        }
    }

    public void reinstall(boolean force) {
        stop();
        logger.info("§eReinstalling PicoLimbo (force=" + force + ")...");
        
        try {
            if (Files.exists(rootDir)) {
                // List all files
                try (java.util.stream.Stream<Path> walk = Files.walk(rootDir)) {
                     walk.sorted(java.util.Comparator.reverseOrder())
                         .forEach(path -> {
                             if (path.equals(rootDir)) return;
                             if (!force && path.getFileName().toString().equals("server.toml")) return;
                             
                             try {
                                 Files.delete(path);
                             } catch (IOException e) {
                                 logger.warning("§cFailed to delete " + path + ": " + e.getMessage());
                             }
                         });
                }
            }
            
            // This will trigger download since files (including version.txt) are gone
            checkForUpdatesAndInstall();
            logger.info("§aPicoLimbo reinstalled successfully.");
            
        } catch (IOException e) {
            logger.severe("§cError during reinstall: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void monitorProcess() {
        new Thread(() -> {
            try {
                int exitCode = process.waitFor();
                if (!expectedStop) {
                    if (exitCode != 0) {
                        logger.severe("§cPicoLimbo crashed with exit code: " + exitCode);
                    }
                } else {
                    logger.info("§7PicoLimbo process exited gracefully (Code: " + exitCode + ")");
                }
            } catch (InterruptedException e) {
                logger.warning("§7PicoLimbo monitor thread interrupted.");
            }
        }, "PicoLimbo-Monitor").start();
    }

    private void inheritIO(InputStream src, String level) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(src))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.info("§8[§bPicoLimbo§8] §f" + line);
                }
            } catch (IOException e) {
                // Ignore stream closed errors
            }
        }).start();
    }

    private Path getBinaryPath() {
        String binaryName = PlatformUtils.getOS() == PlatformUtils.OS.WINDOWS ? "picolimbo.exe" : "picolimbo";
        return rootDir.resolve(binaryName);
    }

    private void checkForUpdatesAndInstall() throws IOException {
        logger.info("§eChecking for PicoLimbo updates...");
        String jsonResponse = fetchUrl(GITHUB_API_URL);
        JsonObject release = JsonParser.parseString(jsonResponse).getAsJsonObject();
        String latestVersion = release.get("tag_name").getAsString();
        
        Path versionFile = rootDir.resolve("version.txt");
        String currentVersion = "";
        if (Files.exists(versionFile)) {
            currentVersion = Files.readString(versionFile).trim();
        }

        if (!latestVersion.equals(currentVersion)) {
            logger.info("§aNew version found: §f" + latestVersion + " §7(Current: " + (currentVersion.isEmpty() ? "None" : currentVersion) + ")");
            downloadAndInstall(release);
            Files.writeString(versionFile, latestVersion);
            logger.info("§aPicoLimbo updated to §f" + latestVersion);
        } else {
            logger.info("§7PicoLimbo is up to date (§f" + currentVersion + "§7).");
            // Ensure binary exists even if version matches (e.g. deleted manually)
            if (!Files.exists(getBinaryPath())) {
                 logger.warning("§cBinary missing, forcing re-download...");
                 downloadAndInstall(release);
            }
        }
    }

    private void downloadAndInstall(JsonObject release) throws IOException {
        String downloadUrl = null;
        String assetName = null;

        String osKey = getOsKey();
        String archKey = getArchKey();

        for (JsonElement el : release.getAsJsonArray("assets")) {
            JsonObject asset = el.getAsJsonObject();
            String name = asset.get("name").getAsString();
            if (name.contains(osKey) && name.contains(archKey)) {
                downloadUrl = asset.get("browser_download_url").getAsString();
                assetName = name;
                break;
            }
        }

        if (downloadUrl == null) {
            throw new IOException("No compatible binary found for " + osKey + " " + archKey);
        }

        logger.info("§eDownloading §f" + assetName + "§e...");
        Path archivePath = rootDir.resolve(assetName);
        downloadFile(downloadUrl, archivePath);

        logger.info("§eExtracting...");
        if (assetName.endsWith(".zip")) {
            extractZip(archivePath, rootDir);
        } else if (assetName.endsWith(".tar.gz")) {
            extractTarGz(archivePath, rootDir);
        }

        Files.delete(archivePath);
        
        String expectedName = PlatformUtils.getOS() == PlatformUtils.OS.WINDOWS ? "picolimbo.exe" : "picolimbo";
        Path expectedPath = rootDir.resolve(expectedName);
        
        if (!Files.exists(expectedPath)) {
             String altName = PlatformUtils.getOS() == PlatformUtils.OS.WINDOWS ? "pico_limbo.exe" : "pico_limbo";
             Path altPath = rootDir.resolve(altName);
             if (Files.exists(altPath)) {
                 Files.move(altPath, expectedPath, StandardCopyOption.REPLACE_EXISTING);
             } else {
                 logger.warning("§cCould not automatically locate 'picolimbo' binary after extraction.");
             }
        }
    }

    private String getOsKey() {
        switch (PlatformUtils.getOS()) {
            case WINDOWS: return "windows";
            case LINUX: return "linux";
            case MACOS: return "macos";
            default: return "unknown";
        }
    }

    private String getArchKey() {
        switch (PlatformUtils.getArch()) {
            case AMD64: return "x86_64";
            case ARM64: return "aarch64";
            default: return "unknown";
        }
    }

    private String fetchUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "PicoLimboBridge");
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        }
    }

    private void downloadFile(String urlString, Path destination) throws IOException {
        URL url = new URL(urlString);
        try (InputStream in = url.openStream()) {
            Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void extractZip(Path zipFile, Path outputDir) throws IOException {
        try (ZipFile zip = new ZipFile(zipFile.toFile())) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                Path entryPath = outputDir.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    try (InputStream in = zip.getInputStream(entry)) {
                        Files.copy(in, entryPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        }
    }

    private void extractTarGz(Path tarGzFile, Path outputDir) throws IOException {
        try (InputStream fi = Files.newInputStream(tarGzFile);
             BufferedInputStream bi = new BufferedInputStream(fi);
             GzipCompressorInputStream gzi = new GzipCompressorInputStream(bi);
             TarArchiveInputStream ti = new TarArchiveInputStream(gzi)) {

            TarArchiveEntry entry;
            while ((entry = ti.getNextTarEntry()) != null) {
                Path entryPath = outputDir.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    try (OutputStream out = Files.newOutputStream(entryPath)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = ti.read(buffer)) != -1) {
                            out.write(buffer, 0, len);
                        }
                    }
                }
            }
        }
    }
}