package br.com.aspenmc.bukkit.manager;

import br.com.aspenmc.bukkit.utils.hologram.Hologram;
import lombok.Getter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Getter
public class HologramManager {

    private Map<Integer, Hologram> hologramMap;

    public HologramManager() {
        this.hologramMap = new HashMap<>();
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
