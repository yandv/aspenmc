package br.com.aspenmc.packet.type.server.language;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.language.Language;
import br.com.aspenmc.packet.Packet;

import java.util.Map;

public class TranslationReload extends Packet {

    private String language;
    private Map<String, String> translations;

    public TranslationReload(Language language) {
        this.language = language.name();
        this.translations = CommonPlugin.getInstance().getLanguageManager().getTranslations(language);
    }

    @Override
    public void receive() {
        if (CommonPlugin.getInstance().getServerId().equals(getSource())) return;

        CommonPlugin.getInstance().getLanguageManager().getTranslationMap().put(Language.getByName(language), translations);
    }
}
