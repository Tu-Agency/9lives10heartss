package lapisteam.kurampa.liveshearts.util;

import lapisteam.kurampa.liveshearts.config.Lang;
import lapisteam.kurampa.liveshearts.service.HeartService;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class ItemUtil {

    private ItemUtil() {}

    /* ======= TOTEM ======= */

    public static boolean isUniqueTotem(ItemStack item, JavaPlugin plugin) {
        if (item == null || item.getType() != Material.TOTEM_OF_UNDYING) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        String name = ColorUtil.translateHex(
                plugin.getConfig().getString("heart-recovery.totem.name", "Unique Totem"));
        int cmd = plugin.getConfig().getInt("heart-recovery.totem.container", 12345);

        return (meta.hasDisplayName() && meta.getDisplayName().equals(name))
                || (meta.hasCustomModelData() && meta.getCustomModelData() == cmd);
    }

    public static void handleTotem(Player player, HeartService service, Lang lang) {
        int newHearts = service.getHearts(player.getName()) + 1;
        if (newHearts > HeartService.MAX_HEARTS) {
            player.sendMessage(lang.msg("error_hp_player"));
            return;
        }
        service.setHearts(player.getName(), newHearts);
        player.sendMessage(lang.msg("heart_recovered_thematic", newHearts));
    }

    /* ======= PLAYER HEAD ======= */

    public static boolean isUniqueHead(ItemStack item, JavaPlugin plugin) {
        if (item == null || item.getType() != Material.PLAYER_HEAD) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasCustomModelData()
                && meta.getCustomModelData() == plugin.getConfig().getInt("head-heart.container", 12345);
    }

    public static void handleHeartHead(Player player,
                                       ItemStack item,
                                       HeartService service,
                                       Lang lang) {

        int cur = service.getHearts(player.getName());
        if (cur >= HeartService.MAX_HEARTS) {
            player.sendMessage(lang.msg("error_hp_player"));
            return;
        }

        // забираем голову
        if (item.getAmount() > 1) item.setAmount(item.getAmount() - 1);
        else player.getInventory().setItemInMainHand(null);

        service.setHearts(player.getName(), cur + 1);
        player.sendMessage(lang.msg("heart_recovered", cur + 1));
    }

    public static ItemStack createHeartHead(Player dead, Lang lang, JavaPlugin plugin) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta == null) return head;

        meta.setOwningPlayer(dead);
        meta.setDisplayName(lang.msg("head-heart.name", dead.getName()));
        // по желанию можем задать lore
        meta.setLore(List.of(ChatColor.DARK_RED + "‣ " + dead.getName()));
        meta.setCustomModelData(plugin.getConfig().getInt("head-heart.container", 12345));
        head.setItemMeta(meta);
        return head;
    }
}
