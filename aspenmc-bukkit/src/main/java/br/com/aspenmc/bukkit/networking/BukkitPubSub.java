package br.com.aspenmc.bukkit.networking;

import br.com.aspenmc.CommonConst;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bukkit.event.server.packet.PacketReceiveEvent;
import br.com.aspenmc.bukkit.event.server.packet.PacketReceivedEvent;
import com.google.gson.JsonParser;
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