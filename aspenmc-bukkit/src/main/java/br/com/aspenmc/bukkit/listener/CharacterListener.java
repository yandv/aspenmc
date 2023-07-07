package br.com.aspenmc.bukkit.listener;

import br.com.aspenmc.bukkit.BukkitCommon;
import br.com.aspenmc.bukkit.BukkitMain;
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
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;

public class CharacterListener implements Listener {

    private static final int MAX_DISTANCE = 128;

    public CharacterListener() {
        ProtocolLibrary.getProtocolManager()
                       .addPacketListener(new PacketAdapter(BukkitMain.getInstance(), PacketType.Play.Client.USE_ENTITY) {

                           @Override
                           public void onPacketReceiving(PacketEvent event) {
                               if (event.isCancelled())
                                   return;

                               Player player = event.getPlayer();

                               if (event.getPacket().getEntityUseActions().read(0) == EntityUseAction.INTERACT
                                   || event.getPacket().getEntityUseActions().read(0) == EntityUseAction.ATTACK) {
                                   int entityId = event.getPacket().getIntegers().read(0);

                                   Character character = BukkitCommon.getInstance().getCharacterManager().getCharacterById(entityId);

                                   if (character != null)
                                       character.getTouchHandler().onTouch(character, player,
                                                                                 event.getPacket().getEntityUseActions().read(0) == EntityUseAction.INTERACT);
                               }

                           }

                       });
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        BukkitCommon.getInstance().getCharacterManager().getCharacters().forEach(character -> {
            if (character.getLocation().getWorld().equals(event.getPlayer().getLocation().getWorld()) && character.getLocation().distance(event.getPlayer().getLocation()) < MAX_DISTANCE)
                character.show(event.getPlayer());
        });
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        BukkitCommon.getInstance().getCharacterManager().getCharacters().forEach(character -> {
            if (character.isShowing(event.getPlayer())) {
                if (!character.getLocation().getWorld().equals(event.getTo().getWorld())) {
                    character.hide(event.getPlayer());
                } else if (character.getLocation().distance(event.getTo()) > MAX_DISTANCE)
                    character.hide(event.getPlayer());
            } else {
                if (character.getLocation().getWorld().equals(event.getTo().getWorld()) && character.getLocation().distance(event.getTo()) < MAX_DISTANCE) {
                    character.show(event.getPlayer());
                }
            }
        });
    }


    @EventHandler
    public void onPlayerMoveUpdate(PlayerMoveUpdateEvent event) {
        BukkitCommon.getInstance().getCharacterManager().getCharacters().forEach(character -> {
            if (character.isShowing(event.getPlayer())) {
                if (!character.getLocation().getWorld().equals(event.getTo().getWorld())) {
                    character.hide(event.getPlayer());
                } else if (character.getLocation().distance(event.getTo()) > MAX_DISTANCE)
                    character.hide(event.getPlayer());
            } else {
                if (character.getLocation().getWorld().equals(event.getTo().getWorld()) && character.getLocation().distance(event.getTo()) < MAX_DISTANCE) {
                    character.show(event.getPlayer());
                }
            }
        });
    }

    @EventHandler
    public void onPlayerJoin(PlayerQuitEvent event) {
        BukkitCommon.getInstance().getCharacterManager().getCharacters().forEach(character -> character.hide(event.getPlayer()));
    }
}
