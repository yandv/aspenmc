package br.com.aspenmc.packet.type.discord;

import br.com.aspenmc.CommonConst;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.entity.sender.Sender;
import br.com.aspenmc.entity.sender.member.Member;
import br.com.aspenmc.packet.Packet;
import br.com.aspenmc.punish.Punish;
import com.google.common.collect.Maps;
import lombok.Getter;

import java.util.Map;

@Getter
public class MessageRequest extends Packet {

    private String messageType;
    private Map<String, Object> data;

    public MessageRequest(String messageType, Map<String, Object> data) {
        discord();
        this.messageType = messageType;
        this.data = data;
    }

    public static void sendPlayerPunish(Punish punish) {
        Map<String, Object> map = Maps.newHashMap();

        map.put("punish", CommonConst.GSON.toJson(punish));

        CommonPlugin.getInstance().getPacketManager().sendPacket(new MessageRequest("punish", map));
    }

    public static void sendReportMessage(Sender sender, Member target, String reason) {
        Map<String, Object> map = Maps.newHashMap();

        map.put("sender", sender.getName());
        map.put("senderId", sender.getUniqueId());
        map.put("target", target.getName());
        map.put("targetId", target.getUniqueId());
        map.put("reason", reason);

        CommonPlugin.getInstance().getPacketManager().sendPacket(new MessageRequest("report", map));
    }

    public static void sendAnticheatMessage(String name, String cheatType, String serverId, String message) {
        Map<String, Object> map = Maps.newHashMap();

        map.put("suspect", name);
        map.put("serverId", serverId);
        map.put("message", message);
        map.put("cheatType", cheatType);

        CommonPlugin.getInstance().getPacketManager().sendPacket(new MessageRequest("anticheat", map));
    }
}
