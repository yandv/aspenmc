package br.com.aspenmc.bukkit.menu.skin;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bukkit.utils.item.ItemBuilder;
import br.com.aspenmc.bukkit.utils.menu.MenuInventory;
import br.com.aspenmc.entity.sender.member.Member;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class SkinInventory extends MenuInventory {

    public SkinInventory(Player player) {
        super("§7Selecione sua skin", 3);

        Member member = CommonPlugin.getInstance().getMemberManager().getMemberById(player.getUniqueId()).orElse(null);

        if (member == null) return;

        setItem(10,
                new ItemBuilder().name("§a" + member.getName()).lore("§fSua skin atual: §7" + member.getPlayerSkin())
                                 .type(Material.SKULL_ITEM).durability(3).skin(member.getSkin()).build());

        setItem(11,
                new ItemBuilder().name("§aAlterar sua skin").lore("§7Clique para alterar sua skin").type(Material.PAPER)
                                 .build(), clickArgs -> {
                    member.sendMessage(member.t("command.skin.usage"));
                    player.closeInventory();
                });

        setItem(12, new ItemBuilder().name("§aBiblioteca").formatLore(
                                             "§7Clique para ver as skins disponibilizadas gratuitamente pelo " +
                                                     "servidor.")
                                     .type(Material.BOOKSHELF).build(),
                clickArgs -> new SkinLibraryInventory(clickArgs.getPlayer(), this));

        if (member.isUsingCustomSkin()) {
            setItem(15, new ItemBuilder().name("§aRemover sua skin").lore("§7Clique para remover sua skin")
                                         .type(Material.BARRIER).build(), clickArgs -> {
                member.performCommand("skin #");
            });
        }

        open(player);
    }
}
