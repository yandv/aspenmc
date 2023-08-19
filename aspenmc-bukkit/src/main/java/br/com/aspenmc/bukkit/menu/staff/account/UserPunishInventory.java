package br.com.aspenmc.bukkit.menu.staff.account;

import br.com.aspenmc.BukkitConst;
import br.com.aspenmc.CommonConst;
import br.com.aspenmc.bukkit.utils.item.ItemBuilder;
import br.com.aspenmc.bukkit.utils.menu.MenuInventory;
import br.com.aspenmc.bukkit.utils.menu.MenuItem;
import br.com.aspenmc.entity.Member;
import br.com.aspenmc.punish.PunishType;
import br.com.aspenmc.utils.string.StringFormat;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class UserPunishInventory extends MenuInventory {

    public UserPunishInventory(Player player, Member member, PunishType punishType, MenuInventory backInventory) {
        this(player, member, punishType, 1, backInventory);
    }

    public UserPunishInventory(Player player, Member member, PunishType punishType, int page,
            MenuInventory backInventory) {
        super("§7" + StringFormat.formatString(punishType), 5);

        int pageStart = 0;
        int pageEnd = BukkitConst.ITEMS_PER_PAGE;

        if (page > 1) {
            pageStart = ((page - 1) * BukkitConst.ITEMS_PER_PAGE);
            pageEnd = (page * BukkitConst.ITEMS_PER_PAGE);
        }

        List<MenuItem> items = member.getPunishConfiguration().getPunishs(punishType).stream()
                                     .sorted((o1, o2) -> Long.compare(o2.getCreatedAt(), o1.getCreatedAt()))
                                     .map(punish -> {
                                         ItemBuilder itemBuilder = new ItemBuilder();

                                         itemBuilder.name("§7#" + punish.getPunishId());
                                         itemBuilder.type(Material.PAPER);
                                         itemBuilder.lore("§fAutor: §7" + punish.getPunisherName(),
                                                 "§fMotivo: §7" + punish.getReason());

                                         if (!punish.hasExpired() && !punish.isRevoked()) {
                                             itemBuilder.lore("§fExpira em: §7" + (punish.isPermanent() ? "Sem prazo" :
                                                     StringFormat.formatTime(
                                                             (punish.getExpiresAt() - System.currentTimeMillis()) /
                                                                     1000)));
                                         }

                                         itemBuilder.lore("§fCriado às: §7" +
                                                 CommonConst.DATE_FORMAT.format(punish.getCreatedAt()), "");

                                         if (punish.isRevoked()) {
                                             itemBuilder.lore("§aPunição revogada pelo " + punish.getAbrogatorName());
                                         } else if (punish.hasExpired()) {
                                             itemBuilder.lore("§aEssa punição expirou.");
                                         } else {
                                             itemBuilder.lore("§cEssa punição está ativa.");
                                         }

                                         return new MenuItem(itemBuilder.build());
                                     }).collect(Collectors.toList());

        if (pageEnd > items.size()) pageEnd = items.size();

        int w = 10;

        for (int i = pageStart; i < pageEnd; i++) {
            setItem(w, items.get(i));

            if (w % 9 == 7) {
                w += 3;
                continue;
            }

            w += 1;
        }

        if (page > 1) {
            setItem(39, new ItemBuilder().type(Material.ARROW).name("§aPágina " + (page - 1)).lore("").build(),
                    clickArgs -> new UserPunishInventory(player, member, punishType, page - 1, backInventory));
        } else if (backInventory != null) {
            setItem(39, new ItemBuilder().name("§aRetorna para " + backInventory.getTitle())
                                         .formatLore("§7Clique para voltar.").type(Material.ARROW).build(),
                    clickArgs -> backInventory.open(player));
        }

        if (Math.ceil(items.size() / BukkitConst.ITEMS_PER_PAGE) + 1 > page) {
            setItem(41, new ItemBuilder().type(Material.ARROW).name("§aPágina " + (page + 1)).lore("").build(),
                    clickArgs -> new UserPunishInventory(player, member, punishType, page + 1, backInventory));
        }

        open(player);
    }
}
