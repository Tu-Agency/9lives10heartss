package lapisteam.kurampa.liveshearts.command.impl;

import lapisteam.kurampa.liveshearts.command.BaseCommand;
import lapisteam.kurampa.liveshearts.config.Lang;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class ReloadCommand implements BaseCommand {

    private final JavaPlugin plugin;
    private final Lang lang;

    public ReloadCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        this.lang   = new Lang(plugin);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("9l.reload")) {
            sender.sendMessage(lang.msg("no_permission")); return;
        }
        plugin.reloadConfig();
        lang.load();
        sender.sendMessage(lang.msg("config_reloaded"));
    }
}
