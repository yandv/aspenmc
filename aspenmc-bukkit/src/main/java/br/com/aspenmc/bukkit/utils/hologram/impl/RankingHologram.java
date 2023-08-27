package br.com.aspenmc.bukkit.utils.hologram.impl;

import br.com.aspenmc.bukkit.BukkitCommon;
import br.com.aspenmc.bukkit.utils.hologram.Hologram;
import lombok.Getter;
import org.bukkit.Location;

import java.util.*;

@Getter
public class RankingHologram {

    private Location location;
    private List<String> lineList;
    private Loader loader;

    private Map<UUID, Integer> playerPageMap;


    public RankingHologram(Location location, Loader loader, String defaultText, String... texts) {
        this.location = location;
        this.lineList = new ArrayList<>();
        this.loader = loader;
        this.lineList = this.loader.load();

        this.playerPageMap = new HashMap<>();

        Hologram hologram = new CraftHologram(texts[0], location);

        hologram.setViewHandler((player, text) -> {
            if (text.startsWith("text-")) {
                int currentPage = this.playerPageMap.getOrDefault(player.getUniqueId(), 1);
                int index = (currentPage - 1) * 10 + Integer.parseInt(text.replace("text-", ""));

                if (index < this.lineList.size()) {
                    return this.lineList.get(index);
                } else {
                    return defaultText;
                }
            }

            return text;
        });

        hologram.setTouchHandler((type, player, right) -> {
            int currentPage = playerPageMap.getOrDefault(player.getUniqueId(), 1);

            if (currentPage == 10) {
                playerPageMap.remove(player.getUniqueId());
            } else {
                playerPageMap.put(player.getUniqueId(), currentPage + 1);
            }

            hologram.getLinesBelow().stream().filter(h -> h.getDisplayName().startsWith("text-"))
                    .forEach(h -> h.updateTitle(player));
        });

        if (texts.length > 1) {
            for (int i = 1; i < texts.length; i++) {
                hologram.addLineBelow(texts[i]);
            }

            hologram.addLineBelow("");
        }

        for (int i = 0; i < 10; i++) {
            hologram.addLineBelow("text-" + i);
        }

        hologram.addLineBelow("");
        hologram.addLineBelow("§aClique para avançar.");

        BukkitCommon.getInstance().getHologramManager().loadHologram(hologram);
    }

    public void update() {
        this.lineList = this.loader.load();
    }

    public interface Loader {

        List<String> load();
    }
}
