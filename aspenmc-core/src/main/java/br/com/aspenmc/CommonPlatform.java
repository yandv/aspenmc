package br.com.aspenmc;

import net.md_5.bungee.api.chat.TextComponent;

import java.util.UUID;

public interface CommonPlatform {

    /**
     * Broadcast a message to all players
     *
     * @param messages The messages to broadcast
     */

    void broadcast(String... messages);

    /**
     * Broadcast a message to all players
     *
     * @param components The components to broadcast
     */

    void broadcast(TextComponent... components);

    /**
     * Get the name of a player by their UUID
     *
     * @param playerId The UUID of the player
     * @return The name of the player
     */

    String getNameById(UUID playerId);

    /**
     * Get the UUID of a player by their name
     *
     * @param playerName The name of the player
     * @return The UUID of the player
     */

    UUID getUniqueId(String playerName);

    /**
     * Run a runnable asynchronously
     *
     * @param runnable The runnable to run
     */

    void runAsync(Runnable runnable);

    /**
     * Run a runnable after a delay
     *
     * @param runnable The runnable to run
     * @param delay    The delay in milliseconds
     * @param period   The period in milliseconds
     */

    void runAsyncTimer(Runnable runnable, long delay, long period);

    /**
     * Run a runnable synchronously
     *
     * @param runnable The runnable to run
     */

    void runSync(Runnable runnable);

    /**
     * Run a runnable after a delay
     *
     * @param runnable The runnable to run
     * @param delay    The delay in milliseconds
     */

    void runLater(Runnable runnable, long delay);

    /**
     * Run a runnable after a delay
     *
     * @param runnable The runnable to run
     * @param delay    The delay in milliseconds
     * @param period   The period in milliseconds
     */

    void runTimer(Runnable runnable, long delay, long period);
}
