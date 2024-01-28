package br.com.aspenmc.bukkit.event.player;

import br.com.aspenmc.bukkit.entity.BukkitMember;
import br.com.aspenmc.bukkit.event.PlayerCancellableEvent;
import br.com.aspenmc.entity.sender.member.status.League;
import br.com.aspenmc.entity.sender.member.status.Status;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public class PlayerChangeLeagueEvent extends PlayerCancellableEvent {

    private final BukkitMember bukkitMember;
    private final Status status;
    private final League oldLeague;
    private final League newLeague;

    public PlayerChangeLeagueEvent(Player p, BukkitMember player, Status status, League oldLeague, League newLeague) {
        super(p);
        this.status = status;
        this.bukkitMember = player;
        this.oldLeague = oldLeague;
        this.newLeague = newLeague;
    }
}
