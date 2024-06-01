package kz.hxncus.mc.simplepenalty.util;

import kz.hxncus.mc.simplepenalty.SimplePenalty;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@UtilityClass
public class Constants {
    public final String YML_EXPANSION = ".yml";
    public final String VERSION = "version";

    public final Set<String> EMBEDDED_LANGUAGES = new HashSet<>(Arrays.asList("langs\\ar.yml", "langs\\bn.yml", "langs\\da.yml", "langs\\de.yml",
            "langs\\en.yml", "langs\\eo.yml", "langs\\es.yml", "langs\\fr.yml", "langs\\hi.yml", "langs\\id.yml",
            "langs\\id.yml", "langs\\ja.yml", "langs\\nl.yml", "langs\\no.yml", "langs\\pt.yml", "langs\\ru.yml",
            "langs\\sv.yml", "langs\\ua.yml", "langs\\zh.yml"));
    public final Set<String> SUPPORTED_LANGUAGES = new HashSet<>();
    public final Set<String> FILES = new HashSet<>(Collections.singletonList("config.yml"));
    static {
        File[] files = new File(SimplePenalty.getInstance().getDataFolder(), "langs").listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(YML_EXPANSION)) {
                    SUPPORTED_LANGUAGES.add(file.getParentFile().getName() + "\\" + file.getName());
                }
            }
        }
        SUPPORTED_LANGUAGES.addAll(EMBEDDED_LANGUAGES);
        FILES.addAll(SUPPORTED_LANGUAGES);
    }
}
