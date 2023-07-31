package br.com.aspenmc.bukkit.event.player.language;

import br.com.aspenmc.bukkit.entity.BukkitMember;
import br.com.aspenmc.bukkit.event.PlayerEvent;
import br.com.aspenmc.language.Language;
import lombok.Getter;

@Getter
public class PlayerLanguageChangedEvent extends PlayerEvent {

    private final BukkitMember member;
    private final Language language;

    public PlayerLanguageChangedEvent(BukkitMember member, Language language) {
        super(member.getPlayer());
        this.member = member;
        this.language = language;
    }
}
