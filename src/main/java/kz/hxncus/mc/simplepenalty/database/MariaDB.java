package kz.hxncus.mc.simplepenalty.database;

import lombok.NonNull;
import org.bukkit.plugin.Plugin;
import org.jooq.SQLDialect;

public class MariaDB extends AbstractDatabase {
    public MariaDB(@NonNull Plugin plugin, String tableSQL, @NonNull DatabaseSettings settings) {
        super(plugin, "jdbc:mariadb://" + settings.host + ":" + settings.port + "/" + settings.database, tableSQL, settings);
    }

    public MariaDB(@NonNull Plugin plugin, @NonNull DatabaseSettings settings) {
        this(plugin, null, settings);
    }

    @Override
    public SQLDialect getSQLDialect() {
        return SQLDialect.MARIADB;
    }
}
