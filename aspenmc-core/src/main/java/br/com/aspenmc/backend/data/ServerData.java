package br.com.aspenmc.backend.data;

import br.com.aspenmc.packet.Packet;
import br.com.aspenmc.server.ProxiedServer;
import br.com.aspenmc.server.ServerType;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface ServerData {

    /**
     * Start a server
     *
     * @param server Server to start
     * @param maxPlayers Max players of the server
     */

    void startServer(ProxiedServer server, int maxPlayers);

    /**
     * Start the current server
     *
     * @param maxPlayers Max players of the server
     */

    void startServer(int maxPlayers);

    /**
     * Stop the current server
     */

    void stopServer();

    /**
     * Stop a server
     *
     * @param serverId The server id
     * @param serverType The server type
     */

    void stopServer(String serverId, ServerType serverType);

    /**
     * Set the max players of the current server
     *
     * @param maxPlayers Max players of the server
     */

    void setMaxPlayers(int maxPlayers);

    /**
     * Set the max players of a server
     *
     * @param serverId The server id
     * @param maxPlayers Max players of the server
     */

    void setMaxPlayers(String serverId, ServerType serverType, int maxPlayers);

    /**
     * Add a player to the server list
     *
     * @param uniqueId The player's unique id
     */

    void joinPlayer(UUID uniqueId);

    /**
     * Add a player to the server list
     *
     * @param serverId The server id
     * @param uniqueId The player's unique id
     */

    void joinPlayer(String serverId, ServerType serverType, UUID uniqueId);

    /**
     * Remove a player from the server list
     *
     * @param uniqueId The player's unique id
     */

    void leavePlayer(UUID uniqueId);

    /**
     * Remove a player from the server list
     *
     * @param serverId The server id
     * @param uniqueId The player's unique id
     */

    void leavePlayer(String serverId, ServerType serverType, UUID uniqueId);

    /**
     * Fetch all the players on a server
     *
     * @param serverId The server id
     * @return The players on the server
     */

    Set<UUID> getOnlinePlayers(String serverId);

    Map<String, ProxiedServer> retrieveServerById(String... serversId);

    Map<String, ProxiedServer> retrieveServerByType(ServerType... serversType);

    /**
     * Send a packet
     *
     * @param packet The packet to send
     * @param <T>    The type of packet
     * @return The packet that was sent
     */

    <T extends Packet> T sendPacket(T packet);

    /**
     * Send a packet
     *
     * @param packets The packets to send
     */

    void sendPacket(Packet... packets);
}
