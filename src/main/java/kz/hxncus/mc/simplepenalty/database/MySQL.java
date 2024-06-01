package kz.hxncus.mc.simplepenalty.database;

import lombok.NonNull;
import org.bukkit.plugin.Plugin;

import java.util.Map;

public class MySQL extends AbstractDatabase {
    public MySQL(@NonNull Plugin plugin, @NonNull String host, @NonNull String port, @NonNull String database, @NonNull String username, @NonNull String password) {
        super(plugin, "jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
    }

    public MySQL(@NonNull Plugin plugin, @NonNull String host, @NonNull String port, @NonNull String database, @NonNull String username, @NonNull String password, Map<String, String> properties, String tableSQL) {
        super(plugin, "jdbc:mysql://" + host + ":" + port + "/" + database, username, password, properties, tableSQL);
    }

    @Override
    public Database.Type getType() {
        return Database.Type.MYSQL;
    }
}
