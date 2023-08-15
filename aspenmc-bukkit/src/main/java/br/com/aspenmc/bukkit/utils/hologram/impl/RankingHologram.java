package br.com.aspenmc.bukkit.utils.hologram.impl;

import br.com.aspenmc.bukkit.BukkitCommon;
import br.com.aspenmc.bukkit.utils.hologram.Hologram;

import java.util.ArrayList;
import java.util.List;

public class RankingHologram {

    private List<String> lineList;
    private Loader loader;


    public RankingHologram(Loader loader, String... texts) {
        this.lineList = new ArrayList<>();
        this.loader = loader;
        this.loader.load();

        Hologram hologram = new CraftHologram(texts[0]).setViewHandler((player, text) -> {
            if (text.startsWith("text-")) {
                int index = Integer.parseInt(text.replace("text-", ""));

                if (index < this.lineList.size()) {
                    return this.lineList.get(index);
                } else {
                    return "§7Ninguém §8- §7Nada...";
                }
            }

            return text;
        });

        if (texts.length > 1) {
            for (int i = 1; i < texts.length; i++) {
                hologram.addLineBelow(texts[i]);
            }
        }

        for (int i = 1; i <= 10; i++) {
            hologram.addLineBelow("text-" + i);
        }

        BukkitCommon.getInstance().getHologramManager().loadHologram(hologram);
    }

    public interface Loader {

        List<String> load();
    }
}
