package br.com.aspenmc.bukkit.manager;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bukkit.utils.hologram.Hologram;
import br.com.aspenmc.bukkit.utils.hologram.impl.RankingHologram;
import lombok.Getter;

import java.util.*;

@Getter
public class HologramManager {

    private Map<Integer, Hologram> hologramMap;
    private Set<RankingHologram> rankingHologramSet;

    public HologramManager() {
        this.hologramMap = new HashMap<>();
        this.rankingHologramSet = new HashSet<>();

        CommonPlugin.getInstance().getPluginPlatform().runAsyncTimer(() -> {
            this.rankingHologramSet.forEach(RankingHologram::update);
        }, 20L * 60L * 5L, 20L * 60L * 5L);
    }

    public RankingHologram loadHologram(RankingHologram rankingHologram) {
        this.rankingHologramSet.add(rankingHologram);
        return rankingHologram;
    }


    public <T extends Hologram> Hologram loadHologram(T hologram) {
        this.hologramMap.put(hologram.getEntityId(), hologram);
        return hologram;
    }

    public Hologram getHologramById(int entityId) {
        return this.hologramMap.get(entityId);
    }

    public Collection<? extends Hologram> getHolograms() {
        return this.hologramMap.values();
    }
}
