package lapisteam.kurampa.liveshearts;

import lapisteam.kurampa.liveshearts.command.CommandManager;
import lapisteam.kurampa.liveshearts.listener.PlayerListener;
import lapisteam.kurampa.liveshearts.service.HeartService;
import lapisteam.kurampa.liveshearts.storage.sqlite.SQLitePlayerRepository;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private HeartService heartService;

    @Override
    public void onEnable() {

        saveDefaultConfig();
        saveResource("lang.yml", false);

        SQLitePlayerRepository repository = new SQLitePlayerRepository(this);
        heartService = new HeartService(repository, this);

        getServer().getPluginManager()
                .registerEvents(new PlayerListener(heartService, this), this);

        new CommandManager(this, heartService).register();
    }

    public HeartService getHeartService() {
        return heartService;
    }

@Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
