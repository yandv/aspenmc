package br.com.aspenmc.manager;

import br.com.aspenmc.CommonConst;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.packet.Packet;
import br.com.aspenmc.utils.ClassGetter;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class PacketManager {

    private final Map<String, Handler<? extends Packet>> waitingPacketMap;
    private final Map<String, Class<? extends Packet>> packetMap;

    private final Map<Class<? extends Packet>, List<Consumer<? extends Packet>>> packetHandlerMap;

    public PacketManager() {
        waitingPacketMap = new ConcurrentHashMap<>();
        packetMap = new HashMap<>();
        packetHandlerMap = new HashMap<>();
//        registerPacket(MemberFieldUpdate.class);
        registerPackets();
    }

    public void onEnable() {
        CommonPlugin.getInstance().getPluginPlatform().runAsyncTimer(this::handleTimeoutPackets, 1L, 1L);
    }

    public <T extends Packet> Consumer<T> registerHandler(Class<T> clazz, Consumer<T> handler) {
        packetHandlerMap.computeIfAbsent(clazz, v -> new ArrayList<>()).add(handler);
        return handler;
    }

    public void registerPackets() {
        for (Class<?> commandClass : ClassGetter.getClassesForPackage(getClass(), "br.com.aspenmc.packet.type"))
            if (Packet.class != commandClass && !Modifier.isAbstract(commandClass.getModifiers())) {
                if (Packet.class.isAssignableFrom(commandClass)) {
                    try {
                        registerPacket(commandClass.asSubclass(Packet.class));
                    } catch (Exception ex) {
                        CommonPlugin.getInstance().getLogger()
                                    .warning("Error when loading command from " + commandClass.getSimpleName() + "!");
                        ex.printStackTrace();
                    }
                }
            }
    }

    public void registerPacket(Class<? extends Packet> clazz) {
        packetMap.put(clazz.getSimpleName(), clazz);
    }

    public void runPacket(JsonObject jsonObject, Function<Packet, Boolean> preReceiveFunction,
            Consumer<Packet> posReceive) {
        Class<? extends Packet> clazz = getPacketClass(jsonObject.get("packetClassName").getAsString());

        if (clazz == null) {
            CommonPlugin.getInstance().getLogger().warning(
                    "The server " + jsonObject.get("source").getAsString() + " has been sent a unknown packet " +
                            jsonObject.get("packetClassName").getAsString() + ".");
            return;
        }

        Packet packet = CommonConst.GSON.fromJson(jsonObject, clazz);

        runPacket(packet, preReceiveFunction, posReceive);
    }

    public Class<? extends Packet> getPacketClass(String name) {
        return packetMap.get(name);
    }

    public <T extends Packet> void runPacket(T packet, Function<Packet, Boolean> preReceiveFunction,
            Consumer<Packet> posReceive) {
        if (!packet.isExclusiveServers() || packet.getServerList().contains(CommonPlugin.getInstance().getServerId()) ||
                packet.getServerTypes().contains(CommonPlugin.getInstance().getServerType().getName())) {

            if (CommonPlugin.getInstance().isServerLogPackets()) {
                CommonPlugin.getInstance()
                            .debug("The server received a packet " + packet.getClass().getSimpleName() + " from " +
                                    packet.getSource() + " with content: " + CommonConst.GSON.toJson(packet) + ".");
            }

            if (preReceiveFunction.apply(packet)) {
                packet.receive();
                posReceive.accept(packet);

                if (waitingPacketMap.containsKey(packet.getId())) {
                    Handler<T> handler = (Handler<T>) waitingPacketMap.get(packet.getId());

                    if (packet.getClass().getSimpleName().equals(handler.packetClassName)) {
                        handler.consumer.accept(packet);

                        waitingPacketMap.remove(packet.getId());
                    }
                }

                if (packetHandlerMap.containsKey(packet.getClass())) {
                    packetHandlerMap.get(packet.getClass())
                                    .forEach(consumer -> ((Consumer<T>) consumer).accept(packet));
                }
            }
        }
    }

    public <T extends Packet> void waitPacket(Class<T> clazz, String identifier, long timeout, Consumer<T> consumer) {
        waitingPacketMap.put(identifier, new Handler<T>(clazz.getSimpleName(), consumer,
                timeout - System.currentTimeMillis() > 0 || timeout == -1 ? timeout :
                        System.currentTimeMillis() + timeout));
    }

    public <T extends Packet> void waitPacket(Class<T> clazz, Packet requestPacket, long timeout,
            Consumer<T> consumer) {
        waitPacket(clazz, requestPacket.getId(), timeout, consumer);
    }

    public <T extends Packet> void waitPacket(Class<T> clazz, String identifier, Consumer<T> consumer) {
        waitPacket(clazz, identifier, -1, consumer);
    }

    public <T extends Packet> void waitPacket(Class<T> clazz, Packet requestPacket, Consumer<T> consumer) {
        waitPacket(clazz, requestPacket.getId(), consumer);
    }

    public void handleTimeoutPackets() {
        Iterator<Map.Entry<String, Handler<? extends Packet>>> iterator = waitingPacketMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, Handler<? extends Packet>> entry = iterator.next();

            if (entry.getValue().getTimeout() != -1 && entry.getValue().getTimeout() < System.currentTimeMillis()) {
                entry.getValue().getConsumer().accept(null);
                iterator.remove();
            }
        }
    }

    public <T extends Packet> void sendPacketAsync(T packet) {
        CommonPlugin.getInstance().getPluginPlatform()
                    .runAsync(() -> CommonPlugin.getInstance().getServerService().sendPacket(packet));
    }

    public <T extends Packet> T sendPacket(T packet) {
        return CommonPlugin.getInstance().getServerService().sendPacket(packet);
    }

    @AllArgsConstructor
    @Getter
    public static class Handler<T extends Packet> {

        private String packetClassName;
        private Consumer<T> consumer;

        private long timeout;
    }
}
