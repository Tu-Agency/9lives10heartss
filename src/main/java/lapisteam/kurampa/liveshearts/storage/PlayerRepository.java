package lapisteam.kurampa.liveshearts.storage;

import java.util.Optional;

public interface PlayerRepository {
    Optional<Integer> findHearts(String playerName);    // empty → первый вход, даём дефолт 10
    void saveHearts(String playerName, int hearts);
}
