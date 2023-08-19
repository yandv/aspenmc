package br.com.aspenmc.bukkit.menu.staff;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bukkit.utils.item.ItemBuilder;
import br.com.aspenmc.bukkit.utils.menu.MenuInventory;
import br.com.aspenmc.bukkit.utils.menu.MenuItem;
import br.com.aspenmc.bukkit.utils.menu.click.MenuClickHandler;
import br.com.aspenmc.entity.Member;
import br.com.aspenmc.language.Language;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class YourPreferencesInventory extends MenuInventory {

    public YourPreferencesInventory(Player player) {
        super(Language.getLanguage(player.getUniqueId()).t("menu.staff-your-preferences.title"), 4);

        Member member = CommonPlugin.getInstance().getMemberManager().getMemberById(player.getUniqueId()).orElse(null);

        if (member == null) return;

        createMenuItem("Entrar no admin", "§7Entrar no modo administrador ao logar em um servidor.", Material.PAPER, 10,
                member.getPreferencesConfiguration()::getAdminOnLogin, (short) 2, v -> {

                    short nextValue = (short) (member.getPreferencesConfiguration().getAdminOnLogin() == 2 ? 0 :
                            member.getPreferencesConfiguration().getAdminOnLogin() + 1);

                    member.getPreferencesConfiguration().setAdminOnLogin(nextValue);
                });

        createMenuItem("Remover itens no Admin", "§7Remover todos os itens do seu inventário ao entrar no modo admin.",
                Material.PAPER, 11, member.getPreferencesConfiguration()::isAdminRemoveItems, v -> {
                    member.getPreferencesConfiguration()
                          .setAdminRemoveItems(!member.getPreferencesConfiguration().isAdminRemoveItems());
                });

        createMenuItem("Falar no StaffChat", "§7Falar em um canal de voz somente para a equipe.", Material.PAPER, 12,
                member.getPreferencesConfiguration()::isStaffChatEnabled, v -> {
                    member.getPreferencesConfiguration()
                          .setStaffChatEnabled(!member.getPreferencesConfiguration().isStaffChatEnabled());
                });

        createMenuItem("Ver no StaffChat", "§7Ver as mensagens que são trocadas no StaffChat.", Material.PAPER, 13,
                member.getPreferencesConfiguration()::isSeeingStaffChatEnabled, v -> {
                    member.getPreferencesConfiguration()
                          .setSeeingStaffChatEnabled(!member.getPreferencesConfiguration().isSeeingStaffChatEnabled());
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

    public void createMenuItem(String name, String lore, Material material, int slot, Supplier<Short> supplier,
            short maxValue, Consumer<Void> consumer) {
        short preferenceEnabled = supplier.get();

        MenuClickHandler clickHandler = clickArgs -> {
            consumer.accept(null);
            createMenuItem(name, lore, material, slot, supplier, maxValue, consumer);
        };

        String text = (preferenceEnabled == 0 ? "§a" : preferenceEnabled == 1 ? "§e" : "§c") + name;

        setItem(slot, new MenuItem(new ItemBuilder().name(text).formatLore(lore).type(material), clickHandler));
        setItem(slot + 9, new MenuItem(new ItemBuilder().name(text).formatLore(lore, "",
                                                                (preferenceEnabled == maxValue - 1 ?
                                                                        "§cClique para " + "ativar." : "§aClique " +
                                                                        "para" + " desativar."))
                                                        .type(material).type(Material.INK_SACK).durability(
                        preferenceEnabled == 0 ? 10 : preferenceEnabled == 1 ? 14 : 8), clickHandler));
    }
}
