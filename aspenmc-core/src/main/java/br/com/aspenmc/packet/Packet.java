package br.com.aspenmc.packet;

import lombok.AllArgsConstructor;
import lombok.Getter;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.server.ServerType;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public abstract class Packet {

    private String id = UUID.randomUUID().toString();
    private String packetClassName = getClass().getSimpleName();

    private final String source;
    private final String serverType;

    private boolean exclusiveServers;
    private List<String> serverList = new ArrayList<>();
    private List<String> serverTypes = new ArrayList<>();

    public Packet() {
        this.source = CommonPlugin.getInstance().getServerId();
        this.serverType = CommonPlugin.getInstance().getServerType().getName();
    }

    public Packet id(String id) {
        this.id = id;
        return this;
    }

    public Packet server(String... servers) {
        exclusiveServers = true;
        Collections.addAll(this.serverList, servers);
        return this;
    }

    public Packet server(ServerType... servers) {
        exclusiveServers = true;
        this.serverTypes.addAll(Arrays.stream(servers).map(ServerType::getName).collect(Collectors.toList()));
        return this;
    }

    public Packet bungeecord() {
        server(ServerType.getByName("bungeecord"));
        return this;
    }

    public Packet discord() {
        server(ServerType.getByName("discord"));
        return this;
    }

    public ServerType getServerType() {
        return ServerType.getByName(this.serverType);
    }

    public void receive() {

    }

    public void send() {

    }
}
