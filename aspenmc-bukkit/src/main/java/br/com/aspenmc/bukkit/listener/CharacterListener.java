package br.com.aspenmc.bukkit.listener;

import br.com.aspenmc.bukkit.BukkitCommon;
import br.com.aspenmc.bukkit.event.player.PlayerMoveUpdateEvent;
import br.com.aspenmc.bukkit.utils.character.Character;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class CharacterListener implements Listener {

    private static final double MAX_DISTANCE = 128;

    public CharacterListener() {
        ProtocolLibrary.getProtocolManager().addPacketListener(
                new PacketAdapter(BukkitCommon.getInstance(), PacketType.Play.Client.USE_ENTITY) {

                    @Override
                    public void onPacketReceiving(PacketEvent event) {
                        if (event.isCancelled()) {
                            return;
                        }

                        Player player = event.getPlayer();


                        int entityId = event.getPacket().getIntegers().read(0);

                        Character character = BukkitCommon.getInstance().getCharacterManager()
                                                          .getCharacterById(entityId);

                        if (character != null) {
                            new BukkitRunnable() {

                                @Override
                                public void run() {
                                    character.getTouchHandler().onTouch(character, player,
                                                                        event.getPacket().getEntityUseActions()
                                                                             .read(0) == EntityUseAction.INTERACT);
                                }
                            }.runTask(BukkitCommon.getInstance());
                        }
                    }
                });
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        BukkitCommon.getInstance().getCharacterManager().getCharacters().forEach(character -> {
            if (character.getLocation().getWorld() == event.getPlayer().getLocation().getWorld()) {
                if (character.getLocation().distance(event.getPlayer().getLocation()) < MAX_DISTANCE) {
                    character.show(event.getPlayer());
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        new BukkitRunnable() {

            @Override
            public void run() {
                BukkitCommon.getInstance().getCharacterManager().getCharacters().forEach(character -> {
                    if (character.getLocation().getWorld() == event.getPlayer().getLocation().getWorld()) {
                        if (character.isShowing(event.getPlayer()) &&
                            character.getLocation().distance(event.getPlayer().getLocation()) < MAX_DISTANCE) {
                            character.hide(event.getPlayer());
                            character.show(event.getPlayer());
                        }
                    }
                });
            }
        }.runTaskLater(BukkitCommon.getInstance(), 5L);
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        handleCharacterForPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerMoveUpdate(PlayerMoveUpdateEvent event) {
        handleCharacterForPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(PlayerQuitEvent event) {
        BukkitCommon.getInstance().getCharacterManager().getCharacters()
                    .forEach(character -> character.hide(event.getPlayer()));
    }

    private void handleCharacterForPlayer(Player player) {
        BukkitCommon.getInstance().getCharacterManager().getCharacters().forEach(character -> {
            if (character.isShowing(player)) {
                if (character.getLocation().getWorld() != player.getLocation().getWorld()) {
                    character.hide(player);
                } else if (character.getLocation().distance(player.getLocation()) > MAX_DISTANCE) {
                    character.hide(player);
                } else {
                    if (character.hasCollision() && character.getLocation().distance(player.getLocation()) < 1.5) {
                        player.setVelocity(player.getLocation().toVector().subtract(character.getLocation().toVector())
                                                 .multiply(0.7).setY(0.4));
                    }
                }
            } else {
                if (character.getLocation().getWorld() == player.getLocation().getWorld() &&
                    character.getLocation().distance(player.getLocation()) < MAX_DISTANCE) {
                    character.show(player);
                }
            }
        });
    }
}
