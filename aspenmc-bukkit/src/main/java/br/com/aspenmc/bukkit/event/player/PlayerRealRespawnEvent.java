package br.com.aspenmc.bukkit.event.player;

import br.com.aspenmc.bukkit.event.PlayerEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class PlayerRealRespawnEvent extends PlayerEvent {

    @Getter
    private Entity killer;

    @Getter
    @Setter
    private Location respawnLocation;

    @Getter
    @Setter
    private boolean dropXp = true;

    @Getter
    @Setter
    private boolean dropItems = true;

    @Getter
    @Setter
    private List<ItemStack> drops;

    @Setter
    @Getter
    private Location dropLocation;

    public PlayerRealRespawnEvent(Player player, Entity killer) {
        super(player, false);
        this.respawnLocation = player.getBedSpawnLocation() == null ? player.getWorld().getSpawnLocation() :
                               player.getBedSpawnLocation();
        this.killer = killer;
        this.dropLocation = player.getLocation();
        this.drops = Arrays.asList(player.getInventory().getContents());
    }
}
