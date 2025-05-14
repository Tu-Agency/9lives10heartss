package lapisteam.kurampa.liveshearts.command.impl;

import lapisteam.kurampa.liveshearts.command.BaseCommand;
import lapisteam.kurampa.liveshearts.config.Lang;
import lapisteam.kurampa.liveshearts.service.HeartService;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class RemoveCommand implements BaseCommand {

    private final HeartService service;
    private final Lang lang;

    public RemoveCommand(HeartService service, JavaPlugin plugin) {
        this.service = service;
        this.lang    = new Lang(plugin);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("9l.remove")) {
            sender.sendMessage(lang.msg("no_permission"));
            return;
        }
        if (args.length != 3) {
            sender.sendMessage(lang.msg("command_usage"));
            return;
        }

        String targetName = args[1];
        int delta;
        try {
            delta = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(lang.msg("invalid_number"));
            return;
        }

        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null || !target.isOnline()) {
            sender.sendMessage(lang.msg("invalid_player"));
            return;
        }
        if (target.getGameMode() == GameMode.SPECTATOR) {
            sender.sendMessage(lang.msg("hearts_spectator_mode"));
            return;
        }

        int current = service.getHearts(targetName);
        int after   = current - delta;

        if (after <= 0) {
            service.setHearts(targetName, 0);
            target.setHealth(0.0);
            sender.sendMessage(lang.msg("hearts_set",
                    "hearts", 0
            ));
        } else {
            service.removeHearts(targetName, delta);
            sender.sendMessage(lang.msg("hearts_set",
                    "hearts", after
            ));
        }
    }
}
