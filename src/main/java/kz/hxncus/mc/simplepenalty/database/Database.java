package kz.hxncus.mc.simplepenalty.database;

import com.zaxxer.hikari.HikariDataSource;
import kz.hxncus.mc.simplepenalty.util.StringUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.bukkit.plugin.Plugin;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.io.File;

public class Database implements IDatabase {
    protected String url;
    protected String tableSQL;
    @Getter
    protected DatabaseSettings settings;
    protected HikariDataSource dataSource;
    protected DSLContext dslContext;

    public Database(@NonNull Plugin plugin, String tableSQL, @NonNull DatabaseSettings settings) {
        this.tableSQL = tableSQL;
        this.settings = settings;
        if (settings.sqlDialect == SQLDialect.SQLITE) {
            this.url = "jdbc:sqlite:" + plugin.getDataFolder().getName() + File.separator + settings.database + ".db";
        } else {
            this.url = "jdbc:" + settings.sqlDialect.getNameLC() + "://" + settings.host + ":" + settings.port + "/" + settings.database;
        }
        createConnection();
    }

    @Override
    public void createConnection() {
        this.dataSource = new HikariDataSource();
        this.dataSource.setJdbcUrl(url);
        this.dataSource.setUsername(settings.username);
        this.dataSource.setPassword(settings.password);
        if (settings.properties != null) {
            settings.properties.forEach(this.dataSource::addDataSourceProperty);
        }
        this.dslContext = DSL.using(this.dataSource, settings.getSqlDialect());
        if (StringUtil.isNotEmpty(tableSQL)) {
            execute(tableSQL);
        }
    }

    @Override
    public int getNewId(@NonNull String table) {
        Record entry = fetchOne("SELECT MAX(id) as maxId FROM " + table);
        if (entry == null) {
            return 1;
        }
        Object obj = entry.get("maxId");
        return obj == null ? 1 : (int) obj + 1;
    }

    @SneakyThrows
    @Override
    public int execute(@NonNull String sql) {
        return dslContext.execute(sql);
    }

    @SneakyThrows
    @Override
    public @NonNull Result<Record> fetch(@NonNull String sql) {
        return dslContext.fetch(sql);
    }

    @SneakyThrows
    @Override
    public @NonNull Result<Record> fetch(@NonNull String sql, Object @NonNull ... bindings) {
        return dslContext.fetch(sql, bindings);
    }

    @SneakyThrows
    @Override
    public @NonNull Result<Record> fetch(@NonNull String sql, QueryPart @NonNull ... parts) {
        return dslContext.fetch(sql, parts);
    }

    @SneakyThrows
    @Override
    public Record fetchOne(@NonNull String sql) {
        return dslContext.fetchOne(sql);
    }

    @SneakyThrows
    @Override
    public Record fetchOne(@NonNull String sql, Object @NonNull ... bindings) {
        return dslContext.fetchOne(sql, bindings);
    }

    @SneakyThrows
    @Override
    public Record fetchOne(@NonNull String sql, QueryPart @NonNull ... parts) {
        return dslContext.fetchOne(sql, parts);
    }

    @SneakyThrows
    @Override
    public int execute(@NonNull String sql, Object @NonNull ... bindings) {
        return dslContext.execute(sql, bindings);
    }

    @SneakyThrows
    @Override
    public int execute(@NonNull String sql, QueryPart @NonNull ... parts) {
        return dslContext.execute(sql, parts);
    }

    @Override
    public void closeConnection() {
        if (this.dataSource != null) {
            this.dataSource.close();
        }
    }

    @Override
    public void reload() {
        closeConnection();
        createConnection();
    }
}
