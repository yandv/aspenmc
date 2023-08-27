package br.com.aspenmc.discord;

import br.com.aspenmc.CommonConst;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.backend.Credentials;
import br.com.aspenmc.backend.type.RedisConnection;
import br.com.aspenmc.discord.command.CommandHandler;
import br.com.aspenmc.discord.command.register.PingCommand;
import br.com.aspenmc.discord.listener.CommandListener;
import br.com.aspenmc.discord.listener.StaffchatListener;
import br.com.aspenmc.discord.manager.CommandManager;
import br.com.aspenmc.discord.manager.DiscordMemberManager;
import br.com.aspenmc.discord.networking.BungeeCordPubSub;
import br.com.aspenmc.packet.type.discord.MessageRequest;
import br.com.aspenmc.packet.type.discord.ServerStaffMessage;
import br.com.aspenmc.punish.Punish;
import br.com.aspenmc.punish.PunishType;
import br.com.aspenmc.server.ServerType;
import br.com.aspenmc.utils.string.StringFormat;
import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;
import java.time.Instant;
import java.util.logging.Logger;

@Getter
public class DiscordMain {

    @Getter
    private static DiscordMain instance;

    private CommonPlugin plugin;

    private JDA jda;

    private CommandManager commandManager;
    private DiscordMemberManager memberManager;

    private WebhookClient staffChatWebHook;

    public DiscordMain() {
        instance = this;
        plugin = new CommonPlugin(new DiscordPlatform(), Logger.getLogger("AspenMC"));

        plugin.setServerId("discord.aspenmc.com.br");
        plugin.setServerType(ServerType.DISCORD);
        plugin.setServerAddress("localhost");

        plugin.setServerLog(true);

        plugin.startConnection(new Credentials("127.0.0.1", "", "", "aspenmc", 6379),
                new Credentials("localhost", "", "", "", 6379));

        plugin.getPluginPlatform().runAsync(
                new RedisConnection.PubSubListener(plugin.getRedisConnection(), new BungeeCordPubSub(),
                        CommonConst.SERVER_PACKET_CHANNEL));

        try {
            setupDiscordServer();
        } catch (Exception e) {
            System.exit(0);
            e.printStackTrace();
            return;
        }

        commandManager = new CommandManager();
        memberManager = new DiscordMemberManager();

        registerListeners();
        registerCommands();
        registerWebhooks();

        plugin.getLogger().info("Discord server started!");
    }

    private void registerWebhooks() {
        staffChatWebHook = new WebhookClientBuilder(
                "https://discord.com/api/webhooks/1145307173547692174/-m_wX9brfoEqnsIvtj5FZCA68FprhhiTmyCtH" +
                        "--SK46AQH92MeXHMlNSqP5YOjXCslZ_").build();
    }

    private void setupDiscordServer() throws LoginException, InterruptedException {

        JDABuilder builder = JDABuilder.createDefault(
                "MTE0Mzc1MDgzMjE1NjgzOTk3Ng.GBKz-z.mCHkwlzGjkDFS-EkCAfKZ7DJMiFpig8q6DYf5U");

        builder.disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE);
        builder.setBulkDeleteSplittingEnabled(false);
        builder.setCompression(Compression.NONE);
        builder.disableCache(CacheFlag.ACTIVITY);
        builder.setMemberCachePolicy(MemberCachePolicy.VOICE.or(MemberCachePolicy.OWNER));
        builder.setChunkingFilter(ChunkingFilter.NONE);
        builder.disableIntents(GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MESSAGE_TYPING);

        (jda = builder.build()).awaitReady();
    }

    private void registerListeners() {
        jda.addEventListener(new CommandListener());
        jda.addEventListener(new StaffchatListener());

        plugin.setServerLogPackets(true);

        plugin.getPacketManager().registerHandler(ServerStaffMessage.class, packet -> {
            staffChatWebHook.send(new WebhookMessageBuilder()
                    .setAvatarUrl("https://www.mc-heads.net/avatar/" + packet.getPlayerName())
                    .setUsername(packet.getPlayerName()).setContent(packet.getMessage()).build());
        });

        plugin.getPacketManager().registerHandler(MessageRequest.class, packet -> {
            switch (packet.getMessageType()) {
            case "punish": {
                TextChannel textChannelById = jda.getTextChannelById(DiscordConst.PUNISH_CHAT_ID);

                if (textChannelById == null) return;

                Punish punish = CommonConst.GSON.fromJson(CommonConst.GSON.toJson(packet.getData().get("punish")),
                        Punish.class);

                EmbedBuilder embedBuilder = new EmbedBuilder();

                embedBuilder.setTitle(punish.getPunisherName() + " foi " +
                        (punish.getPunishType() == PunishType.BAN ? "banido" : "silenciado") + ".");
                embedBuilder.setThumbnail("https://mc-heads.net/avatar/" + punish.getPunisherName());

                embedBuilder.addField("Banido por", punish.getPunisherName(), false);
                embedBuilder.addField("Motivo", punish.getReason(), false);
                embedBuilder.addField("Tempo", punish.getDuration() == -1 ? "Permanente" :
                        StringFormat.formatTime(punish.getDuration() / 1000L), false);

                embedBuilder.setFooter("Enviado automáticamente pelo AspenMC");
                embedBuilder.setTimestamp(Instant.now());

                textChannelById.sendMessageEmbeds(embedBuilder.build()).queue();
                break;
            }
            case "anticheat": {
                TextChannel textChannelById = jda.getTextChannelById("1143752270446600275");

                if (textChannelById == null) return;

                String suspectName = (String) packet.getData().get("suspect");
                String message = (String) packet.getData().get("message");

                EmbedBuilder embedBuilder = new EmbedBuilder();

                embedBuilder.setTitle("Anticheat");
                embedBuilder.setThumbnail("https://mc-heads.net/avatar/" + suspectName);

                embedBuilder.addField("Suspeito", suspectName, true);
                embedBuilder.addField("Mensagem", message, true);
                embedBuilder.addField("Servidor", (String) packet.getData().get("serverId"), false);

                embedBuilder.setFooter("Enviado automáticamente pelo AspenMC");
                embedBuilder.setTimestamp(Instant.now());

                textChannelById.sendMessageEmbeds(embedBuilder.build()).queue();
                break;
            }
            case "report": {
                TextChannel textChannelById = jda.getTextChannelById("1143752270446600275");

                if (textChannelById == null) return;

                String senderName = (String) packet.getData().get("sender");
                String target = (String) packet.getData().get("target");
                String reason = (String) packet.getData().get("reason");

                EmbedBuilder embedBuilder = new EmbedBuilder();

                embedBuilder.setTitle("Novo report!");
                embedBuilder.setThumbnail("https://mc-heads.net/avatar/" + senderName);

                embedBuilder.addField("Reportado", target, true);
                embedBuilder.addField("Quem reportou", senderName, true);
                embedBuilder.addField("Motivo", reason, false);

                embedBuilder.setFooter("Enviado automáticamente pelo AspenMC");
                embedBuilder.setTimestamp(Instant.now());

                textChannelById.sendMessageEmbeds(embedBuilder.build()).queue();
                break;
            }
            }
        });
    }

    private void registerCommands() {
        registerCommand("ping", "Pong!", new PingCommand()).queue();
    }

    public CommandCreateAction registerCommand(String command, String description, CommandHandler commandHandler) {
        commandManager.registerCommand(command, commandHandler);
        return jda.upsertCommand(command, description);
    }
}
