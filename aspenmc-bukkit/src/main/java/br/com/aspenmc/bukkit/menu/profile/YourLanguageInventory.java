package br.com.aspenmc.bukkit.menu.profile;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bukkit.utils.item.ItemBuilder;
import br.com.aspenmc.bukkit.utils.menu.MenuInventory;
import br.com.aspenmc.language.Language;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class YourLanguageInventory extends MenuInventory {

    public YourLanguageInventory(Player player, MenuInventory backInventory) {
        super(Language.getLanguage(player.getUniqueId()).t("menu.your-language.title"), 3);

        for (int i = 0; i < Language.values().length; i++) {
            Language language = Language.values()[i];
            setItem(10 + i,
                    new ItemBuilder().name("§a" + language.getLanguageName()).type(Material.SKULL_ITEM).durability(3)
                                     .skinURL("http://textures.minecraft.net/texture/" + language.getSkinUrl())
                                     .glow(language == Language.getLanguage(player.getUniqueId()))
                                     .lore(language.t("menu.your-language." + language.name().toLowerCase() + ".lore"))
                                     .build(), actionArgs -> {
                        CommonPlugin.getInstance().getMemberManager().getMemberById(player.getUniqueId())
                                    .ifPresent(member -> {
                                        member.performCommand("language " + language.name());
                                        new YourLanguageInventory(player, backInventory);
                                    });
                    });
        }

        if (backInventory != null) {
            setItem(21, new ItemBuilder().name("§aRetorna para " + backInventory.getTitle())
                                         .formatLore("§7Clique para voltar.").type(Material.ARROW).build(),
                    clickArgs -> backInventory.open(player));
        }

        open(player);
    }
}
