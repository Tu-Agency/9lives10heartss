package lapisteam.kurampa.liveshearts.command.impl;

import lapisteam.kurampa.liveshearts.command.BaseCommand;
import lapisteam.kurampa.liveshearts.config.Lang;
import lapisteam.kurampa.liveshearts.service.HeartService;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class GiftCommand implements BaseCommand {

    private final HeartService service;
    private final Lang lang;

    public GiftCommand(HeartService service, JavaPlugin plugin) {
        this.service = service;
        this.lang    = new Lang(plugin);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player giver)) {
            sender.sendMessage(lang.msg("gift_usage"));
            return;
        }
        if (args.length != 2) {
            giver.sendMessage(lang.msg("command_usage"));
            return;
        }

        Player recipient = Bukkit.getPlayerExact(args[1]);
        if (recipient == null || !recipient.isOnline()) {
            giver.sendMessage(lang.msg("invalid_player"));
            return;
        }
        if (giver.equals(recipient)) {
            giver.sendMessage(lang.msg("cannot_gift_self"));
            return;
        }
        if (giver.getGameMode() == GameMode.SPECTATOR ||
                recipient.getGameMode() == GameMode.SPECTATOR) {
            giver.sendMessage(lang.msg("hearts_spectator_mode"));
            return;
        }

        int giverHearts = service.getHearts(giver.getName());
        int recHearts   = service.getHearts(recipient.getName());
        int max         = service.getMaxHearts();

        if (giverHearts <= 1) {
            giver.sendMessage(lang.msg("hearts_not_enough"));
            return;
        }
        if (recHearts >= max) {
            giver.sendMessage(lang.msg("recipient_max_hearts",
                    "max", max
            ));
            return;
        }

        service.removeHearts(giver.getName(), 1);
        service.addHearts(recipient.getName(), 1);

        int afterGiver = giverHearts - 1;
        int afterRec   = recHearts + 1;

        giver.sendMessage(lang.msg("hearts_gifted",
                "player", recipient.getName(),
                "hearts", afterGiver
        ));
        recipient.sendMessage(lang.msg("hearts_received",
                "player", giver.getName(),
                "hearts", afterRec
        ));
    }
}
