package br.com.aspenmc.bukkit.utils.hologram.impl;

import br.com.aspenmc.bukkit.utils.TouchHandler;
import br.com.aspenmc.bukkit.utils.hologram.Hologram;
import br.com.aspenmc.bukkit.utils.hologram.ViewHandler;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CraftHologram extends CraftSingleHologram {

    private final List<Hologram> linesBelow = new ArrayList<>();
    private final List<Hologram> linesAbove = new ArrayList<>();

    public CraftHologram(String displayName, Location location, TouchHandler<Hologram> touchHandler,
            ViewHandler viewHandler) {
        super(displayName, location, touchHandler, viewHandler);
    }

    public CraftHologram(String displayName, Location location) {
        this(displayName, location, EMPTY_TOUCH_HANDLER, ViewHandler.EMPTY);
    }

    public CraftHologram(String displayName, Location location, ViewHandler viewHandler) {
        this(displayName, location, EMPTY_TOUCH_HANDLER, viewHandler);
    }

    public CraftHologram(String displayName) {
        this(displayName, new Location(Bukkit.getWorlds().stream().findFirst().orElse(null), 0, 0, 0));
    }

    @Override
    public Hologram teleport(Location location) {
        super.teleport(location);

        for (int i = 0; i < this.linesBelow.size(); i++) {
            this.linesBelow.get(i).teleport(location.clone().subtract(0.0D, (i + 1) * DISTANCE, 0.0D));
        }

        for (int i = 0; i < this.linesAbove.size(); i++) {
            this.linesAbove.get(i).teleport(location.clone().add(0.0D, (i + 1) * DISTANCE, 0.0D));
        }
        return this;
    }

    @Override
    public Hologram addLineAbove(String line) {
        this.linesAbove.add(new CraftSingleHologram(line,
                getLocation().clone().add(0.0D, (getLinesAbove().size() + 1) * DISTANCE, 0.0D), getTouchHandler(),
                getViewHandler()));
        return this;
    }

    @Override
    public Hologram addLineBelow(String line) {
        this.linesBelow.add(new CraftSingleHologram(line,
                getLocation().clone().subtract(0.0D, (getLinesBelow().size() + 1) * DISTANCE, 0.0D), getTouchHandler(),
                getViewHandler()));
        return this;
    }

    @Override
    public Hologram hide(Player player) {
        super.hide(player);
        linesBelow.forEach(hologram -> hologram.hide(player));
        linesAbove.forEach(hologram -> hologram.hide(player));
        return this;
    }

    @Override
    public Hologram show(Player player) {
        super.show(player);
        linesBelow.forEach(hologram -> hologram.show(player));
        linesAbove.forEach(hologram -> hologram.show(player));
        return this;
    }
}
