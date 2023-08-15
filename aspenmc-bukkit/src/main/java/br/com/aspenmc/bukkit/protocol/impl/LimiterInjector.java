package br.com.aspenmc.bukkit.protocol.impl;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bukkit.BukkitMain;
import br.com.aspenmc.bukkit.protocol.PacketInjector;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import io.netty.buffer.ByteBuf;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LimiterInjector implements PacketInjector {

    private final Pattern pattern = Pattern.compile(".*\\$\\{[^}]*\\}.*");

    @Override
    public void inject(Plugin plugin) {
        ProtocolLibrary.getProtocolManager().addPacketListener(
                new PacketAdapter(BukkitMain.getInstance(), ListenerPriority.LOWEST, PacketType.Play.Server.CHAT,
                        PacketType.Play.Client.CHAT, PacketType.Play.Client.WINDOW_CLICK,
                        PacketType.Play.Client.CUSTOM_PAYLOAD) {

                    @Override
                    public void onPacketSending(PacketEvent event) {
                        if (event.getPlayer() == null) return;

                        if (event.getPacketType() == PacketType.Play.Server.CHAT) {
                            PacketContainer packetContainer = event.getPacket();

                            WrappedChatComponent wrappedChatComponent = packetContainer.getChatComponents().getValues()
                                                                                       .get(0);

                            if (wrappedChatComponent == null) {
                                return;
                            }

                            String jsonMessage = wrappedChatComponent.getJson();

                            if (jsonMessage.indexOf('$') == -1) {
                                return;
                            }

                            if (matches(jsonMessage)) {
                                event.setCancelled(true);
                                packetContainer.getChatComponents().write(0, WrappedChatComponent.fromText(""));
                                System.out.println("The player " + event.getPlayer().getName() +
                                        " is trying to crash the server (Chat)");
                            }
                        }
                    }

                    @Override
                    public void onPacketReceiving(PacketEvent event) {
                        if (event.getPacketType() == PacketType.Play.Client.CHAT) {
                            PacketContainer packetContainer = event.getPacket();

                            String message = packetContainer.getStrings().read(0);

                            if (message.indexOf('$') == -1) {
                                return;
                            }

                            if (matches(message)) {
                                event.setCancelled(true);
                                packetContainer.getStrings().write(0, "");
                                disconnect(event.getPlayer());
                                CommonPlugin.getInstance().debug("The player " + event.getPlayer().getName() +
                                        " is trying to crash the server (Chat)");
                            }
                        } else if (event.getPacketType() == PacketType.Play.Client.WINDOW_CLICK) {
                            if ((int) event.getPacket().getModifier().getValues().get(1) >= 100) {
                                event.setCancelled(true);
                                disconnect(event.getPlayer());
                                CommonPlugin.getInstance().debug("The player " + event.getPlayer().getName() +
                                        " is trying to crash the server (WindowClick)");
                            }
                        } else if (event.getPacketType() == PacketType.Play.Client.CUSTOM_PAYLOAD) {
                            String packetName = event.getPacket().getStrings().getValues().get(0);

                            if (packetName.equals("MC|BEdit") || packetName.equals("MC|BSign")) {
                                if (((ByteBuf) event.getPacket().getModifier().getValues().get(1)).capacity() > 7500) {
                                    event.setCancelled(true);
                                    disconnect(event.getPlayer());
                                    CommonPlugin.getInstance().debug("The player " + event.getPlayer().getName() +
                                            " is trying to crash the server (CustomPayload)");
                                }
                            }
                        }
                    }
                });
    }

    private void disconnect(Player player) {
        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.KICK_DISCONNECT);

        packetContainer.getChatComponents().write(0, WrappedChatComponent.fromText("§cLost connection."));

        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packetContainer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean matches(String message) {
        Matcher matcher = pattern.matcher(message.replaceAll("[^\\x00-\\x7F]", "").toLowerCase(Locale.ROOT));
        return matcher.find();
    }
}
