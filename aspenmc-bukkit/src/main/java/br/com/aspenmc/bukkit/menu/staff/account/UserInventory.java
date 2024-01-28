package br.com.aspenmc.bukkit.menu.staff.account;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bukkit.utils.item.ItemBuilder;
import br.com.aspenmc.bukkit.utils.menu.MenuInventory;
import br.com.aspenmc.entity.sender.member.Member;
import br.com.aspenmc.entity.sender.member.MemberVoid;
import com.mongodb.client.model.Filters;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class UserInventory extends MenuInventory {

    private Member target;
    ;
    private List<? extends Member> otherAccounts;

    public UserInventory(Player player, Member target) {
        this(player, target, null);
    }

    public UserInventory(Player player, Member target, List<? extends Member> otherAccounts) {
        super("§7Conta de " + target.getName(), 5);

        this.target = target;
        this.otherAccounts = otherAccounts;

        setItem(13, new ItemBuilder().name("§a" + target.getName()).type(Material.SKULL_ITEM).durability(3)
                                     .skin(target.getSkin())
                                     .formatLore("\n§fRank: " + target.getDefaultTag().getColoredName()).build());

        createItemAccounts(29, otherAccounts == null ? -1 : otherAccounts.size());
        setItem(30, new ItemBuilder().name("§aTodos os ranks").formatLore("§7Clique para ver todos os ranks.")
                                     .type(Material.ENCHANTED_BOOK).build());
        setItem(31, new ItemBuilder().name("§aHistórico de punições").formatLore("§7Clique para ver todas as punições.")
                                     .type(Material.REDSTONE).build(),
                clickArgs -> new UserPunishHistoryInventory(player, target, this));

        open(player);
    }

    private void createItemAccounts(int slot, int accounts) {
        setItem(slot, new ItemBuilder().name("§aTodas as contas").formatLore(
                accounts == 0 ? "§7Nenhuma outra conta encontrada." :
                        "§7O membro possui " + (accounts == -1 ? "..." : accounts + "") +
                                " conta(s) cadastrada(s) no servidor.",
                "§7Clique " + "para" + " ver todas as " + "contas.").type(Material.BOOK).build(), clickArgs -> {
            if (accounts == -1) return;

            new OthersUserInventory(clickArgs.getPlayer(), target, otherAccounts, this);
        });

        if (accounts == -1) {
            CommonPlugin.getInstance().getMemberService()
                        .getMembers(Filters.eq("ipAddress", target.getIpAddress()), MemberVoid.class)
                        .whenComplete((members, throwable) -> {
                            if (throwable != null) {
                                throwable.printStackTrace();
                                return;
                            }

                            otherAccounts = members;
                            createItemAccounts(slot, members.size() - 1);
                        });
        }
    }
}
