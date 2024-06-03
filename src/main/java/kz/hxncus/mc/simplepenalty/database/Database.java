package kz.hxncus.mc.simplepenalty.database;

import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jooq.QueryPart;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;

public interface Database {
    void createConnection();
    DatabaseSettings getSettings();
    SQLDialect getSQLDialect();
    void closeConnection();
    @NonNull
    Result<Record> fetch(@NonNull String sql);
    @NonNull Result<Record> fetch(@NonNull String sql, Object @NonNull ... bindings);
    @NonNull Result<Record> fetch(@NonNull String sql, QueryPart @NonNull ... parts);
    @Nullable
    Record fetchOne(@NonNull String sql);
    @Nullable Record fetchOne(@NonNull String sql, Object @NonNull ... bindings);
    @Nullable Record fetchOne(@NonNull String sql, QueryPart @NonNull ... parts);
    int execute(@NonNull String sql);
    int execute(@NonNull String sql, Object @NonNull ... bindings);
    int execute(@NonNull String sql, QueryPart @NonNull ... parts);
    int getNewId(@NonNull String table);
    void reload();
}
