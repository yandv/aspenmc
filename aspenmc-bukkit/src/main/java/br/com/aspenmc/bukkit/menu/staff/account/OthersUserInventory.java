package br.com.aspenmc.bukkit.menu.staff.account;

import br.com.aspenmc.BukkitConst;
import br.com.aspenmc.CommonConst;
import br.com.aspenmc.bukkit.utils.item.ItemBuilder;
import br.com.aspenmc.bukkit.utils.menu.MenuInventory;
import br.com.aspenmc.bukkit.utils.menu.MenuItem;
import br.com.aspenmc.entity.Member;
import br.com.aspenmc.utils.string.StringFormat;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class OthersUserInventory extends MenuInventory {

    public OthersUserInventory(Player player, Member target, List<? extends Member> otherAccounts,
            MenuInventory menuInventory) {
        this(player, target, 1, otherAccounts, menuInventory);
    }

    public OthersUserInventory(Player player, Member target, int page, List<? extends Member> otherAccounts,
            MenuInventory menuInventory) {
        super("§7Contas " + target.getName(), 5);

        int pageStart = 0;
        int pageEnd = BukkitConst.ITEMS_PER_PAGE;

        if (page > 1) {
            pageStart = ((page - 1) * BukkitConst.ITEMS_PER_PAGE);
            pageEnd = (page * BukkitConst.ITEMS_PER_PAGE);
        }

        if (pageEnd > otherAccounts.size()) pageEnd = otherAccounts.size();

        int w = 10;

        for (int i = pageStart; i < pageEnd; i++) {
            Member member = otherAccounts.get(i);

            ItemBuilder itemBuilder = new ItemBuilder().name(member.getDefaultTag().getColoredName() + member.getName())
                                                       .type(Material.SKULL_ITEM).durability(3).skin(member.getSkin());

            itemBuilder.lore("", "§fCriada em: §7" + CommonConst.DATE_FORMAT.format(member.getFirstLoginAt()),
                    "§fTempo total online: §7" + StringFormat.formatTime(member.getOnlineTime() / 1000));

            if (member.isOnline()) {
                itemBuilder.lore("§fTempo de sessão: §7" + StringFormat.formatTime(member.getSessionTime() / 1000), "",
                        "§aO usuário está online no momento.");
            } else {
                itemBuilder.lore(
                        "§7Visto por último em " + CommonConst.DATE_FORMAT.format(member.getLastLoginAt()) + "",
                        "§8Há " +
                                StringFormat.formatTime((System.currentTimeMillis() - member.getLastLoginAt()) / 1000));
            }

            setItem(w, itemBuilder.build(),
                    clickArgs -> new UserInventory(clickArgs.getPlayer(), member, otherAccounts));

            if (w % 9 == 7) {
                w += 3;
                continue;
            }

            w += 1;
        }

        if (page > 1) {
            setItem(39, new ItemBuilder().type(Material.ARROW).name("§aPágina " + (page - 1)).lore("").build(),
                    clickArgs -> new OthersUserInventory(clickArgs.getPlayer(), target, page - 1, otherAccounts,
                            menuInventory));
        } else if (menuInventory != null) {
            setItem(39, new ItemBuilder().name("§aRetorna para " + menuInventory.getTitle()).formatLore("§7Clique para voltar.").type(Material.ARROW)
                                         .build(), clickArgs -> menuInventory.open(player));
        }

        if (Math.ceil(otherAccounts.size() / BukkitConst.ITEMS_PER_PAGE) + 1 > page) {
            setItem(41, new ItemBuilder().type(Material.ARROW).name("§aPágina " + (page + 1)).lore("").build(),
                    clickArgs -> new OthersUserInventory(clickArgs.getPlayer(), target, page + 1, otherAccounts,
                            menuInventory));
        }

        open(player);
    }
}
