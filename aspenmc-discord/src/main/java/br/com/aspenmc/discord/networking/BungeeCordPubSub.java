package br.com.aspenmc.discord.networking;

import br.com.aspenmc.CommonConst;
import br.com.aspenmc.CommonPlugin;
import com.google.gson.JsonParser;
import redis.clients.jedis.JedisPubSub;

public class BungeeCordPubSub extends JedisPubSub {

    @Override
    public void onMessage(String channel, String message) {
        if (channel.equals(CommonConst.SERVER_PACKET_CHANNEL)) {
            CommonPlugin.getInstance().getPacketManager()
                        .runPacket(JsonParser.parseString(message).getAsJsonObject(), packet -> true, packet -> {});
        }
    }
}