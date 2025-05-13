package lapisteam.kurampa.liveshearts.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lapisteam.kurampa.liveshearts.service.HeartService;
import lapisteam.kurampa.liveshearts.config.Lang;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


public final class ItemUtil {

    private ItemUtil() {}

    public static boolean isUniqueHead(ItemStack item, JavaPlugin plugin) {
        return isHeartHead(item, plugin);
    }

    public static void handleHeartHead(Player player,
                                       ItemStack item,
                                       HeartService service,
                                       Lang lang) {
        handleHeartHeadInternal(player, item, service, lang);
    }

    public static boolean isHeartHead(ItemStack item, JavaPlugin plugin) {
        if (item == null || item.getType() != Material.PLAYER_HEAD) return false;
        var meta = item.getItemMeta();
        return meta != null
                && meta.hasCustomModelData()
                && meta.getCustomModelData() == plugin.getConfig().getInt("head-heart.container");
    }

    private static void handleHeartHeadInternal(Player player,
                                                ItemStack item,
                                                HeartService service,
                                                Lang lang) {
        int current = service.getHearts(player.getName());
        if (current >= HeartService.MAX_HEARTS) {
            player.sendMessage(lang.msg("error_hp_player")); return;
        }

        if (item.getAmount() > 1) item.setAmount(item.getAmount() - 1);
        else player.getInventory().remove(item);

        service.addHearts(player.getName(), 1);
        int after = service.getHearts(player.getName());
        player.sendMessage(lang.msg("heart_recovered", after));
    }

    public static boolean isUniqueTotem(ItemStack item, JavaPlugin plugin) {
        if (item == null || item.getType() != Material.TOTEM_OF_UNDYING) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        String name = plugin.getConfig().getString("heart-recovery.totem.name", "");
        int cmd    = plugin.getConfig().getInt   ("heart-recovery.totem.container", 12345);

        if (meta.hasDisplayName() && meta.getDisplayName().equals(name)) return true;
        return meta.hasCustomModelData() && meta.getCustomModelData() == cmd;
    }

    public static void handleTotem(Player player,
                                   HeartService service,
                                   Lang lang) {
        int current = service.getHearts(player.getName());
        if (current >= HeartService.MAX_HEARTS) {
            player.sendMessage(lang.msg("error_hp_player")); return;
        }
        service.addHearts(player.getName(), 1);
        int after = service.getHearts(player.getName());
        player.sendMessage(lang.msg("heart_recovered_thematic", after));
    }

    public static ItemStack createHeartHead(Player dead, JavaPlugin plugin) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta == null) return head;

        // 1) Имя и лор из config.yml
        String rawName = plugin.getConfig().getString("head-heart.name", "{player}");
        meta.setDisplayName(ColorUtil.translateHex(rawName.replace("{player}", dead.getName())));

        List<String> rawLore = plugin.getConfig().getStringList("head-heart.lore");
        meta.setLore(rawLore.stream()
                .map(line -> ColorUtil.translateHex(line.replace("{player}", dead.getName())))
                .toList()
        );

        meta.setCustomModelData(plugin.getConfig().getInt("head-heart.container", 12345));

        // 2) Base64-текстура
        String value = plugin.getConfig().getString("head-heart.value", "").trim();
        if (!value.isEmpty()) {
            try {
                // создаём GameProfile с нужной текстурой
                GameProfile gp = new GameProfile(UUID.randomUUID(), dead.getName());
                gp.getProperties().put("textures", new Property("textures", value));

                // получаем класс ResolvableProfile (Paper/NMS)
                Class<?> rpClass = Class.forName("net.minecraft.world.item.component.ResolvableProfile");
                // метод of(GameProfile)
                Method of = rpClass.getMethod("of", GameProfile.class);
                Object resolvable = of.invoke(null, gp);

                // впихиваем в приватное поле profile у CraftMetaSkull
                Field profileField = meta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(meta, resolvable);

            } catch (Throwable ex) {
                plugin.getLogger().warning("Не удалось установить текстуру головы: " + ex.getMessage());
            }
        }

        head.setItemMeta(meta);
        return head;
    }


    public static ItemStack createTotem(JavaPlugin plugin) {
        ItemStack totem = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta meta = totem.getItemMeta();
        if (meta != null) {
            String name = plugin.getConfig().getString("heart-recovery.totem.name", "");
            int cmd     = plugin.getConfig().getInt   ("heart-recovery.totem.container", 12345);
            meta.setDisplayName(name);

            List<String> lore = new ArrayList<>();
            plugin.getConfig()
                    .getStringList("heart-recovery.totem.lore")
                    .forEach(line -> lore.add(line));
            meta.setLore(lore);

            meta.setCustomModelData(cmd);
            totem.setItemMeta(meta);
        }
        return totem;
    }
}
