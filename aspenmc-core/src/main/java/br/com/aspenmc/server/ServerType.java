package br.com.aspenmc.server;

import br.com.aspenmc.utils.string.StringFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum ServerType {

    UNKNOWN,

    BUNGEECORD,
    DISCORD,

    HG,

    LOBBY(true),
    HG_LOBBY(true, "lobby"),
    LOGIN(true);

    private final boolean lobby;
    private final String parent;

    ServerType() {
        this(false, "");
    }

    ServerType(boolean lobby) {
        this(lobby, "");
    }

    public boolean hasParent() {
        return parent != null && !parent.isEmpty();
    }

    public ServerType getParent() {
        return getByName(parent);
    }

    public String getName() {
        return StringFormat.formatString(name());
    }

    public static final Map<String, ServerType> SERVER_MAP;

    static {
        SERVER_MAP = new HashMap<>();

        for (ServerType serverType : ServerType.values()) {
            if (serverType == UNKNOWN) continue;

            SERVER_MAP.put(serverType.name().toLowerCase(), serverType);
        }
    }

    public static ServerType getByName(String name) {
        return SERVER_MAP.getOrDefault(name.toLowerCase(), UNKNOWN);
    }
}
