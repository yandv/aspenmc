package br.com.aspenmc.entity.sender.member.status;

import br.com.aspenmc.server.ServerType;

public enum StatusType {

    HG, ARENA, FPS, LAVA;

    public static StatusType getByServer(ServerType serverType) {
        switch (serverType) {
        case HG_LOBBY:
        case HG:
            return HG;
        case ARENA:
            return ARENA;
        case FPS:
            return FPS;
        case LAVA:
            return LAVA;
        default:
            return null;
        }
    }
}
