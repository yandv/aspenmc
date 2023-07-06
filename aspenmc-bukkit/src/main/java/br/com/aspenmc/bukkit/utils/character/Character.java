package br.com.aspenmc.bukkit.utils.character;

import br.com.aspenmc.bukkit.BukkitCommon;
import br.com.aspenmc.bukkit.utils.TouchHandler;
import br.com.aspenmc.bukkit.utils.hologram.Hologram;
import br.com.aspenmc.utils.string.CodeCreator;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Optional;

public interface Character {

    public static final CodeCreator CODE_CREATOR = new CodeCreator(10).setSpecialCharacters(false).setUpperCase(false)
                                                                      .setNumbers(true);

    /**
     * Set the Character collision
     *
     * @param collision Whether the Character have collision
     */

    void setCollision(boolean collision);

    /**
     * Show the Character to a player
     *
     * @param player The player to show the Character to
     */

    void show(Player player);

    /**
     * Hide the Character from a player
     *
     * @param player The player to hide the Character from
     */

    void hide(Player player);

    /**
     * Check if the Character is showing to a player
     *
     * @param player The player to check if the Character is showing to
     * @return Whether the Character is showing to the player
     */

    boolean isShowing(Player player);

    /**
     * Teleport the Character to a location
     *
     * @param location The location to teleport the Character
     */

    void teleport(Location location);

    Character setHologram(Hologram hologram);

    void setModel(String model);

    /**
     * Check if the Character have collision
     *
     * @return Whether the Character have collision
     */

    boolean hasCollision();

    /**
     * Get the Character's location
     *
     * @return The Character Location
     */

    Location getLocation();

    /**
     * Get the Character Interact Handler
     *
     * @return The Character Interact Handler
     */

    TouchHandler<Character> getTouchHandler();

    /**
     * Retrieve the current entity id
     *
     * @return The current entity id if the Character is spawned, -1 otherwise
     */

    int getEntityId();
}
