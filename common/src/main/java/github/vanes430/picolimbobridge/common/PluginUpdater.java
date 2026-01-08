package github.vanes430.picolimbobridge.common;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.file.Path;

public class PluginUpdater {

    private static final String GITHUB_REPO = "vanes430/PicoLimboBridge";
    private static final String GITHUB_API_URL = "https://api.github.com/repos/" + GITHUB_REPO + "/releases/latest";
    private static final String MODRINTH_URL = "https://modrinth.com/plugin/picolimbobridge";
    
    private final PicoLogger logger;
    private final Path pluginJar;
    private final String platform; // "spigot" or "velocity"

    public PluginUpdater(PicoLogger logger, Path pluginJar, String platform) {
        this.logger = logger;
        this.pluginJar = pluginJar;
        this.platform = platform;
    }

    public void checkAndUpdate() {
        logger.info("§eChecking for plugin updates...");
        
        try {
            // 1. Fetch latest release info
            String jsonResponse = BridgeUtils.fetchUrl(GITHUB_API_URL);
            JsonObject release = JsonParser.parseString(jsonResponse).getAsJsonObject();
            String tagName = release.get("tag_name").getAsString();
            
            // 2. Find sha256sum.txt asset
            String sha256Url = null;
            for (JsonElement el : release.getAsJsonArray("assets")) {
                JsonObject asset = el.getAsJsonObject();
                if (asset.get("name").getAsString().equals("sha256sum.txt")) {
                    sha256Url = asset.get("browser_download_url").getAsString();
                    break;
                }
            }

            if (sha256Url == null) {
                logger.warning("§csha256sum.txt not found in latest release.");
                return;
            }

            // 3. Fetch hashes
            String hashesContent = BridgeUtils.fetchUrl(sha256Url);
            String expectedHash = null;
            
            // Parse lines like: HASH  filename
            try (BufferedReader reader = new BufferedReader(new StringReader(hashesContent))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Look for the line corresponding to our platform
                    // picolimbobridge-spigot-1.0.0-SNAPSHOT.jar
                    // picolimbobridge-velocity-1.0.0-SNAPSHOT.jar
                    if (line.contains("picolimbobridge-" + platform)) {
                        String[] parts = line.trim().split("\\s+");
                        if (parts.length >= 1) {
                            expectedHash = parts[0];
                        }
                        break;
                    }
                }
            }

            if (expectedHash == null) {
                logger.warning("§cCould not find hash for " + platform + " in sha256sum.txt");
                return;
            }

            // 4. Compare with local hash
            String localHash = BridgeUtils.calculateSha256(pluginJar);
            
            if (localHash.equalsIgnoreCase(expectedHash)) {
                logger.info("§aPlugin is up to date (" + localHash.substring(0, 8) + ")");
            } else {
                logger.warning("§cPlugin version outdated!");
                logger.warning("§7Current Hash: " + localHash.substring(0, 8));
                logger.warning("§7Latest Hash:  " + expectedHash.substring(0, 8));
                logger.warning("§ePlease update at: §n" + MODRINTH_URL);
            }

        } catch (Exception e) {
            logger.severe("§cUpdate check failed: " + e.getMessage());
        }
    }
}
