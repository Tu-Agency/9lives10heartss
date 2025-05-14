package lapisteam.kurampa.liveshearts.util;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import lapisteam.kurampa.liveshearts.config.Lang;
import lapisteam.kurampa.liveshearts.service.HeartService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

public final class ItemUtil {

    private ItemUtil() {}

    public static boolean isHeartHead(ItemStack item, JavaPlugin plugin) {
        if (item == null || item.getType() != Material.PLAYER_HEAD) return false;
        var meta = item.getItemMeta();
        return meta != null
                && meta.hasCustomModelData()
                && meta.getCustomModelData()
                == plugin.getConfig().getInt("head-heart.container", 12345);
    }

    public static boolean isCursedHead(ItemStack item, JavaPlugin plugin) {
        if (item == null || item.getType() != Material.PLAYER_HEAD) return false;
        var meta = item.getItemMeta();
        return meta != null
                && meta.hasCustomModelData()
                && meta.getCustomModelData()
                == plugin.getConfig().getInt("head-heart.cursed.container", 12346);
    }

    public static ItemStack createHeartHead(Player dead, JavaPlugin plugin) {
        return createHead(dead, plugin, "head-heart");
    }

    public static ItemStack createCursedHead(Player dead, JavaPlugin plugin) {
        return createHead(dead, plugin, "head-heart.cursed");
    }

    private static ItemStack createHead(Player dead,
                                        JavaPlugin plugin,
                                        String path) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta == null) return head;

        String rawName = plugin.getConfig().getString(path + ".name", "{player}");
        int amount    = plugin.getConfig().getInt(path + ".amount-heart", 1);
        String display = rawName
                .replace("{player}", dead.getName())
                .replace("{amount}", String.valueOf(amount));
        meta.setDisplayName(ColorUtil.translateHex(display));

        List<String> rawLore = plugin.getConfig().getStringList(path + ".lore");
        meta.setLore(rawLore.stream()
                .map(line -> ColorUtil.translateHex(
                        line.replace("{player}", dead.getName())
                                .replace("{amount}", String.valueOf(amount))
                ))
                .toList()
        );

        int cmd = plugin.getConfig().getInt(path + ".container", 12345);
        meta.setCustomModelData(cmd);

        String value = plugin.getConfig().getString(path + ".value", "").trim();
        if (!value.isEmpty()) {
            try {
                Object server = Bukkit.getServer();
                Method createProfile = server.getClass()
                        .getMethod("createProfile", UUID.class);
                Object profObj = createProfile.invoke(server, UUID.randomUUID());
                PlayerProfile profile = (PlayerProfile) profObj;
                profile.getProperties().add(new ProfileProperty("textures", value));
                meta.setPlayerProfile(profile);
            } catch (ReflectiveOperationException ex) {
                plugin.getLogger().warning("Не удалось установить текстуру головы: " + ex.getMessage());
            }
        }

        head.setItemMeta(meta);
        return head;
    }

    public static void handleHeartHead(Player player,
                                       ItemStack item,
                                       HeartService service,
                                       Lang lang) {
        int current = service.getHearts(player.getName());
        int max     = service.getMaxHearts();

        if (current >= max) {
            player.sendMessage(lang.msg("error_max_hearts", "max", max));
            return;
        }

        if (item.getAmount() > 1) item.setAmount(item.getAmount() - 1);
        else player.getInventory().remove(item);

        service.addHearts(player.getName(), 1);
        int after = service.getHearts(player.getName());
        player.sendMessage(lang.msg("heart_recovered", "hearts", after));
    }

    public static void handleCursedHead(Player player,
                                        ItemStack item,
                                        HeartService service,
                                        Lang lang,
                                        JavaPlugin plugin) {
        int amount = plugin.getConfig().getInt("head-heart.cursed.amount-heart", 1);

        if (item.getAmount() > 1) item.setAmount(item.getAmount() - 1);
        else player.getInventory().remove(item);

        service.removeHearts(player.getName(), amount);

        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5 * 20, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 5 * 20, 0));

        player.sendMessage(lang.msg("cursed_used", "amount", amount));
    }

    public static boolean isUniqueTotem(ItemStack item, JavaPlugin plugin) {
        if (item == null || item.getType() != Material.TOTEM_OF_UNDYING) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        String name = plugin.getConfig().getString("heart-recovery.totem.name", "");
        int cmd     = plugin.getConfig().getInt("heart-recovery.totem.container", 12345);
        if (meta.hasDisplayName() && meta.getDisplayName().equals(name)) return true;
        return meta.hasCustomModelData() && meta.getCustomModelData() == cmd;
    }

    public static void handleTotem(Player player,
                                   HeartService service,
                                   Lang lang) {
        int current = service.getHearts(player.getName());
        int max     = service.getMaxHearts();
        if (current >= max) {
            player.sendMessage(lang.msg("error_max_hearts", "max", max));
            return;
        }
        service.addHearts(player.getName(), 1);
        int after = service.getHearts(player.getName());
        player.sendMessage(lang.msg("heart_recovered_thematic", "hearts", after));
    }

    public static ItemStack createTotem(JavaPlugin plugin) {
        ItemStack totem = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta meta = totem.getItemMeta();
        if (meta != null) {
            String name = plugin.getConfig().getString("heart-recovery.totem.name", "");
            int cmd     = plugin.getConfig().getInt("heart-recovery.totem.container", 12345);
            meta.setDisplayName(name);
            meta.setLore(plugin.getConfig().getStringList("heart-recovery.totem.lore"));
            meta.setCustomModelData(cmd);
            totem.setItemMeta(meta);
        }
        return totem;
    }
}
