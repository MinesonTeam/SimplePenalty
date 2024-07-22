package kz.hxncus.mc.simplepenalty.database;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import org.bukkit.configuration.file.FileConfiguration;
import org.jooq.SQLDialect;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Setter
@AllArgsConstructor
public class DatabaseSettings {
    protected final String host;
    protected final String port;
    protected final String database;
    protected final String username;
    protected final String password;
    protected final String tablePrefix;
    protected final SQLDialect sqlDialect;
    protected final Map<String, String> properties;

    public DatabaseSettings(FileConfiguration config) {
        this.host = config.getString("database.sql.host", "localhost");
        this.port = config.getString("database.sql.port", "3306");
        this.database = config.getString("database.sql.database", "simplepenalty");
        this.username = config.getString("database.sql.username", "root");
        this.password = config.getString("database.sql.password", "");
        this.tablePrefix = config.getString("database.sql.table-prefix", "simplepenalty_");
        this.sqlDialect = SQLDialect.valueOf(config.getString("database.type", "SQLITE").toUpperCase(Locale.ENGLISH));
        this.properties = config.getStringList("database.sql.properties").stream().collect(Collectors.toMap(str -> str, str -> config.getString("database.sql.properties." + str, "")));
    }
}
