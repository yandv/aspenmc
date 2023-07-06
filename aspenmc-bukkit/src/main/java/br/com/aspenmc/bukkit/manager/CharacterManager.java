package br.com.aspenmc.bukkit.manager;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bukkit.utils.Location;
import br.com.aspenmc.bukkit.utils.character.Character;
import br.com.aspenmc.bukkit.utils.character.impl.DefaultCharacter;
import br.com.aspenmc.entity.member.Skin;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.*;

public class CharacterManager {

    private Set<Character> characterSet;
    @Getter
    private Map<String, CharacterModel> characterModelMap;

    public CharacterManager() {
        this.characterSet = new HashSet<>();
        this.characterModelMap = new HashMap<>();
        loadModel(
                new CharacterModel("default", Location.fromLocation(Bukkit.getWorld("world").getSpawnLocation()), true,
                                   CommonPlugin.getInstance().getDefaultSkin()));
    }

    public void loadModel(CharacterModel model) {
        this.characterModelMap.put(model.getCharacterName().toLowerCase(), model);
    }

    public Optional<CharacterModel> getModel(String characterName) {
        return Optional.ofNullable(this.characterModelMap.get(characterName.toLowerCase()));
    }

    public void unloadModel(CharacterModel model) {
        this.characterModelMap.remove(model.getCharacterName().toLowerCase());
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

    public void setLocation(CharacterModel characterModel, Location location) {
        characterModel.setLocation(location);
        getCharacters().stream().filter(character -> characterModel.getCharacterName().equals(character.getModelName()))
                       .forEach(character -> character.teleport(location.toLocation()));
    }

    public void setSkin(CharacterModel characterModel, Skin skin) {
        characterModel.setSkin(skin);
        getCharacters().stream().filter(character -> characterModel.getCharacterName().equals(character.getModelName()))
                       .filter(character -> character instanceof DefaultCharacter)
                       .map(character -> (DefaultCharacter) character).forEach(character -> character.setSkin(skin));
    }

    public void setCollision(CharacterModel characterModel, boolean collision) {
        characterModel.setCollision(collision);
        getCharacters().stream().filter(character -> characterModel.getCharacterName().equals(character.getModelName()))
                       .forEach(character -> character.setCollision(collision));
    }
}
