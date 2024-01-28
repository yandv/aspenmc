package br.com.aspenmc.bukkit.utils;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bukkit.BukkitCommon;
import br.com.aspenmc.entity.sender.member.Skin;
import br.com.aspenmc.utils.ProtocolVersion;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.*;
import com.comphenix.protocol.wrappers.EnumWrappers.Difficulty;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.google.common.cache.CacheLoader;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Level;

public class PlayerAPI {

    public static void changePlayerName(Player player, String name) {
        changePlayerName(player, name, true);
    }

    public static void changePlayerName(Player player, String name, boolean respawn) {
        if (respawn) {
            removeFromTab(player);
        }

        try {
            Object minecraftServer = MinecraftReflection.getMinecraftServerClass().getMethod("getServer").invoke(null);
            Object playerList = minecraftServer.getClass().getMethod("getPlayerList").invoke(minecraftServer);
            Field f = playerList.getClass().getSuperclass().getDeclaredField("playersByName");
            f.setAccessible(true);
            Map<String, Object> playersByName = (Map<String, Object>) f.get(playerList);
            playersByName.remove(player.getName());
            WrappedGameProfile profile = WrappedGameProfile.fromPlayer(player);
            Field field = profile.getHandle().getClass().getDeclaredField("name");
            field.setAccessible(true);
            field.set(profile.getHandle(), name);
            field.setAccessible(false);
            playersByName.put(name, MinecraftReflection.getCraftPlayerClass().getMethod("getHandle").invoke(player));
            f.setAccessible(false);
        } catch (Exception ex) {
            CommonPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to change player name", ex);
            return;
        }

        if (respawn) {
            respawnPlayer(player);
        }
    }

