package kz.hxncus.mc.simplepenalty.database;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jooq.SQLDialect;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatabaseSettings {
    protected String host;
    protected String port;
    protected String database;
    protected String username;
    protected String password;
    protected String tablePrefix;
    protected SQLDialect sqlDialect;
    protected Map<String, String> properties;
}
