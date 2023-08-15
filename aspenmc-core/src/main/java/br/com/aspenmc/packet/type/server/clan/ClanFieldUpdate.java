package br.com.aspenmc.packet.type.server.clan;

import br.com.aspenmc.CommonConst;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.clan.Clan;
import br.com.aspenmc.entity.Member;
import br.com.aspenmc.packet.Packet;
import br.com.aspenmc.utils.Reflection;
import com.google.gson.JsonElement;
import lombok.Getter;

import java.lang.reflect.Field;
import java.util.UUID;

@Getter
public class ClanFieldUpdate extends Packet {

    private UUID clanId;
    private String[] fields;
    private JsonElement[] values;

    public ClanFieldUpdate(Clan clan, String[] fields, JsonElement[] values) {
        this.clanId = clan.getClanId();
        this.fields = fields;
        this.values = values;
    }

    @Override
    public void receive() {
        if (getSource().equalsIgnoreCase(CommonPlugin.getInstance().getServerId())) return;

        CommonPlugin.getInstance().getClanManager().getClanById(clanId).ifPresent(clan -> {
            for (int i = 0; i < fields.length; i++) {
                try {
                    Field field = Reflection.getField(Member.class, fields[i]);
                    Object object = values[i] == null || values[i].isJsonNull() ? null :
                            CommonConst.GSON.fromJson(values[i], field.getType());

                    field.setAccessible(true);
                    field.set(clan, object);
                } catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
