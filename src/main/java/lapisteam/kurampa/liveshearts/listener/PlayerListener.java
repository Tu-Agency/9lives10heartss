package lapisteam.kurampa.liveshearts.listener;

import lapisteam.kurampa.liveshearts.service.HeartService;
import lapisteam.kurampa.liveshearts.config.ConfigKeys;
import lapisteam.kurampa.liveshearts.config.Lang;
import lapisteam.kurampa.liveshearts.util.ItemUtil;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerListener implements Listener {

    private final HeartService service;
    private final JavaPlugin plugin;
    private final Lang lang;

    public PlayerListener(HeartService service, JavaPlugin plugin) {
        this.service = service;
        this.plugin  = plugin;
        this.lang    = new Lang(plugin);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        service.handleDeath(p);

        if (plugin.getConfig().getBoolean(ConfigKeys.HEAD_HEART + ".enabled", true)) {
            e.getDrops().add(ItemUtil.createHeartHead(p, plugin));
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        int hearts = service.getHearts(p.getName());

        if (hearts <= 0) {
            p.setGameMode(GameMode.SPECTATOR);
            return;
        }

        double hp = hearts * 2.0;
        p.setMaxHealth(hp);
        p.setHealth(hp);
    }



    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        // получаем кол-во сердец (1 сердце = 2 hp)
        int hearts = service.getHearts(p.getName());
        double healthPoints = hearts * 2.0;

        // выставляем максимальное здоровье
        p.setMaxHealth(healthPoints);
        // и обновляем текущее здоровье на полное
        p.setHealth(healthPoints);
    }

    @EventHandler
    public void onEat(PlayerItemConsumeEvent e) {
        if (!plugin.getConfig().getBoolean(ConfigKeys.RECOVERY_EAT + ".enabled", true)) return;

        Material food = Material.matchMaterial(
                plugin.getConfig().getString(ConfigKeys.RECOVERY_EAT + ".food", "ENCHANTED_GOLDEN_APPLE"));
        if (food == null || e.getItem().getType() != food) return;

        Player p = e.getPlayer();
        if (p.getGameMode() == GameMode.SPECTATOR) {
            p.sendMessage(lang.msg("hearts_spectator_mode"));
            return;
        }

        service.addHearts(p.getName(), 1);
        p.sendMessage(lang.msg("heart_recovered", service.getHearts(p.getName())));
    }

    @EventHandler
    public void onResurrect(EntityResurrectEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;
        if (!plugin.getConfig().getBoolean(ConfigKeys.RECOVERY_TOTEM + ".enabled", true)) return;

        if (ItemUtil.isUniqueTotem(player.getInventory().getItemInMainHand(), plugin)
                || ItemUtil.isUniqueTotem(player.getInventory().getItemInOffHand(), plugin)) {
            ItemUtil.handleTotem(player, service, lang);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
        if (ItemUtil.isUniqueHead(item, plugin)) {
            ItemUtil.handleHeartHead(e.getPlayer(), item, service, lang);
            e.setCancelled(true);
        }
    }
}
