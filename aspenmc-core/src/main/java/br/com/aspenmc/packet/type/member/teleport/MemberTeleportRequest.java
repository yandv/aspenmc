package br.com.aspenmc.packet.type.member.teleport;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.entity.Member;
import br.com.aspenmc.packet.Packet;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class MemberTeleportRequest extends Packet {

    private UUID uniqueId;
    private UUID targetId;
    private String targetName;

    public MemberTeleportRequest(UUID uniqueId, UUID targetId) {
        this.uniqueId = uniqueId;
        this.targetId = targetId;
        bungeecord();
    }

    public MemberTeleportRequest(UUID uniqueId, String targetName) {
        this.uniqueId = uniqueId;
        this.targetName = targetName;
        bungeecord();
    }

    @Override
    public void receive() {
        super.receive();

        Member member = CommonPlugin.getInstance().getMemberManager().getMemberById(uniqueId).orElse(null);

        if (member == null) return;

        if (targetId != null) {
            String nameById = CommonPlugin.getInstance().getPluginPlatform().getNameById(targetId);

            if (nameById != null) {
                member.sendServer(CommonPlugin.getInstance().getServerId());
                CommonPlugin.getInstance().getServerService().sendPacket(
                        new MemberTeleportResponse(uniqueId, targetId, nameById,
                                                   CommonPlugin.getInstance().getServerId(), true).server(getSource()));
            }
        } else {
            UUID idByName = CommonPlugin.getInstance().getPluginPlatform().getUniqueId(targetName);

            if (idByName != null) {
                member.sendServer(CommonPlugin.getInstance().getServerId());
                CommonPlugin.getInstance().getServerService().sendPacket(
                        new MemberTeleportResponse(uniqueId, idByName, targetName,
                                                   CommonPlugin.getInstance().getServerId(), true).server(getSource()));
            }
        }
    }
}
