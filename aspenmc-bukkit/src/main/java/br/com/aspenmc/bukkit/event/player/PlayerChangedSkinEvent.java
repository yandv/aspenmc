package br.com.aspenmc.bukkit.event.player;

import lombok.Getter;
import me.minehurt.bukkit.event.NormalEvent;
import me.minehurt.bukkit.event.PlayerEvent;
import me.minehurt.bukkit.member.BukkitMember;
import me.minehurt.entity.member.Skin;
import org.bukkit.entity.Player;

@Getter
public class PlayerChangedSkinEvent extends NormalEvent {

    private final BukkitMember member;
    private final Skin skin;

    public PlayerChangedSkinEvent(BukkitMember member, Skin skin) {
        this.member = member;
        this.skin = skin;
    }
}
