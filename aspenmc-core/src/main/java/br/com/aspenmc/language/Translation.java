package br.com.aspenmc.language;

public interface Translation {

    String getId();

    default String translate(Language language, String... replaces) {
        return language.t(getId(), replaces);
    }
}
