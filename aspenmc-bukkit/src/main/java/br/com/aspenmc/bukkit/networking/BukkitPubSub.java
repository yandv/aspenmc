package br.com.aspenmc.bukkit.networking;

import com.google.gson.JsonParser;
import me.minehurt.CommonConst;
import me.minehurt.CommonPlugin;
import me.minehurt.bukkit.event.server.packet.PacketReceiveEvent;
import me.minehurt.bukkit.event.server.packet.PacketReceivedEvent;
import org.bukkit.Bukkit;
import redis.clients.jedis.JedisPubSub;

public class BukkitPubSub extends JedisPubSub {

    @Override
    public void onMessage(String channel, String message) {
        if (channel.equals(CommonConst.SERVER_PACKET_CHANNEL)) {
            CommonPlugin.getInstance().getPacketManager()
                        .runPacket(JsonParser.parseString(message).getAsJsonObject(), packet -> {
                            PacketReceiveEvent packetReceiveEvent = new PacketReceiveEvent(packet);
                            Bukkit.getPluginManager().callEvent(packetReceiveEvent);
                            return !packetReceiveEvent.isCancelled();
                        }, packet -> Bukkit.getPluginManager().callEvent(new PacketReceivedEvent(packet)));
        }
    }
}