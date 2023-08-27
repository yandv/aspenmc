package br.com.aspenmc.bukkit.menu.skin;

import br.com.aspenmc.BukkitConst;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bukkit.entity.BukkitMember;
import br.com.aspenmc.bukkit.event.player.PlayerChangedSkinEvent;
import br.com.aspenmc.bukkit.utils.PlayerAPI;
import br.com.aspenmc.bukkit.utils.item.ItemBuilder;
import br.com.aspenmc.bukkit.utils.menu.MenuInventory;
import br.com.aspenmc.language.Language;
import br.com.aspenmc.packet.type.member.skin.SkinChangeRequest;
import br.com.aspenmc.packet.type.member.skin.SkinChangeResponse;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SkinLibraryInventory extends MenuInventory {

    private static final List<Skin> LIBRARY_SKINS = new ArrayList<>();

    static {
        LIBRARY_SKINS.add(new Skin("viado", CommonPlugin.getInstance().getDefaultSkin(), Gender.MALE));
    }

    public SkinLibraryInventory(Player player, MenuInventory backInventory) {
        this(player, 1, backInventory);
    }

    public SkinLibraryInventory(Player player, int page, MenuInventory backInventory) {
        super(Language.getLanguage(player.getUniqueId()).t("menu.skin-library.title"), 5);

        int pageStart = 0;
        int pageEnd = BukkitConst.ITEMS_PER_PAGE;

        if (page > 1) {
            pageStart = ((page - 1) * BukkitConst.ITEMS_PER_PAGE);
            pageEnd = (page * BukkitConst.ITEMS_PER_PAGE);
        }

        if (pageEnd > LIBRARY_SKINS.size()) pageEnd = LIBRARY_SKINS.size();

        int w = 10;

        for (int i = pageStart; i < pageEnd; i++) {
            Skin skin = LIBRARY_SKINS.get(i);

            ItemBuilder itemBuilder = ItemBuilder.newItemBuilder(Material.SKULL_ITEM, (short) 3)
                                                 .name("§a" + skin.skinName)
                                                 .lore("", "§7Gênero: §f" + skin.gender.getName(), "",
                                                         "§aClique para selecionar.");


            setItem(w, itemBuilder.build(), clickArgs -> changePlayerSkin(clickArgs.getPlayer(), skin));

            if (w % 9 == 7) {
                w += 3;
                continue;
            }

            w += 1;
        }

        if (page > 1) {
            setItem(39, new ItemBuilder().type(Material.ARROW).name("§aPágina " + (page - 1)).lore("").build(),
                    clickArgs -> new SkinLibraryInventory(clickArgs.getPlayer(), page - 1, backInventory));
        } else if (backInventory != null) {
            setItem(39, new ItemBuilder().name("§aRetorna para " + backInventory.getTitle())
                                         .formatLore("§7Clique para voltar.").type(Material.ARROW).build(),
                    clickArgs -> backInventory.open(player));
        }

        if (Math.ceil(LIBRARY_SKINS.size() / BukkitConst.ITEMS_PER_PAGE) + 1 > page) {
            setItem(41, new ItemBuilder().type(Material.ARROW).name("§aPágina " + (page + 1)).lore("").build(),
                    clickArgs -> new SkinLibraryInventory(clickArgs.getPlayer(), page + 1, backInventory));
        }

        open(player);
    }

    private void changePlayerSkin(Player player, Skin skin) {
        CommonPlugin.getInstance().getMemberManager().getMemberById(player.getUniqueId(), BukkitMember.class)
                    .ifPresent(member -> {

                        CommonPlugin.getInstance().getPacketManager().waitPacket(SkinChangeResponse.class,
                                CommonPlugin.getInstance().getPacketManager()
                                            .sendPacket(new SkinChangeRequest(player.getUniqueId(), skin.profieSkin)),
                                500, packet -> {
                                    if (packet == null) {
                                        member.sendMessage(member.t("skin-change-error"));
                                        return;
                                    }

                                    if (packet.getSkinResult() == SkinChangeResponse.SkinResult.SUCCESS) {
                                        PlayerAPI.changePlayerSkin(player, skin.profieSkin, true);
                                        Bukkit.getPluginManager()
                                              .callEvent(new PlayerChangedSkinEvent(member, skin.profieSkin));
                                        member.setPlayerSkin(member.getName());
                                        member.setSkin(skin.profieSkin);
                                        member.sendMessage(member.t("skin-changed-to-default-sucess", "%player%",
                                                member.getName()));
                                    } else {
                                        member.sendMessage("§c" + packet.getErrorMessage());
                                    }
                                });


                        player.closeInventory();
                    });
    }

    @AllArgsConstructor
    public static class Skin {

        private String skinName;
        private br.com.aspenmc.entity.member.Skin profieSkin;
        private Gender gender;
    }

    public enum Gender {
        FEMALE, MALE;

        private String getName() {
            return this == FEMALE ? "Feminino" : "Masculino";
        }
    }
}
