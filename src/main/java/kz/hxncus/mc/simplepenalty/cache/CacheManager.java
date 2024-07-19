package kz.hxncus.mc.simplepenalty.cache;

import kz.hxncus.mc.simplepenalty.SimplePenalty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jooq.Record;
import org.jooq.Result;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@EqualsAndHashCode
public class CacheManager {
    private static SimplePenalty plugin;
    private final AtomicInteger maxId;
    private final Map<UUID, PlayerPenaltyCache> playerPenaltyCacheMap = new ConcurrentHashMap<>();

    public CacheManager(SimplePenalty plugin) {
        CacheManager.plugin = plugin;
        maxId = new AtomicInteger(plugin.getDatabase().getNewId(plugin.getDatabase().getSettings().getTablePrefix() + "players"));
        for (Player player : Bukkit.getOnlinePlayers()) {
            unloadPlayerFromDatabase(player);
        }

    }

    public boolean isPlayerHasPenaltyCache(UUID uuid) {
        return playerPenaltyCacheMap.containsKey(uuid);
    }

    public PlayerPenaltyCache getPlayerPenaltyCache(final UUID uuid) {
        return playerPenaltyCacheMap.computeIfAbsent(uuid, PlayerPenaltyCache::new);
    }

    public PlayerPenaltyCache getPlayerPenaltyCache(final Player player) {
        return getPlayerPenaltyCache(player.getUniqueId());
    }

    public PlayerPenaltyCache removePlayerPenaltyCache(final UUID uuid) {
        return playerPenaltyCacheMap.remove(uuid);
    }

    public PlayerPenaltyCache unloadPlayerFromDatabase(UUID uuid, String name) {
        PlayerPenaltyCache penaltyCache = getPlayerPenaltyCache(uuid);
        Result<Record> entries = plugin.getDatabase().fetch(new StringBuilder().append("SELECT * FROM ")
                                                                               .append(plugin.getDatabase().getSettings().getTablePrefix())
                                                                               .append("players WHERE offender = ?").toString(), name);
        for (Record entry : entries) {
            penaltyCache.getPenalties().add(new PenaltyCache(entry.get("id", Long.class), entry.get("officer", String.class),
                entry.get("offender", String.class), entry.get("description", String.class),
                entry.get("count", Integer.class), entry.get("time", Long.class)));
        }
        return penaltyCache;
    }

    public PlayerPenaltyCache unloadPlayerFromDatabase(Player player) {
        return unloadPlayerFromDatabase(player.getUniqueId(), player.getName());
    }

    public PlayerPenaltyCache unloadPlayerFromDatabase(OfflinePlayer offlinePlayer) {
        return unloadPlayerFromDatabase(offlinePlayer.getUniqueId(), offlinePlayer.getName());
    }

    public void loadPlayerIntoDatabase(UUID uuid) {
        for (PenaltyCache penalty : getPlayerPenaltyCache(uuid).getPenalties()) {
            if (penalty.getCount() > 0) {
                plugin.getDatabase().fetchOne(new StringBuilder().append("REPLACE INTO ")
                    .append(plugin.getDatabase().getSettings().getTablePrefix())
                    .append("players (id, officer, offender, count, description, time) VALUES (?, ?, ?, ?, ?, ?)")
                    .toString(), penalty.getId(), penalty.getOfficer(), penalty.getOffender(), penalty.getCount(), penalty.getDescription().length() > 256 ? penalty.getDescription().substring(0, 256) : penalty.getDescription(), penalty.getTime());
            } else {
                plugin.getDatabase().fetchOne(new StringBuilder().append("DELETE FROM ")
                                                                 .append(plugin.getDatabase().getSettings().getTablePrefix())
                                                                 .append("players WHERE id = ?")
                                                                 .toString(), penalty.getId());
            }
        }
        removePlayerPenaltyCache(uuid);
    }
}
