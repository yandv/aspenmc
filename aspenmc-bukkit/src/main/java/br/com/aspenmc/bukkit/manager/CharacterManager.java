package br.com.aspenmc.bukkit.manager;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bukkit.utils.Location;
import br.com.aspenmc.bukkit.utils.character.Character;
import br.com.aspenmc.bukkit.utils.character.impl.DefaultCharacter;
import br.com.aspenmc.entity.member.Skin;
import lombok.Getter;
import org.bukkit.Bukkit;

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
