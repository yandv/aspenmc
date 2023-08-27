package br.com.aspenmc.packet.type.member;

import br.com.aspenmc.CommonConst;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.entity.Member;
import br.com.aspenmc.packet.Packet;
import br.com.aspenmc.utils.Reflection;
import com.google.gson.JsonElement;
import lombok.AllArgsConstructor;

import java.lang.reflect.Field;
import java.util.UUID;

@AllArgsConstructor
public class MemberFieldUpdate extends Packet {

    private UUID playerId;
    private String[] fields;
    private JsonElement[] values;

    @Override
    public void receive() {
        if (getSource().equalsIgnoreCase(CommonPlugin.getInstance().getServerId())) return;

        CommonPlugin.getInstance().getMemberManager().getMemberById(playerId).ifPresent(member -> {
            for (int i = 0; i < fields.length; i++) {
                try {

                    String fieldName = fields[i];

                    if (fieldName.contains(".")) {
                        String[] split = fieldName.split("\\.");

                        String field = split[0];
                        String subField = split[1];

                        Field parentField = Reflection.getField(Member.class, field);
                        Object parentObject = parentField.get(member);

                        if (parentObject == null) continue;

                        Field childField = Reflection.getField(parentObject.getClass(), subField);
                        Object childObject = values[i] == null || values[i].isJsonNull() ? null :
                                CommonConst.GSON.fromJson(values[i], childField.getType());

                        childField.setAccessible(true);
                        childField.set(parentObject, childObject);
                        continue;
                    }

                    Field field = Reflection.getField(Member.class, fieldName);
                    Object object = values[i] == null || values[i].isJsonNull() ? null :
                            CommonConst.GSON.fromJson(values[i], field.getType());

                    field.setAccessible(true);
                    field.set(member, object);
                } catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
