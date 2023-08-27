package br.com.aspenmc.discord.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DiscordMemberManager {

    private Map<String, UUID> discordMembersMap;

    public DiscordMemberManager() {
        discordMembersMap = new HashMap<>();
    }

    public void registerMember(String discordId, UUID playerId) {
        discordMembersMap.put(discordId, playerId);
    }

    public UUID getUniqueId(String discordId) {
        return discordMembersMap.get(discordId);
    }

    public void unregisterMember(String discordId) {
        discordMembersMap.remove(discordId);
    }

}
