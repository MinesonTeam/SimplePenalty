package kz.hxncus.mc.simplepenalty.manager;

import kz.hxncus.mc.simplepenalty.SimplePenalty;
import kz.hxncus.mc.simplepenalty.util.Constants;
import kz.hxncus.mc.simplepenalty.util.StringUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

@Getter
@EqualsAndHashCode
public class LangManager {
    private static SimplePenalty plugin;
    private final FileConfiguration langConfig;
    private String lang;

    public LangManager(SimplePenalty plugin) {
        LangManager.plugin = plugin;
        lang = plugin.getConfig().getString("lang");
        if (StringUtil.isEmpty(lang) || !Constants.SUPPORTED_LANGUAGES.contains("translations\\" + lang + Constants.YML_EXPANSION)) {
            plugin.getLogger().warning(() -> String.format("Unknown language '%s'. Selected 'en' as the default language.", lang));
            lang = "en";
        }
        File file = new File(plugin.getFileManager().getLangsDir() + File.separator + lang + Constants.YML_EXPANSION);
        this.langConfig = YamlConfiguration.loadConfiguration(file);
    }
}
