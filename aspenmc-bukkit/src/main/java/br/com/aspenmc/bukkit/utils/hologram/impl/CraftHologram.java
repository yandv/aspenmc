package br.com.aspenmc.bukkit.utils.hologram.impl;

import br.com.aspenmc.bukkit.utils.hologram.Hologram;
import br.com.aspenmc.bukkit.utils.TouchHandler;
import br.com.aspenmc.bukkit.utils.hologram.ViewHandler;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.*;

@Getter
public class CraftHologram implements Hologram {

    public static final double DISTANCE = 0.25D;

    private String displayName;
    private Location location;

    @Setter
    private TouchHandler<Hologram> touchHandler;
    @Setter
    private ViewHandler viewHandler;

    private final List<Hologram> linesBelow;
    private final List<Hologram> linesAbove;

    private EntityArmorStand armorStand;
    private final Set<UUID> showing;
    private final Set<UUID> invisibleTo;

    public CraftHologram(String displayName, Location location, TouchHandler<Hologram> touchHandler, ViewHandler viewHandler) {
        this.displayName = displayName;
        this.location = location;
        this.touchHandler = touchHandler;
        this.viewHandler = viewHandler;

        this.linesBelow = new ArrayList<>();
        this.linesAbove = new ArrayList<>();

        this.showing = new HashSet<>();
        this.invisibleTo = new HashSet<>();

        createEntity();
        Bukkit.getOnlinePlayers().forEach(player -> show(player));
    }

    public CraftHologram(String displayName, Location location) {
        this(displayName, location, EMPTY_TOUCH_HANDLER, ViewHandler.EMPTY);
    }

    @Override
    public Hologram setDisplayName(String displayName) {
        this.displayName = displayName;
        this.armorStand.setCustomName(displayName);
        this.armorStand.setCustomNameVisible(isCustomNameVisible());
        return this;
    }

    @Override
    public Hologram teleport(Location location) {
        this.location = location;
        this.armorStand.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(),
                                    location.getPitch());

        for (Player player : Bukkit.getOnlinePlayers()) {
            hide(player);
            show(player);
        }

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
        CraftHologram hologram = new CraftHologram(line, getLocation().clone().add(0.0D, (getLinesBelow().size() + 1) *
                                                                                         DISTANCE, 0.0D));

        hologram.setTouchHandler(getTouchHandler());
        hologram.setViewHandler(getViewHandler());
        this.linesAbove.add(hologram);
        return this;
    }

    @Override
    public Hologram addLineBelow(String line) {
        CraftHologram hologram = new CraftHologram(line, getLocation().clone().subtract(0.0D,
                                                                                        (getLinesBelow().size() + 1) *
                                                                                        DISTANCE, 0.0D));

        hologram.setTouchHandler(getTouchHandler());
        hologram.setViewHandler(getViewHandler());

        this.linesBelow.add(hologram);
        return this;
    }

    @Override
    public boolean hasTouchHandler() {
        return getTouchHandler() != null;
    }

    @Override
    public boolean hasViewHandler() {
        return getViewHandler() != null;
    }

    @Override
    public Hologram hide(Player player) {
        if (!showing.contains(player.getUniqueId())) {
            return this;
        }

        showing.remove(player.getUniqueId());
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;

        playerConnection.sendPacket(new PacketPlayOutEntityDestroy(armorStand.getId()));
        linesBelow.forEach(hologram -> hologram.hide(player));
        linesAbove.forEach(hologram -> hologram.hide(player));
        return this;
    }

    @Override
    public Hologram show(Player player) {
        if (showing.contains(player.getUniqueId())) {
            return this;
        }

        showing.add(player.getUniqueId());

        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;

        playerConnection.sendPacket(new PacketPlayOutSpawnEntityLiving(armorStand));
        linesBelow.forEach(hologram -> hologram.show(player));
        linesAbove.forEach(hologram -> hologram.show(player));
        return this;
    }

    public void block(Player player) {
        invisibleTo.add(player.getUniqueId());
        hide(player);
    }

    public void unblock(Player player) {
        invisibleTo.remove(player.getUniqueId());
    }

    public boolean isBlocked(Player player) {
        return invisibleTo.contains(player.getUniqueId());
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public boolean isHiddenForPlayer(Player player) {
        return !showing.contains(player.getUniqueId());
    }

    @Override
    public int getEntityId() {
        return armorStand == null ? -1 : armorStand.getId();
    }

    public boolean isCustomNameVisible() {
        return (displayName != null && !displayName.isEmpty());
    }

    private void createEntity() {
        armorStand = new EntityArmorStand(((CraftWorld) location.getWorld()).getHandle());

        armorStand.setInvisible(true);
        armorStand.setGravity(false);
        armorStand.setCustomName(displayName);
        armorStand.setCustomNameVisible(isCustomNameVisible());

        armorStand.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(),
                               location.getPitch());
    }
}
