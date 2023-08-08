package br.com.aspenmc.bukkit.menu.profile;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bukkit.utils.item.ItemBuilder;
import br.com.aspenmc.bukkit.utils.menu.MenuInventory;
import br.com.aspenmc.bukkit.utils.menu.MenuItem;
import br.com.aspenmc.bukkit.utils.menu.click.MenuClickHandler;
import br.com.aspenmc.entity.Member;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class YourPreferencesInventory extends MenuInventory {

    public YourPreferencesInventory(Player player) {
        super("§7Suas estatísticas", 4);

        Member member = CommonPlugin.getInstance().getMemberManager().getMemberById(player.getUniqueId()).orElse(null);

        if (member == null) return;

        createMenuItem("Mensagens privadas", "§7Receber ou não mensagens privadas pelo chat de texto.", Material.PAPER,
                10, member.getPreferencesConfiguration()::isTellEnabled, v -> {
                    member.getPreferencesConfiguration()
                          .setTellEnabled(!member.getPreferencesConfiguration().isTellEnabled());
                });

        createMenuItem("Mensagens de chat", "§7Receber ou não mensagens de todos os jogadores pelo chat de texto.",
                Material.PAPER, 11, member.getPreferencesConfiguration()::isChatEnabled, v -> {
                    member.getPreferencesConfiguration()
                          .setChatEnabled(!member.getPreferencesConfiguration().isChatEnabled());
                });

        createMenuItem("Convite para clans", "§7Receber ou não convites para entrar em clans.",
                Material.PAPER, 12, member.getPreferencesConfiguration()::isClanInvitesEnabled, v -> {
                    member.getPreferencesConfiguration()
                          .setClanInvitesEnabled(!member.getPreferencesConfiguration().isClanInvitesEnabled());
                });

        open(player);
    }

    public void createMenuItem(String name, String lore, Material material, int slot, Supplier<Boolean> supplier,
            Consumer<Void> consumer) {
        boolean preferenceEnabled = supplier.get();

        MenuClickHandler clickHandler = clickArgs -> {
            consumer.accept(null);
            createMenuItem(name, lore, material, slot, supplier, consumer);
        };

        setItem(slot, new MenuItem(
                new ItemBuilder().name((preferenceEnabled ? "§a" : "§c") + name).formatLore(lore).type(material),
                clickHandler));
        setItem(slot + 9, new MenuItem(new ItemBuilder().name((preferenceEnabled ? "§a" : "§c") + name)
                                                        .formatLore(lore, "",
                                                                (preferenceEnabled ? "§aClique para desativar." :
                                                                        "§cClique para ativar.")).type(material)
                                                        .type(Material.INK_SACK).durability(preferenceEnabled ? 10 : 8),
                clickHandler));
    }
}
