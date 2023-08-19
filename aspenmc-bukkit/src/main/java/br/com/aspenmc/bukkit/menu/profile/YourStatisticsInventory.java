package br.com.aspenmc.bukkit.menu.profile;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bukkit.utils.item.ItemBuilder;
import br.com.aspenmc.bukkit.utils.menu.MenuInventory;
import br.com.aspenmc.entity.member.status.Status;
import br.com.aspenmc.entity.member.status.StatusField;
import br.com.aspenmc.entity.member.status.StatusType;
import br.com.aspenmc.language.Language;
import br.com.aspenmc.utils.string.StringFormat;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class YourStatisticsInventory extends MenuInventory {

    public YourStatisticsInventory(Player player, MenuInventory backInventory) {
        super(Language.getLanguage(player.getUniqueId()).t("menu.your-statistics.title"), 3);

        int slot = 10;

        for (StatusType statusType : StatusType.values()) {
            int currentSlot = slot++;

            if (CommonPlugin.getInstance().getStatusManager().hasLoadedStatus(player.getUniqueId(), statusType)) {
                createItem(currentSlot,
                        CommonPlugin.getInstance().getStatusManager().getStatusById(player.getUniqueId(), statusType),
                        player);
            } else {
                CommonPlugin.getInstance().getStatusService().getStatusById(player.getUniqueId(), statusType)
                            .whenComplete((status, throwable) -> {
                                if (status != null) {
                                    createItem(currentSlot, status, player);

                                    if (player.isOnline()) {
                                        CommonPlugin.getInstance().getStatusManager().loadStatus(status);
                                    }
                                }
                            });

                createItem(currentSlot, new Status(player.getUniqueId(), statusType), player);
            }
        }

        if (backInventory != null) {
            setItem(21, new ItemBuilder().name("§aRetorna para " + backInventory.getTitle())
                                         .formatLore("§7Clique para voltar.").type(Material.ARROW).build(),
                    clickArgs -> backInventory.open(player));
        }

        open(player);
    }

    private void createItem(int slot, Status status, Player player) {
        ItemBuilder itemBuilder = new ItemBuilder();

        switch (status.getStatusType()) {
        case HG: {
            itemBuilder.type(Material.MUSHROOM_SOUP);
            itemBuilder.name("§aHG");
            itemBuilder.lore("§fVitórias: §7" + status.get(StatusField.WINS),
                    "§fKills: §7" + status.get(StatusField.KILLS), "§fMortes: §7" + status.get(StatusField.DEATHS), "",
                    "§fLiga: " + status.getLeague().getColoredSymbol());
            break;
        }
        case ARENA:
        case FPS:
            itemBuilder.type(status.getStatusType() == StatusType.FPS ? Material.GLASS : Material.IRON_CHESTPLATE);
            itemBuilder.name("§a" + StringFormat.formatString(status.getStatusType().name()));
            itemBuilder.lore("§fKills: §7" + status.get(StatusField.KILLS),
                    "§fMortes: §7" + status.get(StatusField.DEATHS),
                    "§fKillstreak: §7" + status.get(StatusField.KILLSTREAK),
                    "§fMáximo killstreak: §7" + status.get(StatusField.MAX_KILLSTREAK), "",
                    "§fLiga: " + status.getLeague().getColoredSymbol());
            break;
        case LAVA:
            itemBuilder.type(Material.LAVA_BUCKET);
            itemBuilder.name("§aLava");
            itemBuilder.lore("§4Impossível: ", "  §fCompleto: §70/0", "  §fMelhor tempo: §7999m", "");
            itemBuilder.lore("§cDíficil: ", "  §fCompleto: §70/0", "  §fMelhor tempo: §7999m", "");
            itemBuilder.lore("§6Médio: ", "  §fCompleto: §70/0", "  §fMelhor tempo: §7999m", "");
            itemBuilder.lore("§aFácil: ", "  §fCompleto: §70/0", "  §fMelhor tempo: §7999m", "",
                    "§fLiga: " + status.getLeague().getColoredSymbol());
            break;
        }

//        player.sendMessage(status.getStatusType().name() + " " + slot);
        setItem(slot, itemBuilder.build());
    }
}
