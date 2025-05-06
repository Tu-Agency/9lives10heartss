package lapisteam.kurampa.liveshearts.storage.sqlite;

import lapisteam.kurampa.liveshearts.storage.PlayerRepository;
import org.bukkit.plugin.java.JavaPlugin;
import org.sqlite.SQLiteDataSource;

import java.io.File;
import java.sql.*;
import java.util.Optional;

public class SQLitePlayerRepository implements PlayerRepository {

    private final SQLiteDataSource ds = new SQLiteDataSource();

    public SQLitePlayerRepository(JavaPlugin plugin) {
        File db = new File(plugin.getDataFolder(), "data.db");
        ds.setUrl("jdbc:sqlite:" + db.getAbsolutePath());
        try (Connection c = ds.getConnection();
             Statement st = c.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS players (name TEXT PRIMARY KEY, hearts INTEGER)");
        } catch (SQLException e) {
            plugin.getLogger().severe("Cannot init database");
            e.printStackTrace();
        }
    }

    @Override
    public Optional<Integer> findHearts(String playerName) {
        String sql = "SELECT hearts FROM players WHERE name = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, playerName);
            ResultSet rs = p.executeQuery();
            return rs.next() ? Optional.of(rs.getInt("hearts")) : Optional.empty();
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public void saveHearts(String playerName, int hearts) {
        String sql = "INSERT INTO players(name, hearts) VALUES(?, ?) ON CONFLICT(name) DO UPDATE SET hearts = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, playerName);
            p.setInt(2, hearts);
            p.setInt(3, hearts);
            p.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
