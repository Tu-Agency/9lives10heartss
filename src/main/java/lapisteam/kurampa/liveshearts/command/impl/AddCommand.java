package lapisteam.kurampa.liveshearts.command.impl;

import lapisteam.kurampa.liveshearts.command.BaseCommand;
import lapisteam.kurampa.liveshearts.config.Lang;
import lapisteam.kurampa.liveshearts.service.HeartService;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public final class AddCommand implements BaseCommand {

    private final HeartService service;
    private final Lang lang;

    public AddCommand(HeartService service, JavaPlugin plugin) {
        this.service = service;
        this.lang    = new Lang(plugin);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("9l.add")) {
            sender.sendMessage(lang.msg("no_permission"));
            return;
        }
        if (args.length != 3) {
            sender.sendMessage(lang.msg("command_usage"));
            return;
        }

        OfflinePlayer op = Bukkit.getOfflinePlayer(args[1]);
        if (!op.isOnline() && !op.hasPlayedBefore()) {
            sender.sendMessage(lang.msg("invalid_player"));
            return;
        }
        Player target = op.getPlayer();
        UUID id = op.getUniqueId();

        int delta;
        try {
            delta = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(lang.msg("invalid_number"));
            return;
        }

        int current = service.getHearts(id);
        int max     = service.getMaxHearts();
        int after   = current + delta;

        if (after > max) {
            sender.sendMessage(lang.msg("error_invalid_number", "max", max));
            return;
        }

        service.addHearts(id, delta);
        sender.sendMessage(lang.msg("hearts_set", "hearts", after));
    }
}
