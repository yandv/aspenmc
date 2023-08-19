package br.com.aspenmc.bukkit.event.player;

import br.com.aspenmc.bukkit.entity.BukkitMember;
import br.com.aspenmc.bukkit.event.PlayerCancellableEvent;
import br.com.aspenmc.entity.member.status.League;
import br.com.aspenmc.entity.member.status.Status;
import br.com.aspenmc.entity.member.status.StatusType;
import org.bukkit.entity.Player;

import lombok.Getter;

@Getter
public class PlayerChangeLeagueEvent extends PlayerCancellableEvent {

    private BukkitMember bukkitMember;
    private Status status;
    private League oldLeague;
    private League newLeague;

    public PlayerChangeLeagueEvent(Player p, BukkitMember player, Status status, League oldLeague, League newLeague) {
        super(p);
        this.status = status;
        this.bukkitMember = player;
        this.oldLeague = oldLeague;
        this.newLeague = newLeague;
    }
}
