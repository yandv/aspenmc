package br.com.aspenmc.packet.type.server.tag;

import lombok.AllArgsConstructor;
import lombok.Getter;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.packet.Packet;

@Getter
@AllArgsConstructor
public class TagDelete extends Packet {

    private String tagName;

    @Override
    public void receive() {
        CommonPlugin.getInstance().getPermissionManager().unloadTag(tagName);
    }
}
