package kz.hxncus.mc.simplepenalty.database;

import org.bukkit.configuration.ConfigurationSection;
import org.jooq.SQLDialect;

import java.util.Locale;
import java.util.stream.Collectors;

public class ConfigAdapter extends DatabaseSettings {
    public ConfigAdapter(ConfigurationSection section) {
        this.host = section.getString("database.sql.host", "localhost");
        this.port = section.getString("database.sql.port", "3306");
        this.database = section.getString("database.sql.database", "simplepenalty");
        this.username = section.getString("database.sql.username", "root");
        this.password = section.getString("database.sql.password", "");
        this.tablePrefix = section.getString("database.sql.table-prefix", "simplepenalty_");
        this.sqlDialect = SQLDialect.valueOf(section.getString("database.type", "SQLITE").toUpperCase(Locale.ENGLISH));
        this.properties = section.getStringList("database.sql.properties").stream().collect(Collectors.toMap(str -> str, str -> section.getString("database.sql.properties." + str, "")));
    }
}
