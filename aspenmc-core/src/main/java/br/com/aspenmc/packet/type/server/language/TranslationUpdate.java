package br.com.aspenmc.packet.type.server.language;

import br.com.aspenmc.CommonConst;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.language.Language;
import br.com.aspenmc.packet.Packet;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;

@AllArgsConstructor
@Getter
public class TranslationUpdate extends Packet {

    private String language;
    private String translationId;
    private String translation;

    @Override
    public void receive() {
        if (CommonPlugin.getInstance().getServerId().equals(getSource())) return;

        CommonPlugin.getInstance().getLanguageManager()
                    .translation(Language.getByName(language), translationId, translation);
    }
}
