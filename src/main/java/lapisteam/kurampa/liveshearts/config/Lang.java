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

    public String msg(String key, Object... placeholders) {
        String raw = lang.getString("messages." + key, "§c<" + key + ">");
        String colored = ColorUtil.translateHex(raw);
        for (int i = 0; i < placeholders.length; i++) {
            colored = colored.replace("{" + i + "}", String.valueOf(placeholders[i]));
        }
        return colored;
    }
}
