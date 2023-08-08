package br.com.aspenmc.manager;

import br.com.aspenmc.CommonConst;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.language.Language;
import br.com.aspenmc.language.Translation;
import br.com.aspenmc.packet.type.server.language.TranslationUpdate;
import br.com.aspenmc.utils.FileUtils;
import br.com.aspenmc.utils.json.JsonUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

import java.util.HashMap;
import java.util.Map;

@Getter
public class LanguageManager {

    public static void main(String[] args) {
        LanguageManager languageManager = new LanguageManager();
    }

    private final Map<Language, Map<String, String>> translationMap;

    public LanguageManager() {
        translationMap = new HashMap<>();

        for (Language language : Language.values()) {
            translationMap.put(language, new HashMap<>());

            try {
                JsonObject jsonObject = JsonUtils.fileToJson(
                        FileUtils.createFile(language.name().toLowerCase() + ".json",
                                             CommonConst.PRINCIPAL_DIRECTORY + "translations", true)).getAsJsonObject();

                Map<String, String> map = new HashMap<>();

                for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                    map.put(entry.getKey(), entry.getValue().getAsString());
                }

                translationMap.put(language, map);
                System.out.println("The language " + language.name() + " has been loaded.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String translateOrDefault(Language language, String translationId, String defaultMessage, String... replaces) {
        if (!translationMap.containsKey(language)) {
            translationMap.put(language, new HashMap<>());
        }

        Map<String, String> languageMap = translationMap.get(language);

        String translation;

        if (languageMap.containsKey(translationId)) {
            translation = languageMap.get(translationId);
        } else {
            CommonPlugin.getInstance().getPluginPlatform()
                        .runAsync(() -> setTranslation(language, translationId, defaultMessage));
            translation = defaultMessage;
        }

        for (int i = 0; i < replaces.length; i += 2) {
            translation = translation.replace(replaces[i], replaces[i + 1]);
        }

        return ChatColor.translateAlternateColorCodes('&', translation);
    }

    public String translate(Language language, String translationId, String... replaces) {
        return translateOrDefault(language, translationId, "{" + translationId + "}", replaces);
    }

    public String translate(Language language, Translation translation, String... replaces) {
        return translate(language, translation.getId(), replaces);
    }

    public void setTranslation(Language language, String translationId, String translation) {
        translationMap.computeIfAbsent(language, v -> new HashMap<>()).put(translationId, translation);

        CommonPlugin.getInstance().getPacketManager()
                    .sendPacket(new TranslationUpdate(language, translationId, translation));

        try {
            JsonUtils.saveJsonAsFile(CommonConst.GSON_PRETTY.toJson(translationMap.get(language)),
                                     language.name().toLowerCase() + ".json",
                                     CommonConst.PRINCIPAL_DIRECTORY + "translations");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Map<String, String> getTranslations(Language language) {
        return translationMap.get(language);
    }
}