    public static void removeFromTab(Player player) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
        packet.getPlayerInfoAction().write(0, PlayerInfoAction.REMOVE_PLAYER);
        if (player.getGameMode() != null) {
            try {
                Object entityPlayer = MinecraftReflection.getCraftPlayerClass().getMethod("getHandle").invoke(player);
                Object getDisplayName = MinecraftReflection.getEntityPlayerClass().getMethod("getPlayerListName")
                                                           .invoke(entityPlayer);
                packet.getPlayerInfoDataLists().write(0, Arrays.asList(
                        new PlayerInfoData(WrappedGameProfile.fromPlayer(player), 0,
                                           NativeGameMode.fromBukkit(player.getGameMode()),
                                           getDisplayName != null ? WrappedChatComponent.fromHandle(getDisplayName) :
                                                   null)));
            } catch (FieldAccessException | IllegalAccessException | IllegalArgumentException |
                     InvocationTargetException | NoSuchMethodException | SecurityException ex) {
                CommonPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to remove player from tab list", ex);
                return;
            }
        }

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.canSee(player)) {
                continue;
            }

            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(online, packet);
            } catch (InvocationTargetException ex) {
                CommonPlugin.getInstance().getLogger()
                            .log(Level.SEVERE, "Failed to send packet to " + online.getName(), ex);
            }
        }
    }

    public static void respawnPlayer(Player player) {
        respawnSelf(player);

        CommonPlugin.getInstance().getPluginPlatform().runLater(() -> {
            Bukkit.getOnlinePlayers().stream().filter(onlinePlayer -> !onlinePlayer.equals(player))
                  .filter(onlinePlayer -> onlinePlayer.canSee(player)).forEach(onlinePlayer -> {
                      onlinePlayer.hidePlayer(player);
                      onlinePlayer.showPlayer(player);
                  });
        }, 5L);
    }

    @SuppressWarnings("deprecation")
    public static void respawnSelf(Player player) {
        List<PlayerInfoData> data = new ArrayList<>();

        if (player.getGameMode() != null) {
            try {
                Object entityPlayer = MinecraftReflection.getCraftPlayerClass().getMethod("getHandle").invoke(player);
                Object getDisplayName = MinecraftReflection.getEntityPlayerClass().getMethod("getPlayerListName")
                                                           .invoke(entityPlayer);
                int ping = (int) MinecraftReflection.getEntityPlayerClass().getField("ping").get(entityPlayer);
                data.add(new PlayerInfoData(WrappedGameProfile.fromPlayer(player), ping,
                                            NativeGameMode.fromBukkit(player.getGameMode()),
                                            getDisplayName != null ? WrappedChatComponent.fromHandle(getDisplayName) :
                                                    null));
            } catch (FieldAccessException | IllegalAccessException | IllegalArgumentException |
                     InvocationTargetException | NoSuchMethodException | SecurityException | NoSuchFieldException ex) {
                CommonPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to respawn player", ex);
                return;
            }
        }

        PacketContainer addPlayerInfo = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
        addPlayerInfo.getPlayerInfoAction().write(0, PlayerInfoAction.ADD_PLAYER);
        addPlayerInfo.getPlayerInfoDataLists().write(0, data);

        PacketContainer removePlayerInfo = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
        removePlayerInfo.getPlayerInfoAction().write(0, PlayerInfoAction.REMOVE_PLAYER);
        removePlayerInfo.getPlayerInfoDataLists().write(0, data);

        PacketContainer respawnPlayer = new PacketContainer(PacketType.Play.Server.RESPAWN);
        respawnPlayer.getIntegers().write(0, player.getWorld().getEnvironment().getId());
        respawnPlayer.getDifficulties().write(0, Difficulty.valueOf(player.getWorld().getDifficulty().name()));

        if (player.getGameMode() != null) {
            respawnPlayer.getGameModes().write(0, NativeGameMode.fromBukkit(player.getGameMode()));
        }

        respawnPlayer.getWorldTypeModifier().write(0, player.getWorld().getWorldType());
        boolean flying = player.isFlying();

        CommonPlugin.getInstance().getPluginPlatform().runSync(() -> {
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, removePlayerInfo);
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, addPlayerInfo);
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, respawnPlayer);
                player.getInventory().setHeldItemSlot(player.getInventory().getHeldItemSlot());
                player.teleport(player.getLocation());
                player.setFlying(flying);
                player.setWalkSpeed(player.getWalkSpeed());
                player.setMaxHealth(player.getMaxHealth());
                player.setHealthScale(player.getHealthScale());
                player.setExp(player.getExp());
                player.setLevel(player.getLevel());
                player.updateInventory();
            } catch (InvocationTargetException ex) {
                CommonPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to respawn player", ex);
            }
        });
    }

    public static WrappedSignedProperty changePlayerSkin(Player player, String value, String signature,
            boolean respawn) {
        return changePlayerSkin(player, new WrappedSignedProperty("textures", value, signature));
    }

    public static WrappedSignedProperty changePlayerSkin(Player player, String name, UUID uuid, boolean respawn) {
        WrappedSignedProperty property = null;
        WrappedGameProfile gameProfile = WrappedGameProfile.fromPlayer(player);
        gameProfile.getProperties().clear();

        try {
            //gameProfile.getProperties()
            //           .put("textures", property = TextureFetcher.loadTexture(new WrappedGameProfile(uuid, name)));
        } catch (CacheLoader.InvalidCacheLoadException exception) {
            CommonPlugin.getInstance()
                        .debug("Failed to load skin for " + player.getUniqueId() + " (" + player.getName() + "): " +
                                       exception.getMessage());
            gameProfile.getProperties().clear();
        }

        if (respawn) {
            respawnPlayer(player);
        }

        return property;
    }

    public static WrappedSignedProperty changePlayerSkin(Player player, WrappedSignedProperty wrappedSignedProperty) {
        WrappedGameProfile gameProfile = WrappedGameProfile.fromPlayer(player);

        gameProfile.getProperties().clear();
        gameProfile.getProperties().put("textures", wrappedSignedProperty);

        respawnPlayer(player);

        return wrappedSignedProperty;
    }

    public static void changePlayerSkin(Player player, WrappedSignedProperty property, boolean respawn) {
        WrappedGameProfile gameProfile = WrappedGameProfile.fromPlayer(player);
        gameProfile.getProperties().clear();

        gameProfile.getProperties().put("textures", property);

        if (respawn) {
            respawnPlayer(player);
        }
    }

    public static void changePlayerSkin(Player player, Skin skin, boolean respawn) {
        changePlayerSkin(player, skin.getValue(), skin.getSignature(), respawn);
    }

    public static void removePlayerSkin(Player player) {
        removePlayerSkin(player, true);
    }

    public static void removePlayerSkin(Player player, boolean respawn) {
        WrappedGameProfile gameProfile = WrappedGameProfile.fromPlayer(player);
        gameProfile.getProperties().clear();

        if (respawn) {
            respawnPlayer(player);
        }
    }

    public static void broadcastHeader(String header) {
        broadcastHeaderAndFooter(header, null);
    }

    public static void broadcastFooter(String footer) {
        broadcastHeaderAndFooter(null, footer);
    }

    public static void broadcastHeaderAndFooter(String header, String footer) {
        for (Player player : Bukkit.getOnlinePlayers())
            setHeaderAndFooter(player, header, footer);
    }

    public static void setHeaderAndFooter(Player p, String rawHeader, String rawFooter) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER);
        packet.getChatComponents().write(0, WrappedChatComponent.fromText(rawHeader));
        packet.getChatComponents().write(1, WrappedChatComponent.fromText(rawFooter));

        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(p, packet);
        } catch (InvocationTargetException ex) {
            CommonPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to send packet to " + p.getName(), ex);
        }
    }

    public static void title(Player player, String title, String subTitle) {
        if (BukkitCommon.getInstance().getProtocolVersion(player).getId() >= 47) {
            sendPacket(player, new PacketBuilder(PacketType.Play.Server.TITLE)
                    .writeTitleAction(0, EnumWrappers.TitleAction.TITLE)
                    .writeChatComponents(0, WrappedChatComponent.fromText(title)).build());
            sendPacket(player, new PacketBuilder(PacketType.Play.Server.TITLE)
                    .writeTitleAction(0, EnumWrappers.TitleAction.SUBTITLE)
                    .writeChatComponents(0, WrappedChatComponent.fromText(subTitle)).build());
            sendPacket(player, new PacketBuilder(PacketType.Play.Server.TITLE)
                    .writeTitleAction(0, EnumWrappers.TitleAction.TIMES).writeInteger(0, 10).writeInteger(1, 20)
                    .writeInteger(2, 20).build());
        }
    }

    public static void subtitle(Player player, String subTitle) {
        if (BukkitCommon.getInstance().getProtocolVersion(player).getId() >= 47) {
            sendPacket(player, new PacketBuilder(PacketType.Play.Server.TITLE)
                    .writeTitleAction(0, EnumWrappers.TitleAction.SUBTITLE)
                    .writeChatComponents(0, WrappedChatComponent.fromText(subTitle)).build());
        }
    }

    public static void title(Player player, String title, String subTitle, int fadeIn, int stayIn, int fadeOut) {
        if (BukkitCommon.getInstance().getProtocolVersion(player).getId() >= 47) {
            sendPacket(player, new PacketBuilder(PacketType.Play.Server.TITLE)
                    .writeTitleAction(0, EnumWrappers.TitleAction.TITLE)
                    .writeChatComponents(0, WrappedChatComponent.fromText(title)).build());
            sendPacket(player, new PacketBuilder(PacketType.Play.Server.TITLE)
                    .writeTitleAction(0, EnumWrappers.TitleAction.SUBTITLE)
                    .writeChatComponents(0, WrappedChatComponent.fromText(subTitle)).build());
            sendPacket(player, new PacketBuilder(PacketType.Play.Server.TITLE)
                    .writeTitleAction(0, EnumWrappers.TitleAction.TIMES).writeInteger(0, fadeIn).writeInteger(1, stayIn)
                    .writeInteger(2, fadeOut).build());
        }
    }

    public static void actionbar(Player player, String text) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.CHAT);
        packet.getChatComponents().write(0, WrappedChatComponent.fromJson("{\"text\":\"" + text + " \"}"));
        packet.getBytes().write(0, (byte) 2);
        sendPacket(player, packet);
    }

    public static void broadcastActionBar(String text) {
        Bukkit.getOnlinePlayers().forEach(player -> actionbar(player, text));
    }

    public static void sendPacket(Player player, PacketContainer packet) {
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static ProtocolVersion getProtocolVersion(Player player) {
        return ProtocolVersion.getById(ProtocolLibrary.getProtocolManager().getProtocolVersion(player));
    }

    public void addToTab(Player player, Collection<? extends Player> players) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
        packet.getPlayerInfoAction().write(0, PlayerInfoAction.ADD_PLAYER);

        if (player.getGameMode() != null) {
            try {
                Object entityPlayer = MinecraftReflection.getCraftPlayerClass().getMethod("getHandle").invoke(player);
                Object getDisplayName = MinecraftReflection.getEntityPlayerClass().getMethod("getPlayerListName")
                                                           .invoke(entityPlayer);
                packet.getPlayerInfoDataLists().write(0, Arrays.asList(
                        new PlayerInfoData(WrappedGameProfile.fromPlayer(player), 0,
                                           NativeGameMode.fromBukkit(player.getGameMode()),
                                           getDisplayName != null ? WrappedChatComponent.fromHandle(getDisplayName) :
                                                   null)));
            } catch (FieldAccessException | IllegalAccessException | IllegalArgumentException |
                     InvocationTargetException | NoSuchMethodException | SecurityException e1) {
                e1.printStackTrace();
            }
        }

        for (Player online : players) {
            if (!online.canSee(player)) {
                continue;
            }

            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(online, packet);
            } catch (InvocationTargetException e) {
                CommonPlugin.getInstance().getLogger()
                            .log(Level.SEVERE, "Failed to send packet to " + online.getName(), e);
            }
        }
    }
}
