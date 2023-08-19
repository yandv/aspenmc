package br.com.aspenmc.bukkit.menu.skin;

import br.com.aspenmc.bukkit.utils.menu.MenuInventory;
import br.com.aspenmc.language.Language;
import org.bukkit.entity.Player;

public class SkinLibraryInventory extends MenuInventory {

    public SkinLibraryInventory(Player player) {
        super(Language.getLanguage(player.getUniqueId()).t("menu.skin-library.title"), 5);

        open(player);
    }
}
