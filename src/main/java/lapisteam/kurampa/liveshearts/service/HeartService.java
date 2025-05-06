package lapisteam.kurampa.liveshearts.service;

import lapisteam.kurampa.liveshearts.storage.PlayerRepository;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class HeartService {

    public static final int MAX_HEARTS = 10;

    private final PlayerRepository repository;
    private final JavaPlugin plugin;

    public HeartService(PlayerRepository repository, JavaPlugin plugin) {
        this.repository = repository;
        this.plugin     = plugin;
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
        removeHearts(player.getName(), 1);
        if (getHearts(player.getName()) <= 0) {
            player.setGameMode(GameMode.SPECTATOR);
        }
    }

    private void applyHealthAttribute(String playerName, int hearts) {
        Player player = Bukkit.getPlayerExact(playerName);
        if (player == null) return;

        double maxHp = hearts * 2.0;
        player.setMaxHealth(maxHp);

        if (player.getHealth() > maxHp) {
            player.setHealth(maxHp);
        }
    }
}
