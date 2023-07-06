package br.com.aspenmc.bukkit.utils.character;

import br.com.aspenmc.bukkit.utils.character.impl.DefaultCharacter;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class CharacterCreator {

    private String title;
    private Location location;

    private List<String> linesBelow;

    private List<String> linesAbove;

    private Class<? extends Character> characterClass = DefaultCharacter.class;

    public CharacterCreator character(Class<? extends Character> characterClass) {
        this.characterClass = characterClass;
        return this;
    }

    public CharacterCreator location(Location location) {
        this.location = location;
        return this;
    }

    public CharacterCreator title(String title) {
        this.title = title;
        return this;
    }

    public CharacterCreator lineBelow(String line) {
        if (this.linesBelow == null) {
            this.linesBelow = new ArrayList<>();
        }

        this.linesBelow.add(line);
        return this;
    }

    public CharacterCreator lineAbove(String line) {
        if (this.linesAbove == null) {
            this.linesAbove = new ArrayList<>();
        }

        this.linesAbove.add(line);
        return this;
    }

    public Character build() {
        return null;
    }

    public static Character createCharacter() {
        return null;
    }
}
