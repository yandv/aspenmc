package br.com.aspenmc.bukkit.manager;

import br.com.aspenmc.bukkit.utils.character.Character;
import lombok.Getter;

import java.util.*;

@Getter
public class CharacterManager {

    private final Set<Character> characterSet;

    public CharacterManager() {
        this.characterSet = new HashSet<>();
    }

    public void registerCharacter(Character character) {
        this.characterSet.add(character);
    }

    public Character getCharacterById(int entityId) {
        return this.characterSet.stream().filter(character -> character.getEntityId() == entityId).findFirst()
                                .orElse(null);
    }

    public Collection<Character> getCharacters() {
        return this.characterSet;
    }
}
