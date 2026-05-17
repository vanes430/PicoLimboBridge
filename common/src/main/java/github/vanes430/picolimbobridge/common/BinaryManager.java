package github.vanes430.picolimbobridge.common;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class BinaryManager {
    private static final String GITHUB_API_URL = "https://api.github.com/repos/Quozul/PicoLimbo/releases/latest";
    private final Path rootDir;
    private final PicoLogger logger;

    public BinaryManager(Path rootDir, PicoLogger logger) {
        this.rootDir = rootDir;
        this.logger = logger;
    }

    public void installIfNeeded() throws IOException {
        logger.info(MessageManager.get("checking-updates"));
        JsonObject release = JsonParser.parseString(BridgeUtils.fetchUrl(GITHUB_API_URL)).getAsJsonObject();
        String latest = release.get("tag_name").getAsString();
        Path verFile = rootDir.resolve("version.txt");
        String current = Files.exists(verFile) ? Files.readString(verFile).trim() : "";

        if (!latest.equals(current) || !Files.exists(getBinaryPath())) {
            logger.info(MessageManager.get("new-version-found", latest, current.isEmpty() ? "None" : current));
            downloadAndInstall(release);
            Files.writeString(verFile, latest);
        } else {
            logger.info(MessageManager.get("picolimbo-up-to-date", current));
        }
    }

    private void downloadAndInstall(JsonObject release) throws IOException {
        String os = getOsKey(), arch = getArchKey(), url = null, name = null;
        for (JsonElement el : release.getAsJsonArray("assets")) {
            JsonObject a = el.getAsJsonObject();
            String n = a.get("name").getAsString();
            if (n.contains(os) && n.contains(arch) && (!os.equals("linux") || n.contains("gnu"))) {
                url = a.get("browser_download_url").getAsString();
                name = n; break;
            }
        }
        if (url == null) throw new IOException("No binary for " + os + " " + arch);
        logger.info(MessageManager.get("downloading-binary", name));
        Path archive = rootDir.resolve(name);
        BridgeUtils.downloadFile(url, archive);
        logger.info(MessageManager.get("extracting-binary"));
        if (name.endsWith(".zip")) ArchiveUtils.extractZip(archive, rootDir);
        else ArchiveUtils.extractTarGz(archive, rootDir);
        Files.delete(archive);
        checkBinaryNaming();
    }

    private void checkBinaryNaming() throws IOException {
        Path expected = getBinaryPath();
        if (!Files.exists(expected)) {
            Path alt = rootDir.resolve(PlatformUtils.getOS() == PlatformUtils.OS.WINDOWS ? "pico_limbo.exe" : "pico_limbo");
            if (Files.exists(alt)) Files.move(alt, expected, StandardCopyOption.REPLACE_EXISTING);
            else logger.warning(MessageManager.get("locate-binary-fail"));
        }
    }

    public Path getBinaryPath() {
        return rootDir.resolve(PlatformUtils.getOS() == PlatformUtils.OS.WINDOWS ? "picolimbo.exe" : "picolimbo");
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
