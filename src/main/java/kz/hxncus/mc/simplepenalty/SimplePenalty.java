package kz.hxncus.mc.simplepenalty;

import kz.hxncus.mc.simplepenalty.command.SimplePenaltyCommand;
import kz.hxncus.mc.simplepenalty.database.*;
import kz.hxncus.mc.simplepenalty.listener.PlayerListener;
import kz.hxncus.mc.simplepenalty.manager.CacheManager;
import kz.hxncus.mc.simplepenalty.manager.FileManager;
import kz.hxncus.mc.simplepenalty.manager.LangManager;
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
    private LangManager langManager;
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
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            cacheManager.loadPlayerIntoDatabase(player.getUniqueId());
        }
        database.closeConnection();
    }

    private void registerManagers() {
        this.fileManager = new FileManager(this);
        this.langManager = new LangManager(this);
        this.cacheManager = new CacheManager(this);
    }

    private void registerDatabase() {
        getDataFolder().mkdir();
        String tableSQL = new StringBuilder().append("CREATE TABLE IF NOT EXISTS ")
            .append(getConfig().getString("database.sql.table-prefix", "sp_"))
            .append("players (id BIGINT, officer VARCHAR(32), offender VARCHAR(32), count INT, description VARCHAR(256), time BIGINT, PRIMARY KEY (id))")
            .toString();
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
        new SimplePenaltyCommand(this);
    }

    private void registerListeners(PluginManager pluginManager) {
        pluginManager.registerEvents(new PlayerListener(this), this);
    }

    private void registerMetrics() {
        Metrics metrics = new Metrics(this, 22104);
        metrics.addCustomChart(new Metrics.SimplePie("used_language", () -> this.langManager.getLang()));
    }
}
