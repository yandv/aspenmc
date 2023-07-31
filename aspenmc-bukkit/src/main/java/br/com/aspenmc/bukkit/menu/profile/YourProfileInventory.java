package br.com.aspenmc.bukkit.menu.profile;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bukkit.menu.skin.SkinInventory;
import br.com.aspenmc.bukkit.utils.item.ItemBuilder;
import br.com.aspenmc.bukkit.utils.menu.MenuInventory;
import br.com.aspenmc.entity.Member;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class YourProfileInventory extends MenuInventory {

    public YourProfileInventory(Player player) {
        super("§7Seu perfil", 5);

        Member member = CommonPlugin.getInstance().getMemberManager().getMemberById(player.getUniqueId()).orElse(null);

        if (member == null) return;

        setTitle(member.t("menu.your-profile-inventory.skin-item.name", "§7Seu perfil"));
        setItem(13, new ItemBuilder().name("§a" + member.getName()).type(Material.SKULL_ITEM).durability(3)
                                     .skin(member.getSkin())
                                     .formatLore("\n§fRank: " + member.getDefaultTag().getColoredName()).build());

        setItem(29, new ItemBuilder()
                .name(member.t("menu.your-profile-inventory.statistics-item.name", "§aSuas estatísticas"))
                .type(Material.PAPER).build(), clickArgs -> new YourStatisticsInventory(clickArgs.getPlayer()));
        setItem(30, new ItemBuilder()
                .name(member.t("menu.your-profile-inventory.preferences-item.name", "§aSuas preferências"))
                .type(Material.DIODE).build(), clickArgs -> new YourPreferencesInventory(clickArgs.getPlayer()));
        setItem(31, new ItemBuilder().name(member.t("menu.your-profile-inventory.language-item.name", "§aSeu idioma"))
                                     .type(Material.SKULL_ITEM).durability(3)
                                     .skinURL("b04831f7a7d8f624c9633996e3798edad49a5d9bcd18ecf75bfae66be48a0a6b")
                                     .build(), clickArgs -> new YourLanguageInventory(clickArgs.getPlayer()));
        setItem(32, new ItemBuilder().name(member.t("menu.your-profile-inventory.skin-item.name", "§aSua pele"))
                                     .type(Material.SKULL_ITEM).durability(3).build(),
                clickArgs -> new SkinInventory(clickArgs.getPlayer()));

        open(player);
    }
}
