package br.com.aspenmc.bukkit.event.player.language;

import br.com.aspenmc.bukkit.entity.BukkitMember;
import br.com.aspenmc.bukkit.event.PlayerCancellableEvent;
import br.com.aspenmc.language.Language;
import lombok.Getter;
import lombok.Setter;

@Getter
public class PlayerLanguageChangeEvent extends PlayerCancellableEvent {

    private final BukkitMember member;
    @Setter
    private Language newLanguage;
    private final Language oldLanguage;

    public PlayerLanguageChangeEvent(BukkitMember member, Language newLanguage, Language oldLanguage) {
        super(member.getPlayer());
        this.member = member;
        this.newLanguage = newLanguage;
        this.oldLanguage = oldLanguage;
    }
}
