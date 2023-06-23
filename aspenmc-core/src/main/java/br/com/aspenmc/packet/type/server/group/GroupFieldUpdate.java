package br.com.aspenmc.packet.type.server.group;

import com.google.gson.JsonElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import br.com.aspenmc.CommonConst;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.packet.Packet;
import br.com.aspenmc.permission.Group;
import br.com.aspenmc.utils.Reflection;

import java.lang.reflect.Field;

@Getter
@AllArgsConstructor
public class GroupFieldUpdate extends Packet {

    private String groupName;
    private String[] fields;
    private JsonElement[] values;

    @Override
    public void receive() {
        Group group = CommonPlugin.getInstance().getPermissionManager().getGroupByName(groupName).orElse(null);

        if (group == null) {
            CommonPlugin.getInstance().getLogger()
                        .warning("Received group field update for non-existent group " + groupName);
            return;
        }

        for (int i = 0; i < fields.length; i++) {
            try {
                Field field = Reflection.getField(Group.class, fields[i]);
                Object object = values[i] == null || values[i].isJsonNull() ? null :
                                CommonConst.GSON.fromJson(values[i], field.getType());

                field.setAccessible(true);
                field.set(group, object);
            } catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
