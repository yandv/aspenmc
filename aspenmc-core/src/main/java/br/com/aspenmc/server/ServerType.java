package br.com.aspenmc.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ServerType {

    /*
     * DEFAULT
     */

    public static final ServerType BUNGEECORD = new ServerType("bungeecord");

    public static final ServerType DISCORD = new ServerType("discord");

    /*
     * SERVER
     */

    public static final ServerType LOBBY = new ServerType("lobby", true);

    public static final ServerType LOGIN = new ServerType("login", true);

    /*
     * SKYWARS
     */

    public static final ServerType SKYWARS_LOBBY = new ServerType("sw_lobby", "lobby", true);

    public static final ServerType SKYWARS_SOLO = new ServerType("sw_solo", "sw_lobby", true);


    /*
     * UNKNOWN
     */

    public static final ServerType UNKNOWN = new ServerType("unknown", true);
    private String name;
    private boolean lobby;

    private String parent;


    public ServerType(String name) {
        this(name, false, "");
    }


    public ServerType(String name, boolean lobby) {
        this(name, lobby, "");
    }

    public ServerType(String name, String parent, boolean lobby) {
        this(name, lobby, parent);
    }

    public String name() {
        return getName();
    }

    public boolean hasParent() {
        return parent != null && !parent.isEmpty();
    }

    public ServerType getParent() {
        return getByName(parent);
    }

    @Override
    public String toString() {
        return name();
    }

    public static final Map<String, ServerType> SERVER_MAP;

    static {
        SERVER_MAP = new HashMap<>();

        for (Field field : ServerType.class.getDeclaredFields()) {
            if (field.getType() == ServerType.class) {
                try {
                    ServerType serverType = (ServerType) field.get(null);

                    if (serverType == UNKNOWN) continue;

                    SERVER_MAP.put(field.getName().toLowerCase(), serverType);
                    SERVER_MAP.put(serverType.getName().toLowerCase(), serverType);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static ServerType[] values() {
        return SERVER_MAP.values().toArray(new ServerType[0]);
    }

    public static ServerType getByName(String name) {
        return SERVER_MAP.get(name.toLowerCase());
    }
}
