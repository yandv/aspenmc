package br.com.aspenmc.bukkit.utils.character;

import br.com.aspenmc.bukkit.utils.Location;
import br.com.aspenmc.bukkit.utils.character.impl.DefaultCharacter;
import br.com.aspenmc.entity.member.Skin;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.util.EulerAngle;

@Getter
@Setter
@AllArgsConstructor
public class CharacterModel {

    private final String characterName;

    private Location location;

    private boolean collision;

    private Skin skin;

    private EulerAngle headRotation;
    private EulerAngle bodyRotation;

    private EulerAngle leftArmRotation;
    private EulerAngle rightArmRotation;

    private EulerAngle leftLegRotation;
    private EulerAngle rightLegRotation;

    private String classType;

    public CharacterModel(String characterName, Location location, boolean collision, Skin skin) {
        this(characterName, location, collision, skin, null, null, null, null, null, null,
             DefaultCharacter.class.getSimpleName());
    }

    public Class<? extends Character> getCharacterClass() {
        if (classType != null) {
            try {
                return (Class<? extends Character>) Class.forName(
                        "me.minehurt.bukkit.utils.character.impl." + classType);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (skin != null) {
            return DefaultCharacter.class;
        }

        return null;
    }
}
