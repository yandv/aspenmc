package br.com.aspenmc.clan;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum ClanTag {

    NONE("§7"), BETA("§1");

    private final String color;

    private static final Map<String, ClanTag> BY_NAME = new HashMap<>();

    static {
        for (ClanTag tag : values()) {
            BY_NAME.put(tag.name().toLowerCase(), tag);
        }
    }

    public static ClanTag getByName(String name) {
        return BY_NAME.get(name.toLowerCase());
    }
}
