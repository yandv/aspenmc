package br.com.aspenmc.bukkit.utils.character.impl;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bukkit.BukkitCommon;
import br.com.aspenmc.bukkit.utils.TouchHandler;
import br.com.aspenmc.bukkit.utils.character.Character;
import br.com.aspenmc.bukkit.utils.hologram.Hologram;
import br.com.aspenmc.entity.member.Skin;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
public class DefaultCharacter implements Character {

    private Location location;

    private TouchHandler<Character> touchHandler;

    private boolean collision;

    private Skin skin;
    private EntityPlayer entityPlayer;
    private EntityBat entityBat;

    private Hologram hologram;

    private final Set<UUID> showing;

    public DefaultCharacter(Location location, boolean collision, TouchHandler<Character> touchHandler, Skin skin,
            Hologram hologram) {
        this.location = location;
        this.touchHandler = touchHandler;
        this.skin = skin;
        this.hologram = hologram;

        if (this.hologram != null) {
            this.hologram.teleport(location.clone().add(0, 0.1, 0));
        }

        this.showing = new HashSet<>();

        createCharacter();
        Bukkit.getOnlinePlayers().forEach(this::show);
    }

    public DefaultCharacter(Location location, TouchHandler<Character> touchHandler, Skin skin, Hologram hologram) {
        this(location, false, touchHandler, skin, hologram);
    }

    public DefaultCharacter(Location location, TouchHandler<Character> touchHandler, String name, Hologram hologram) {
        this(location, false, touchHandler, CommonPlugin.getInstance().getSkinService().loadData(name)
                                                        .orElse(CommonPlugin.getInstance().getDefaultSkin()), hologram);
    }

    public DefaultCharacter(Location location, TouchHandler<Character> touchHandler) {
        this(location, touchHandler, CommonPlugin.getInstance().getDefaultSkin(), null);
    }

    @Override
    public void setCollision(boolean collision) {
        this.collision = collision;
    }

    @Override
    public void show(Player player) {
        if (showing.contains(player.getUniqueId())) {
            return;
        }

        showing.add(player.getUniqueId());
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;

        connection.sendPacket(
                new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityPlayer));
        connection.sendPacket(new PacketPlayOutNamedEntitySpawn(entityPlayer));
        connection.sendPacket(
                new PacketPlayOutEntityMetadata(entityPlayer.getId(), entityPlayer.getDataWatcher(), true));
        connection.sendPacket(
                new PacketPlayOutEntityHeadRotation(entityPlayer, (byte) ((location.getYaw() * 256f) / 360f)));
        connection.sendPacket(new PacketPlayOutSpawnEntityLiving(entityBat));
        connection.sendPacket(new PacketPlayOutAttachEntity(0, entityBat, entityPlayer));

        DataWatcher watcher = entityPlayer.getDataWatcher();
        watcher.watch(10, (byte) 127);
        connection.sendPacket(new PacketPlayOutEntityMetadata(entityPlayer.getId(), watcher, true));

        Bukkit.getScheduler().scheduleSyncDelayedTask(BukkitCommon.getInstance(), () -> connection.sendPacket(
                        new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER,
                                entityPlayer)),
                40L);
    }

    @Override
    public void hide(Player player) {
        if (!showing.contains(player.getUniqueId())) {
            return;
        }

        showing.remove(player.getUniqueId());

        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;

        playerConnection.sendPacket(
                new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entityPlayer));
        playerConnection.sendPacket(new PacketPlayOutEntityDestroy(entityPlayer.getId()));
        playerConnection.sendPacket(new PacketPlayOutEntityDestroy(entityBat.getId()));
    }

    @Override
    public boolean isShowing(Player player) {
        return showing.contains(player.getUniqueId());
    }

    @Override
    public void teleport(Location location) {
        this.location = location;
        entityPlayer.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(),
                location.getPitch());
        entityBat.setLocation(location.getX() * 32.0, location.getY() * 32.0, location.getZ() * 32.0, 0.0f, 0.0f);

        for (Player player : Bukkit.getOnlinePlayers()) {
            hide(player);
            show(player);
        }

        if (this.hologram != null) {
            this.hologram.teleport(location.clone().add(0, 0.1, 0));
        }
    }

    @Override
    public Character setHologram(Hologram hologram) {
        this.hologram = hologram;
        this.hologram.teleport(location.clone().add(0, 0.1, 0));
        return this;
    }

    @Override
    public boolean hasCollision() {
        return collision;
    }

    @Override
    public int getEntityId() {
        return entityPlayer.getId();
    }

    private void createCharacter() {
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();

        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "§8[" + Character.CODE_CREATOR.random() + "]");

        entityPlayer = new EntityPlayer(server, world, gameProfile, new PlayerInteractManager(world));

        if (skin != null) {
            gameProfile.getProperties().clear();
            PropertyMap propertyMap = new PropertyMap();
            propertyMap.put("textures", new Property("textures", skin.getValue(), skin.getSignature()));
            gameProfile.getProperties().putAll(propertyMap);
        }

        entityPlayer.getBukkitEntity().setRemoveWhenFarAway(false);
        entityPlayer.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(),
                location.getPitch());
        entityPlayer.setInvisible(false);

        entityBat = new EntityBat(world);
        entityBat.setLocation(location.getX() * 32.0, location.getY() * 32.0, location.getZ() * 32.0, 0.0f, 0.0f);
        entityBat.setInvisible(true);
    }

    public void setSkin(Skin skin) {
        this.skin = skin;
        Bukkit.getOnlinePlayers().forEach(this::hide);
        createCharacter();
        Bukkit.getOnlinePlayers().forEach(this::show);
    }
}
