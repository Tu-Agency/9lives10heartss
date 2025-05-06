package lapisteam.kurampa.liveshearts.command.impl;

import lapisteam.kurampa.liveshearts.command.BaseCommand;
import lapisteam.kurampa.liveshearts.config.Lang;
import lapisteam.kurampa.liveshearts.service.HeartService;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class LookCommand implements BaseCommand {

    private final HeartService service;
    private final Lang lang;

    public LookCommand(HeartService service, JavaPlugin plugin) {
        this.service = service;
        this.lang    = new Lang(plugin);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("9l.look")) {
            sender.sendMessage(lang.msg("no_permission")); return;
        }
        if (args.length != 2) {
            sender.sendMessage(lang.msg("command_usage")); return;
        }

        String playerName = args[1];
        int hearts = service.getHearts(playerName);

        Player target = Bukkit.getPlayerExact(playerName);
        if (target != null && target.getGameMode() == GameMode.SPECTATOR) {
            sender.sendMessage(lang.msg("hearts_spectator_mode"));
        } else {
            sender.sendMessage(lang.msg("hearts_look", hearts));
        }
    }
}
