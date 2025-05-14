package lapisteam.kurampa.liveshearts.storage.sqlite;

import lapisteam.kurampa.liveshearts.storage.PlayerRepository;
import org.bukkit.plugin.java.JavaPlugin;
import org.sqlite.SQLiteDataSource;

import java.io.File;
import java.sql.*;
import java.util.Optional;
import java.util.UUID;

public class SQLitePlayerRepository implements PlayerRepository {

    private final SQLiteDataSource ds = new SQLiteDataSource();

    public SQLitePlayerRepository(JavaPlugin plugin) {
        File dbFile = new File(plugin.getDataFolder(), "data.db");
        ds.setUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        try (Connection conn = ds.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS players (
                  uuid   TEXT PRIMARY KEY,
                  hearts INTEGER NOT NULL
                )
                """);
        } catch (SQLException ex) {
            plugin.getLogger().severe("Не удалось инициализировать базу данных: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public Optional<Integer> findHearts(UUID playerId) {
        String sql = "SELECT hearts FROM players WHERE uuid = ?";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, playerId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getInt("hearts"));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public void saveHearts(UUID playerId, int hearts) {
        String sql = """
            INSERT INTO players (uuid, hearts) 
            VALUES (?, ?)
            ON CONFLICT(uuid) DO UPDATE SET hearts = excluded.hearts
            """;
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, playerId.toString());
            ps.setInt(2, hearts);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
