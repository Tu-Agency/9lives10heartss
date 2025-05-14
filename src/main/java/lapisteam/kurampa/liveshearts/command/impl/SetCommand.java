package lapisteam.kurampa.liveshearts.command.impl;

import lapisteam.kurampa.liveshearts.command.BaseCommand;
import lapisteam.kurampa.liveshearts.config.Lang;
import lapisteam.kurampa.liveshearts.service.HeartService;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class SetCommand implements BaseCommand {

    private final HeartService service;
    private final Lang lang;

    public SetCommand(HeartService service, JavaPlugin plugin) {
        this.service = service;
        this.lang    = new Lang(plugin);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("9l.set")) {
            sender.sendMessage(lang.msg("no_permission"));
            return;
        }
        if (args.length != 3) {
            sender.sendMessage(lang.msg("command_usage"));
            return;
        }

        String playerName = args[1];
        int hearts;
        try {
            hearts = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(lang.msg("invalid_number"));
            return;
        }

        int max = service.getMaxHearts();
        if (hearts < 0 || hearts > max) {
            sender.sendMessage(lang.msg("invalid_number",
                    "max", max
            ));
            return;
        }

        service.setHearts(playerName, hearts);
        sender.sendMessage(lang.msg("hearts_set",
                "hearts", hearts
        ));
    }
}
