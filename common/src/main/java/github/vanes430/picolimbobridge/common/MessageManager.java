package github.vanes430.picolimbobridge.common;

import java.util.HashMap;
import java.util.Map;

public class MessageManager {
    private static final Map<String, String> messages = new HashMap<>();

    public static void loadMessages(Map<String, Object> data) {
        messages.clear();
        if (data != null) {
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                messages.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
    }

    public static String get(String key, Object... args) {
        String msg = messages.getOrDefault(key, key);
        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                msg = msg.replace("{" + i + "}", String.valueOf(args[i]));
            }
        }
        return msg;
    }
}
