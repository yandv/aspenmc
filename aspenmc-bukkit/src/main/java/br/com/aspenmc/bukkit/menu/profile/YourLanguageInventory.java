package br.com.aspenmc.bukkit.menu.profile;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bukkit.utils.item.ItemBuilder;
import br.com.aspenmc.bukkit.utils.menu.MenuInventory;
import br.com.aspenmc.language.Language;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class YourLanguageInventory extends MenuInventory {

    public YourLanguageInventory(Player player) {
        super("§7" + Language.getLanguage(player.getUniqueId()).t("menu.your-language.title", "§7Seu idioma"), 3);

        for (int i = 0; i < Language.values().length; i++) {
            Language language = Language.values()[i];
            setItem(10 + i,
                    new ItemBuilder().name("§a" + language.getLanguageName()).type(Material.SKULL_ITEM).durability(3)
                                     .skinURL("http://textures.minecraft.net/texture/" + language.getSkinUrl())
                                     .lore(language.translate(
                                             "menu.your-language." + language.name().toLowerCase() + ".lore"))
                                     .build(), actionArgs -> {
                        CommonPlugin.getInstance().getMemberManager().getMemberById(player.getUniqueId())
                                    .ifPresent(member -> {
                                        member.performCommand("language " + language.name());
                                        new YourLanguageInventory(player);
                                    });
                    });
        }

        open(player);
    }
}
