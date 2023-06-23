package br.com.aspenmc.entity;

import br.com.aspenmc.permission.Group;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.UUID;

public interface Sender {

    /**
     * Gets the original name of the sender
     *
     * @return the name of the sender
     */

    String getName();

    /**
     * Gets the original name of the sender, but if the sender is using a fake name, it will return the fake name
     *
     * @return the name of the sender
     */

    String getRealName();

    /**
     * Gets the constraint name of the sender
     *
     * @return the constraint name of the sender
     */

    default String getConstraintName() {
        return getName() + "(" + getUniqueId() + ")";
    }

    /**
     * Gets the unique id of the sender
     *
     * @return the unique id of the sender
     */

    UUID getUniqueId();

    /**
     * Sends a message to the sender
     *
     * @param messages the messages to send
     */

    void sendMessage(String... messages);

    /**
     * Sends a message to the sender
     *
     * @param messages the messages to send
     */

    void sendMessage(TextComponent... messages);

    /**
     * Checks if the sender is a player
     *
     * @return true if the sender is a player
     */

    boolean isPlayer();

    /**
     * Checks if the sender has a permission
     *
     * @param permission the permission to check
     * @return true if the sender has the permission
     */

    boolean hasPermission(String permission);

    Group getServerGroup();
}
