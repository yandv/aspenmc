package br.com.aspenmc.bukkit.event.player.tag;

import br.com.aspenmc.bukkit.event.PlayerCancellableEvent;
import br.com.aspenmc.permission.Tag;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

@Getter
public class PlayerChangeTagEvent extends PlayerCancellableEvent {

	private Tag oldTag;
	@Setter
	private Tag newTag;
	private boolean forced;

	public PlayerChangeTagEvent(Player p, Tag oldTag, Tag newTag, boolean forced) {
		super(p);
		this.oldTag = oldTag;
		this.newTag = newTag;
		this.forced = forced;
	}

}
