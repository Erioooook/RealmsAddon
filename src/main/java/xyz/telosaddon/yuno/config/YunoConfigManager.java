package xyz.telosaddon.yuno.config;

import net.fabricmc.loader.api.FabricLoader;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class YunoConfigManager {
    private static final Path CONFIG_PATH =
        FabricLoader.getInstance().getConfigDir().resolve("yuno_config.json");

    private static YunoConfigData data;

    public static void init() {
        if (Files.notExists(CONFIG_PATH)) {
            data = new YunoConfigData();
            save();
        } else {
            load();
        }
    }

    public static void load() {
        try {
            String txt = Files.readString(CONFIG_PATH);
            JSONObject obj = new JSONObject(txt);
            data = new YunoConfigData();
            data.healthThreshold   = (float) obj.optDouble("healthThreshold", data.healthThreshold);
            data.autoDropEnabled   = obj.optBoolean("autoDropEnabled", data.autoDropEnabled);
        } catch (IOException e) {
            data = new YunoConfigData();
            save();
        }
    }

    public static void save() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("healthThreshold",   data.healthThreshold);
            obj.put("autoDropEnabled",   data.autoDropEnabled);

            Files.writeString(CONFIG_PATH, obj.toString(4)); // pretty print
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static float  getThreshold() { return data.healthThreshold; }
    public static boolean isEnabled()   { return data.autoDropEnabled; }

    public static void setThreshold(float t) {
        data.healthThreshold = t; save();
    }

    public static void toggleEnabled() {
        data.autoDropEnabled = !data.autoDropEnabled; save();
    }
}
