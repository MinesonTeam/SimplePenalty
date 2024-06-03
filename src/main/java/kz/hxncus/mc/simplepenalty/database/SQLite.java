package kz.hxncus.mc.simplepenalty.database;

import lombok.NonNull;
import org.bukkit.plugin.Plugin;
import org.jooq.SQLDialect;

public class SQLite extends AbstractDatabase {
    public SQLite(@NonNull Plugin plugin, @NonNull String directory, String tableSQL, @NonNull DatabaseSettings settings) {
        super(plugin, "jdbc:sqlite:plugins/" + directory + "/" + settings.database + ".db", tableSQL, settings);
    }

    public SQLite(@NonNull Plugin plugin, String tableSQL, @NonNull DatabaseSettings settings) {
        this(plugin, plugin.getDataFolder().getName(), tableSQL, settings);
    }

    public SQLite(@NonNull Plugin plugin, @NonNull DatabaseSettings settings) {
        this(plugin, null, settings);
    }

    @Override
    public SQLDialect getSQLDialect() {
        return SQLDialect.SQLITE;
    }
}
