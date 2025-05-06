package lapisteam.kurampa.liveshearts.service;

import lapisteam.kurampa.liveshearts.config.ConfigKeys;
import org.bukkit.plugin.java.JavaPlugin;

public final class GameModeService {

    public enum Mode { HARD, IMMORTAL }

    private final JavaPlugin plugin;

    public GameModeService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public Mode getMode() {
        String v = plugin.getConfig().getString(ConfigKeys.GAMEMODE, "hard").toUpperCase();
        return Mode.valueOf(v);
    }

    public void setMode(Mode mode) {
        plugin.getConfig().set(ConfigKeys.GAMEMODE, mode.name().toLowerCase());
        plugin.saveConfig();
    }
}
