package br.com.aspenmc.language;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.entity.Member;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Getter
public class Language {

    public static final Language PORTUGUESE = new Language("Português", "pt-br",
            "9668a1fb6af81b231bbcc4de5f7f95803bbd194f5827da027fa70321cf47c");
    public static final Language ENGLISH = new Language("English", "en",
            "bee5c850afbb7d8843265a146211ac9c615f733dcc5a8e2190e5c247dea32");

    @Getter(AccessLevel.NONE)
    private String name;
    private final String languageName;
    private final String languageCode;

    private final String skinUrl;

    /**
     * Retrieve the translation by the id and replace the placeholders
     * If the translation is not found, the default message will be returned
     *
     * @param translateId id of the translation
     * @param replaces    placeholders
     * @return the translation
     */

    public String t(String translateId, String... replaces) {
        return CommonPlugin.getInstance().getLanguageManager().translate(this, translateId, replaces);
    }

    /**
     * @return the name of the language
     */

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return name();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Language) {
            return ((Language) obj).name().equals(name());
        }

        return false;
    }

    public static final Map<String, Language> LANGUAGE_MAP;

    static {
        LANGUAGE_MAP = new HashMap<>();

        for (Field field : Language.class.getDeclaredFields()) {
            if (field.getType() == Language.class) {
                try {
                    Language language = (Language) field.get(null);

                    language.name = field.getName().toUpperCase();

                    LANGUAGE_MAP.put(field.getName().toLowerCase(), language);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Language getLanguage(UUID uniqueId) {
        return CommonPlugin.getInstance().getMemberManager().getMemberById(uniqueId).map(Member::getLanguage)
                           .orElse(CommonPlugin.getInstance().getDefaultLanguage());
    }

    public static Language[] values() {
        return LANGUAGE_MAP.values().toArray(new Language[0]);
    }

    public static Language getByName(String name) {
        return LANGUAGE_MAP.getOrDefault(name.toLowerCase(), LANGUAGE_MAP.values().stream().filter(lang ->
                                                                                 lang.getLanguageCode().equalsIgnoreCase(name) || lang.name().equalsIgnoreCase(name)).findFirst()
                                                                         .orElse(null));
    }

    public static Stream<Language> stream() {
        return Stream.of(values());
    }
}
