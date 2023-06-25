package br.com.aspenmc.bungee.networking;

import com.google.gson.JsonParser;
import br.com.aspenmc.CommonConst;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bungee.event.PacketReceiveEvent;
import br.com.aspenmc.bungee.event.PacketReceivedEvent;
import net.md_5.bungee.api.ProxyServer;
import redis.clients.jedis.JedisPubSub;

public class BungeeCordPubSub extends JedisPubSub {

    @Override
    public void onMessage(String channel, String message) {
        if (channel.equals(CommonConst.SERVER_PACKET_CHANNEL)) {
            CommonPlugin.getInstance().getPacketManager().runPacket(JsonParser.parseString(message).getAsJsonObject(),
                                                                    packet -> !ProxyServer.getInstance()
                                                                                          .getPluginManager().callEvent(
                                                                                    new PacketReceiveEvent(packet))
                                                                                          .isCancelled(),
                                                                    packet -> ProxyServer.getInstance()
                                                                                         .getPluginManager().callEvent(
                                                                                    new PacketReceivedEvent(packet)));
        }
    }
}