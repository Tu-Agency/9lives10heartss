package lapisteam.kurampa.liveshearts.config;

import lapisteam.kurampa.liveshearts.util.ColorUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class Lang {

    private final JavaPlugin plugin;
    private FileConfiguration lang;

    public Lang(JavaPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "lang.yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource("lang.yml", false);
        }
        lang = YamlConfiguration.loadConfiguration(file);
    }

    public String msg(String path, Object... placeholders) {
        String raw = lang.getString("messages." + path, "§c<" + path + ">");
        String s = ColorUtil.translateHex(raw);

        if (placeholders.length % 2 == 0) {
            for (int i = 0; i < placeholders.length; i += 2) {
                String key = String.valueOf(placeholders[i]);
                String val = String.valueOf(placeholders[i + 1]);
                s = s.replace("{" + key + "}", val);
            }
        } else {
            for (int i = 0; i < placeholders.length; i++) {
                s = s.replace("{" + i + "}", String.valueOf(placeholders[i]));
            }
        }
        return s;
    }
}
