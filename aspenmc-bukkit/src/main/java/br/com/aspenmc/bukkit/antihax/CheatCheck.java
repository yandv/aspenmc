package br.com.aspenmc.bukkit.antihax;

import br.com.aspenmc.bukkit.BukkitCommon;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class CheatCheck implements Listener {

    public void alert(Player player, String args) {
        BukkitCommon.getInstance().getCheatManager().getOrLoad(player.getUniqueId()).alert(CheatType.AUTOSOUP, 0, 10);
    }

    public void alert(Player player) {
        alert(player, "");
    }
}
