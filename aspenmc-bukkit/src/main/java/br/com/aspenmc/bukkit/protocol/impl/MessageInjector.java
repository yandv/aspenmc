package br.com.aspenmc.bukkit.protocol.impl;

import br.com.aspenmc.bukkit.protocol.PacketInjector;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MessageInjector implements PacketInjector {

    private Map<UUID, Message> messageMap;

    @Override
    public void inject(Plugin plugin) {
        messageMap = new HashMap<>();
        ProtocolLibrary.getProtocolManager().addPacketListener(
                new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.CHAT) {

                    @Override
                    public void onPacketSending(PacketEvent e) {
                        if (e.getPacketType() == PacketType.Play.Server.CHAT) {
                            try {
                                // tile.bed.notValid
                                String json = e.getPacket().getChatComponents().read(0).getJson();

                                if (json.equals("{\"translate\":\"chat.type.achievement\"}") ||
                                        json.equals("{\"translate\":\"chat.type.achievement.taken\"}") ||
                                        json.equals("{\"translate\":\"tile.bed.noSleep\"}") ||
                                        json.equals("{\"translate\":\"tile.bed.notValid\"}")) {
                                    e.setCancelled(true);
                                    return;
                                }

                                if (json.contains("text")) {
                                    Message message = messageMap.get(e.getPlayer().getUniqueId());

                                    if (message != null && message.message.equals(json)) {
                                        if (System.currentTimeMillis() - message.createdAt < 250) {
                                            e.setCancelled(true);
                                        }
                                    }

                                    messageMap.put(e.getPlayer().getUniqueId(), message(json));
                                }
                            } catch (Exception ignored) {
                            }
                        }
                    }
                });

        Bukkit.getPluginManager().registerEvents(new Listener() {

            @EventHandler
            public void onPlayerQuit(PlayerQuitEvent event) {
                messageMap.remove(event.getPlayer().getUniqueId());
            }
        }, plugin);
    }

    public Message message(String message) {
        return new Message(message, System.currentTimeMillis());
    }

    @AllArgsConstructor
    public static final class Message {

        private String message;
        private long createdAt;
    }
}
