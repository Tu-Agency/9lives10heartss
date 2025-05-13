package lapisteam.kurampa.liveshearts.service;

import lapisteam.kurampa.liveshearts.config.Lang;
import lapisteam.kurampa.liveshearts.storage.PlayerRepository;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class HeartService {

    public static final int MAX_HEARTS = 10;

    private final PlayerRepository repository;
    private final JavaPlugin plugin;
    private final Lang lang;

    public HeartService(PlayerRepository repository, JavaPlugin plugin) {
        this.repository = repository;
        this.plugin     = plugin;
        this.lang       = new Lang(plugin);
    }

    public int getHearts(String playerName) {
        return repository.findHearts(playerName).orElse(MAX_HEARTS);
    }

    public void setHearts(String playerName, int hearts) {
        int h = Math.max(1, Math.min(MAX_HEARTS, hearts));
        repository.saveHearts(playerName, h);
        applyHealthAttribute(playerName, h);
    }

    public void addHearts(String playerName, int delta) {
        setHearts(playerName, getHearts(playerName) + delta);
    }

    public void removeHearts(String playerName, int delta) {
        setHearts(playerName, getHearts(playerName) - delta);
    }

    public void handleDeath(Player player) {
        String name = player.getName();
        int current = getHearts(name);
        String mode  = plugin.getConfig().getString("gamemode", "hard");

        if (mode.equalsIgnoreCase("immortal")) {
            if (current > 1) {
                repository.saveHearts(name, current - 1);
                applyHealthAttribute(name, current - 1);
            }
            return;
        }
        if (current > 1) {
            repository.saveHearts(name, current - 1);
            applyHealthAttribute(name, current - 1);
            player.sendMessage(lang.msg("hearts-decreased", "hearts", current - 1));
        } else {
            repository.saveHearts(name, 0);
            player.setGameMode(GameMode.SPECTATOR);
            player.sendMessage(lang.msg("spectator-mode"));
        }
    }



    private void applyHealthAttribute(String playerName, int hearts) {
        Player player = Bukkit.getPlayerExact(playerName);
        if (player == null) return;

        double hp = hearts * 2.0;
        player.setMaxHealth(hp);
        if (player.getHealth() > hp) {
            player.setHealth(hp);
        }
    }
}
