package lapisteam.kurampa.liveshearts.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lapisteam.kurampa.liveshearts.config.Lang;
import lapisteam.kurampa.liveshearts.service.HeartService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

public final class ItemUtil {

    private ItemUtil() {}

    /**
     * Проверяет, что это наша «сердечная голова» по CustomModelData.
     */
    public static boolean isHeartHead(ItemStack item, JavaPlugin plugin) {
        if (item == null || item.getType() != Material.PLAYER_HEAD) return false;
        var meta = item.getItemMeta();
        return meta != null
                && meta.hasCustomModelData()
                && meta.getCustomModelData() == plugin.getConfig().getInt("head-heart.container");
    }

    /**
     * Создаёт «сердечную голову» с текстурой из конфига.
     */
    public static ItemStack createHeartHead(Player dead, Lang lang, JavaPlugin plugin) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta == null) return head;

        // Устанавливаем displayName и lore
        meta.setDisplayName(lang.msg("head-heart.name", dead.getName()));
        meta.setLore(List.of(
                lang.msg("head-heart.lore.0"),
                lang.msg("head-heart.lore.1")
        ));

        // Устанавливаем CustomModelData
        int cmd = plugin.getConfig().getInt("head-heart.container", 12345);
        meta.setCustomModelData(cmd);

        // Получаем Base64-текстуру
        String value = plugin.getConfig().getString("head-heart.value", "");
        if (!value.isBlank()) {
            try {
                // Создаём профиль с нужной текстурой
                GameProfile profile = new GameProfile(UUID.randomUUID(), null);
                profiltures", value));

                // Впрыскиваем в SkullMeta через reflection
                Field profileField = meta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(meta, profile);
            } catch (Exception ex) {
                plugin.getLogger().warning("Не удалось установить текстуру для head-heart: " + ex.getMessage());
            }
        }

        head.setItemMeta(meta);
        return head;
    }

    /**
     * Обрабатывает использование «сердечной головы» игроком:
     * — убирает один айтем
     * — даёт +1 сердце (и обновляет здоровье)
     */
    public static void handleHeartHead(Player player,
                                       ItemStack item,
                                       HeartService service,
                                       Lang lang) {
        int current = service.getHearts(player.getName());
        if (current >= HeartService.MAX_HEARTS) {
            player.sendMessage(lang.msg("error_hp_player"));
            return;
        }

        // забираем один айтем
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().remove(item);
        }

        // добавляем сердце
        service.addHearts(player.getName(), 1);
        int after = service.getHearts(player.getName());
        player.sendMessage(lang.msg("heart_recovered", after));
    }
}
