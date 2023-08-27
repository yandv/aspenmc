package br.com.aspenmc.bukkit.protocol.impl;

import br.com.aspenmc.bukkit.protocol.PacketInjector;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.plugin.Plugin;

public class MessageInjector implements PacketInjector {

    @Override
    public void inject(Plugin plugin) {
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
                                }
                            } catch (Exception ignored) {
                            }
                        }
                    }
                });
    }
}
