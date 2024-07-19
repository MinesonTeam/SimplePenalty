package kz.hxncus.mc.simplepenalty;

import kz.hxncus.mc.simplepenalty.cache.CacheManager;
import kz.hxncus.mc.simplepenalty.cache.PenaltyCache;
import kz.hxncus.mc.simplepenalty.cache.PlayerPenaltyCache;
import kz.hxncus.mc.simplepenalty.command.PenaltyCommand;
import kz.hxncus.mc.simplepenalty.database.*;
import kz.hxncus.mc.simplepenalty.listener.PlayerListener;
import kz.hxncus.mc.simplepenalty.manager.FileManager;
import kz.hxncus.mc.simplepenalty.util.Messages;
import kz.hxncus.mc.simplepenalty.util.Metrics;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class SimplePenalty extends JavaPlugin {
    @Getter
    private static SimplePenalty instance;
    private FileManager fileManager;
    private CacheManager cacheManager;
    private Database database;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        registerDatabase();
        registerManagers();
        registerCommands();
        registerListeners(Bukkit.getPluginManager());
        registerMetrics();
        registerTasks();
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            cacheManager.loadPlayerIntoDatabase(player.getUniqueId());
        }
        database.closeConnection();
        reloadConfig();
    }

    private void registerManagers() {
        this.fileManager = new FileManager(this);
        this.cacheManager = new CacheManager(this);
    }

    private void registerDatabase() {
        getDataFolder().mkdir();
        String tableSQL = "CREATE TABLE IF NOT EXISTS " +
                getConfig().getString("database.sql.table-prefix", "sp_") +
                "players (id BIGINT, officer VARCHAR(32), offender VARCHAR(32), count INT, description VARCHAR(256), time BIGINT, PRIMARY KEY (id))";
        DatabaseSettings settings = new DatabaseSettings(getConfig());
        switch(getConfig().getString("database.type", "SQLite")) {
            case "MariaDB":
                this.database = new MariaDB(this, tableSQL, settings);
                break;
            case "MySQL":
                this.database = new MySQL(this, tableSQL, settings);
                break;
            default:
                this.database = new SQLite(this, tableSQL, settings);
                break;
        }
    }

    private void registerCommands() {
        new PenaltyCommand(this);
    }

    private void registerListeners(PluginManager pluginManager) {
        pluginManager.registerEvents(new PlayerListener(this), this);
    }

    private void registerMetrics() {
        Metrics metrics = new Metrics(this, 22104);
        metrics.addCustomChart(new Metrics.SimplePie("used_language", () -> this.fileManager.getLang()));
    }

    private void registerTasks() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerPenaltyCache penaltyCache = cacheManager.getPlayerPenaltyCache(player);
                for (PenaltyCache penalty : penaltyCache.getPenalties()) {
                    if (System.currentTimeMillis() >= penalty.getTime()) {
                        penalty.setCount(Math.min(getConfig().getInt("penalty-max-count", 10000), (int) (penalty.getCount() * getConfig().getDouble("penalty-expire-multiplayer", 2.0))));
                        penalty.setTime(getConfig().getInt("penalty-expire-time", 604800) * 1000L + System.currentTimeMillis());
                        player.sendTitle(Messages.PENALTY_EXPIRED_TITLE.getMessage(0), Messages.PENALTY_EXPIRED_TITLE.getMessage(1),
                                getConfig().getInt("penalty-title-fadeIn", 10), getConfig().getInt("penalty-title-stay", 70),
                                getConfig().getInt("penalty-title-fadeOut", 20));
                    }
                }
            }
        }, 20L, 20L);
    }
}
