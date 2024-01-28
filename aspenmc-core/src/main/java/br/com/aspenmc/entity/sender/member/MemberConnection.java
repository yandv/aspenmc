package br.com.aspenmc.entity.sender.member;

import lombok.Getter;

import java.util.UUID;

@Getter
public class MemberConnection {

    private final String playerName;

    private final UUID playerId;
    private final boolean premium;

    private transient boolean cached;

    public MemberConnection(String playerName, UUID playerId, boolean premium) {
        this.playerName = playerName;
        this.playerId = playerId;
        this.premium = premium;
    }

    public boolean isCracked() {
        return !this.premium;
    }

    public MemberConnection cache() {
        this.cached = true;
        return this;
    }
}
