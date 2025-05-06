package lapisteam.kurampa.liveshearts.command.impl;

import lapisteam.kurampa.liveshearts.command.BaseCommand;
import lapisteam.kurampa.liveshearts.config.Lang;
import lapisteam.kurampa.liveshearts.service.HeartService;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class ResurrectCommand implements BaseCommand {

    private final HeartService service;
    private final Lang lang;

    public ResurrectCommand(HeartService service, JavaPlugin plugin) {
        this.service = service;
        this.lang    = new Lang(plugin);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("9l.resurrect")) {
            sender.sendMessage(lang.msg("no_permission"));  return;
        }
        if (args.length != 3) {
            sender.sendMessage(lang.msg("command_usage"));  return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null || !target.isOnline()) {
            sender.sendMessage(lang.msg("invalid_player")); return;
        }
        if (target.getGameMode() != GameMode.SPECTATOR) {
            sender.sendMessage(lang.msg("not_in_spectator")); return;
        }

        int hearts;
        try { hearts = Integer.parseInt(args[2]); }
        catch (NumberFormatException e) {
            sender.sendMessage(lang.msg("invalid_number")); return;
        }
        if (hearts < 1 || hearts > HeartService.MAX_HEARTS) {
            sender.sendMessage(lang.msg("invalid_number")); return;
        }

        service.setHearts(target.getName(), hearts);
        target.setGameMode(GameMode.SURVIVAL);

        sender.sendMessage(lang.msg("resurrected", target.getName(), hearts));
        target.sendMessage(lang.msg("you_have_been_resurrected", hearts));
    }
}
