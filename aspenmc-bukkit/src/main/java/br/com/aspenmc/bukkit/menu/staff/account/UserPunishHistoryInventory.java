package br.com.aspenmc.bukkit.menu.staff.account;

import br.com.aspenmc.bukkit.utils.item.ItemBuilder;
import br.com.aspenmc.bukkit.utils.menu.MenuInventory;
import br.com.aspenmc.entity.Member;
import br.com.aspenmc.punish.PunishType;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class UserPunishHistoryInventory extends MenuInventory {

    public UserPunishHistoryInventory(Player player, Member target, MenuInventory backInventory) {
        super("§7Histórico de " + target.getName(), 5);

        setItem(13, new ItemBuilder().name("§a" + target.getName()).type(Material.SKULL_ITEM).durability(3)
                                     .skin(target.getSkin())
                                     .formatLore("\n§fRank: " + target.getDefaultTag().getColoredName()).build());

        setItem(29, new ItemBuilder().name("§aBanimentos").type(Material.BOOK).formatLore(
                        (target.getPunishConfiguration().getCount(PunishType.BAN) == 0 ? "§7Nenhum banimento " +
                                "registrado." :
                                "§7" + target.getPunishConfiguration().getCount(PunishType.BAN) + " banimentos " +
                                        "registrados.") + "\n§7Clique para ver os banimentos.").type(Material.BOOK).build(),
                clickArgs -> new UserPunishInventory(player, target, PunishType.BAN, this));

        setItem(30, new ItemBuilder().name("§aSilenciamentos").type(Material.BOOK).formatLore(
                                             (target.getPunishConfiguration().getCount(PunishType.BAN) == 0 ?
                                                     "§7Nenhum silenciamento registrado." :
                                                     "§7" + target.getPunishConfiguration().getCount(PunishType.BAN) +
                                                             " banimsilenciamentoentos registrados.") + "\n§7Clique " + "para ver os silenciamento.")
                                     .type(Material.BOOK).build(),
                clickArgs -> new UserPunishInventory(player, target, PunishType.MUTE, this));

        if (backInventory != null) {
            setItem(40, new ItemBuilder().name("§aRetorna para " + backInventory.getTitle())
                                         .formatLore("§7Clique para voltar.").type(Material.ARROW).build(),
                    clickArgs -> backInventory.open(player));
        }

        open(player);
    }
}
