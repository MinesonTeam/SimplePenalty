package kz.hxncus.mc.simplepenalty;

import kz.hxncus.mc.simplepenalty.command.FDPenaltyCommand;
import kz.hxncus.mc.simplepenalty.database.Database;
import kz.hxncus.mc.simplepenalty.database.MariaDB;
import kz.hxncus.mc.simplepenalty.database.MySQL;
import kz.hxncus.mc.simplepenalty.database.SQLite;
import kz.hxncus.mc.simplepenalty.manager.FileManager;
import kz.hxncus.mc.simplepenalty.manager.LangManager;
import kz.hxncus.mc.simplepenalty.util.Metrics;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class SimplePenalty extends JavaPlugin {
    @Getter
    private static SimplePenalty instance;
    private FileManager fileManager;
    private LangManager langManager;
    private Database database;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        registerStaff();
    }

    public void registerStaff() {
        registerManagers(this);
        registerCommands(this);
        registerDatabase(this);
        registerMetrics(this);
    }

    private void registerManagers(SimplePenalty plugin) {
        this.fileManager = new FileManager(plugin);
        this.langManager = new LangManager(plugin);
    }

    private void registerCommands(SimplePenalty plugin) {
        new FDPenaltyCommand(plugin);
    }

    private void registerDatabase(SimplePenalty plugin) {
        FileConfiguration config = plugin.getConfig();
        String host = config.getString("database.sql.host", "localhost");
        String port = config.getString("database.sql.port", "3306");
        String databaseName = config.getString("database.sql.database", "fdp");
        String username = config.getString("database.sql.username", "root");
        String password = config.getString("database.sql.password", "");
        switch(config.getString("database.type", "SQLite")) {
            case "MariaDB":
                this.database = new MariaDB(plugin, host, port, databaseName, username, password);
                break;
            case "MySQL":
                this.database = new MySQL(plugin, host, port, databaseName, username, password);
                break;
            default:
                this.database = new SQLite(plugin, databaseName);
                break;
        }

    }

    private void registerMetrics(SimplePenalty plugin) {
        Metrics metrics = new Metrics(plugin, 22104);
        metrics.addCustomChart(new Metrics.SimplePie("used_language", () -> this.langManager.getLang()));
    }
}
