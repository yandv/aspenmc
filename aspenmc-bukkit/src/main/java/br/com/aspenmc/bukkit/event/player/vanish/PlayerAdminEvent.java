package br.com.aspenmc.bukkit.event.player.vanish;

import br.com.aspenmc.bukkit.event.PlayerCancellableEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Getter
public class PlayerAdminEvent extends PlayerCancellableEvent {

    private AdminMode adminMode;
    @Setter
    private GameMode gameMode;

    private ItemStack[] contents;
    private ItemStack[] armorContents;

    public PlayerAdminEvent(Player player, AdminMode adminMode, GameMode mode, ItemStack[] contents,
            ItemStack[] armorContents) {
        super(player);
        this.adminMode = adminMode;
        this.gameMode = mode;
        this.contents = contents;
        this.armorContents = armorContents;
    }

    public PlayerAdminEvent(Player player, AdminMode adminMode, GameMode mode) {
        this(player, adminMode, mode, player.getInventory().getContents(), player.getInventory().getArmorContents());
    }

    public enum AdminMode {
        ADMIN, //
        PLAYER
    }
}
