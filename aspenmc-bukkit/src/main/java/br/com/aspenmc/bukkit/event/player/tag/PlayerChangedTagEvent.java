package br.com.aspenmc.bukkit.event.player.tag;

import br.com.aspenmc.bukkit.event.PlayerCancellableEvent;
import br.com.aspenmc.entity.Member;
import br.com.aspenmc.permission.Tag;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

@Getter
public class PlayerChangedTagEvent extends PlayerCancellableEvent {

    private Member member;

    private Tag oldTag;
    @Setter
    private Tag newTag;
    private boolean forced;

    public PlayerChangedTagEvent(Player player, Member member, Tag oldTag, Tag newTag, boolean forced) {
        super(player);
        this.member = member;
        this.oldTag = oldTag;
        this.newTag = newTag;
        this.forced = forced;
    }
}
