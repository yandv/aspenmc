package br.com.aspenmc.bukkit.menu;

import br.com.aspenmc.bukkit.utils.item.ItemBuilder;
import br.com.aspenmc.bukkit.utils.menu.MenuInventory;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ConfirmInventory extends MenuInventory {

    public ConfirmInventory(Player player, String title, ResultHandler resultHandler) {
        this(player, title, resultHandler, null);
    }


    public ConfirmInventory(Player player, String title, ResultHandler resultHandler, MenuInventory backInventory) {
        super(title, 3);

        setItem(11, new ItemBuilder().name("§aConfirmar").type(Material.WOOL).durability(5).build(),
                clickArgs -> resultHandler.result(true));

        setItem(15, new ItemBuilder().name("§cCancelar").type(Material.WOOL).durability(14).build(), clickArgs -> {
            resultHandler.result(false);

            if (backInventory != null) {
                backInventory.open(clickArgs.getPlayer());
            }
        });

        open(player);
    }

    public interface ResultHandler {

        void result(boolean confirm);
    }
}
