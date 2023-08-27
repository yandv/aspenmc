package br.com.aspenmc.bukkit.antihax;

import br.com.aspenmc.bukkit.BukkitCommon;
import br.com.aspenmc.bukkit.entity.PlayerData;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import net.minecraft.server.v1_8_R3.PacketPlayInClientCommand;
import net.minecraft.server.v1_8_R3.PacketPlayInEntityAction;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerListener implements Listener {

    public PlayerListener() {
        ProtocolLibrary.getProtocolManager().addPacketListener(
                new PacketAdapter(BukkitCommon.getInstance(), ListenerPriority.LOWEST,
                        PacketType.Play.Client.WINDOW_CLICK, PacketType.Play.Client.CLIENT_COMMAND,
                        PacketType.Play.Client.CLOSE_WINDOW, PacketType.Play.Client.USE_ENTITY,
                        PacketType.Play.Client.ENTITY_ACTION, PacketType.Play.Client.ARM_ANIMATION,

                        PacketType.Play.Client.FLYING, PacketType.Play.Client.LOOK,
                        PacketType.Play.Client.POSITION_LOOK, PacketType.Play.Client.POSITION,

                        PacketType.Play.Client.KEEP_ALIVE,

                        PacketType.Play.Server.CLOSE_WINDOW, PacketType.Play.Server.OPEN_WINDOW) {

                    @Override
                    public void onPacketReceiving(PacketEvent event) {
                        Player player = event.getPlayer();

                        if (player == null) return;

                        PlayerData playerData = BukkitCommon.getInstance().getCheatManager()
                                                            .getOrLoad(player.getUniqueId());

                        if (event.getPacketType() == PacketType.Play.Client.CLIENT_COMMAND) {
                            PacketPlayInClientCommand.EnumClientCommand command = ((PacketPlayInClientCommand) event
                                    .getPacket().getHandle()).a();

                            if (command == PacketPlayInClientCommand.EnumClientCommand.OPEN_INVENTORY_ACHIEVEMENT) {
                                playerData.setInventoryOpened(true);
                            }
                        } else if (event.getPacketType() == PacketType.Play.Client.CLOSE_WINDOW) {
                            playerData.setInventoryOpened(false);
                        } else if (event.getPacketType() == PacketType.Play.Client.WINDOW_CLICK) {
                            playerData.setLastWindowClick(System.currentTimeMillis());
                        } else if (event.getPacketType() == PacketType.Play.Client.USE_ENTITY) {
                            playerData.setLastAttack(System.currentTimeMillis());
                        } else if (event.getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {
                            PacketPlayInEntityAction.EnumPlayerAction action = ((PacketPlayInEntityAction) event
                                    .getPacket().getHandle()).b();

                            switch (action) {
                            case START_SNEAKING:
                                playerData.setSneaking(true);
                                break;
                            case STOP_SNEAKING:
                                playerData.setSneaking(false);
                                break;
                            case START_SPRINTING:
                                playerData.setSprinting(true);
                                break;
                            case STOP_SPRINTING:
                                playerData.setSprinting(false);
                                break;
                            }
                        } else if (event.getPacketType() == PacketType.Play.Client.FLYING) {
                            playerData.setLastFlying(System.currentTimeMillis());
                        } else if (event.getPacketType() == PacketType.Play.Client.LOOK) {
                            playerData.setLastFlying(System.currentTimeMillis());
                        } else if (event.getPacketType() == PacketType.Play.Client.POSITION_LOOK) {
                            playerData.setLastFlying(System.currentTimeMillis());
                        } else if (event.getPacketType() == PacketType.Play.Client.POSITION) {
                            playerData.setLastFlying(System.currentTimeMillis());
                        } else if (event.getPacketType() == PacketType.Play.Client.KEEP_ALIVE) {
                            playerData.setLastPing(playerData.getPing());
                            playerData.setPing(((CraftPlayer) event.getPlayer()).getHandle().ping);
                            playerData.setDeltaPing(playerData.getPing() - playerData.getLastPing());
                        }
                    }

                    @Override
                    public void onPacketSending(PacketEvent event) {
                        Player player = event.getPlayer();
                        PlayerData playerData = BukkitCommon.getInstance().getCheatManager()
                                                            .getOrLoad(player.getUniqueId());

                        if (event.getPacketType() == PacketType.Play.Server.OPEN_WINDOW) {
                            playerData.setInventoryOpened(true);
                        } else if (event.getPacketType() == PacketType.Play.Server.CLOSE_WINDOW) {
                            playerData.setInventoryOpened(false);
                        }
                    }
                });
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        PlayerData playerData = BukkitCommon.getInstance().getCheatManager().getOrLoad(event.getPlayer().getUniqueId());

        if (playerData == null) return;

        playerData.setInventoryOpened(false);

        playerData.setDeltaX(0);
        playerData.setDeltaY(0);
        playerData.setDeltaZ(0);

        playerData.setLastDeltaX(0);
        playerData.setLastDeltaY(0);
        playerData.setLastDeltaZ(0);

        playerData.setJumpHeight(0);

        playerData.setInventoryOpened(false);
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        PlayerData playerData = BukkitCommon.getInstance().getCheatManager().getOrLoad(event.getPlayer().getUniqueId());

        if (playerData == null) return;

        playerData.setInventoryOpened(false);

        playerData.setDeltaX(0);
        playerData.setDeltaY(0);
        playerData.setDeltaZ(0);

        playerData.setLastDeltaX(0);
        playerData.setLastDeltaY(0);
        playerData.setLastDeltaZ(0);

        playerData.setJumpHeight(0);

        playerData.setLastTeleport(System.currentTimeMillis());

        playerData.setInventoryOpened(false);
    }
}
