package br.com.aspenmc.packet.type.server.tag;

import com.google.gson.JsonElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import br.com.aspenmc.CommonConst;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.packet.Packet;
import br.com.aspenmc.permission.Tag;
import br.com.aspenmc.utils.Reflection;

import java.lang.reflect.Field;

@Getter
@AllArgsConstructor
public class TagFieldUpdate extends Packet {

    private String tagName;
    private String[] fields;
    private JsonElement[] values;

    @Override
    public void receive() {
        Tag tag = CommonPlugin.getInstance().getPermissionManager().getTagByName(tagName).orElse(null);

        if (tag == null) {
            CommonPlugin.getInstance().getLogger().warning("Received tag field update for non-existent tag " + tagName);
            return;
        }

        for (int i = 0; i < fields.length; i++) {
            try {
                Field field = Reflection.getField(Tag.class, fields[i]);
                Object object = values[i] == null || values[i].isJsonNull() ? null :
                                CommonConst.GSON.fromJson(values[i], field.getType());

                field.setAccessible(true);
                field.set(tag, object);
            } catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
