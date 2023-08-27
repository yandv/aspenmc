package br.com.aspenmc.packet.type.discord;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.packet.Packet;
import br.com.aspenmc.permission.Group;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class ServerStaffMessage extends Packet {

    private UUID playerId;
    private String playerName;
    private String groupName;

    private String message;

    @Nullable
    public Group getGroup() {
        return CommonPlugin.getInstance().getPermissionManager().getGroupByName(groupName).orElse(null);
    }
}
