package br.com.aspenmc.bukkit.command.register;

import br.com.aspenmc.BukkitConst;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bukkit.BukkitCommon;
import br.com.aspenmc.bukkit.entity.BukkitMember;
import br.com.aspenmc.bukkit.event.server.LocationChangeEvent;
import br.com.aspenmc.command.CommandArgs;
import br.com.aspenmc.command.CommandFramework;
import br.com.aspenmc.command.CommandHandler;
import br.com.aspenmc.entity.Sender;
import br.com.aspenmc.packet.type.member.teleport.MemberTeleportRequest;
import br.com.aspenmc.packet.type.member.teleport.MemberTeleportResponse;
import br.com.aspenmc.permission.Group;
import br.com.aspenmc.server.ServerType;
import br.com.aspenmc.utils.string.StringFormat;
import br.com.aspenmc.utils.string.TimeFormat;
import com.google.common.base.Joiner;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

public class ModeratorCommand implements CommandHandler {

    @CommandFramework.Command(name = "admin", aliases = { "adm" }, console = false,
            permission = BukkitConst.PERMISION_ADMIN_MODE)
    public void adminCommand(CommandArgs cmdArgs) {
        Player player = ((BukkitMember) cmdArgs.getSenderAsMember(BukkitMember.class)).getPlayer();

        if (BukkitCommon.getInstance().getVanishManager().isPlayerInAdmin(player)) {
            BukkitCommon.getInstance().getVanishManager().setPlayer(player);
        } else {
            BukkitCommon.getInstance().getVanishManager().setPlayerInAdmin(player);
        }
    }

    @CommandFramework.Command(name = "build", permission = "command.build")
    public void buildCommand(CommandArgs cmdArgs) {
        BukkitMember member = cmdArgs.getSenderAsMember(BukkitMember.class);

        boolean buildEnabled = !member.isBuildEnabled();
        member.setBuildEnabled(buildEnabled);
        member.sendMessage(buildEnabled ? "§aVocê agora está no modo de construção." :
                "§cVocê agora não está mais no modo de construção.");
    }

    @CommandFramework.Command(name = "time", aliases = { "tempo" }, permission = "command.time")
    public void timeCommand(CommandArgs cmdArgs) {
        Sender sender = cmdArgs.getSender();
        String[] args = cmdArgs.getArgs();

        if (args.length == 0) {
            sender.sendMessage(" §a» §fUse §a" + cmdArgs.getLabel() + " <tempo> §fpara alterar o tempo do jogo.");
            return;
        }

        switch (args[0].toLowerCase()) {
        case "stop": {
            BukkitCommon.getInstance().setTimerEnabled(!BukkitCommon.getInstance().isTimerEnabled());
            BukkitCommon.getInstance().setConsoleControl(false);
            sender.sendMessage(BukkitCommon.getInstance().isTimerEnabled() ? "§aO tempo do jogo foi retomado." :
                    "§aO tempo do jogo foi parado.");
            break;
        }
        case "console": {
            BukkitCommon.getInstance().setConsoleControl(!BukkitCommon.getInstance().isConsoleControl());
            sender.sendMessage(BukkitCommon.getInstance().isConsoleControl() ?
                    "§aO tempo do jogo foi retomado para o comando do console." :
                    "§aO tempo do jogo foi retirado do comando do console.");
            break;
        }
        case "set": {
            if (args.length == 1) {
                sender.sendMessage(
                        " §a» §fUse §a" + cmdArgs.getLabel() + " set <tempo> §fpara alterar o tempo do jogo.");
                return;
            }

            Long time = 0L;

            try {
                time = Long.valueOf(args[1]);
            } catch (Exception e) {
                sender.sendMessage("§cO tempo inserido é inválido.");
                return;
            }

            if (sender.isPlayer()) {
                cmdArgs.getSenderAsMember(BukkitMember.class).getPlayer().getWorld().setTime(time);
            } else {
                long t = time;
                Bukkit.getWorlds().stream().findFirst().ifPresent(world -> world.setTime(t));
            }

            sender.sendMessage("§aO tempo do mundo foi alterado para " + time + ".");
            break;
        }
        default: {
            long time;

            try {
                time = StringFormat.getTimeFromString(args[0], true);
            } catch (Exception e) {
                sender.sendMessage("§cO tempo inserido é inválido.");
                return;
            }

            int seconds = (int) Math.floor((time - System.currentTimeMillis()) / 1000.0D);

            if (seconds >= 60 * 120) {
                seconds = 60 * 120;
            }

            sender.sendMessage(
                    "§aO tempo do jogo foi alterado para " + StringFormat.formatTime(seconds, TimeFormat.NORMAL) + ".");
            BukkitCommon.getInstance().updateTime(seconds);
            break;
        }
        }
    }

    @CommandFramework.Command(name = "setlocation", aliases = { "setloc" }, permission = "command.location")
    public void setlocationCommand(CommandArgs cmdArgs) {
        Sender sender = cmdArgs.getSender();
        String[] args = cmdArgs.getArgs();

        if (args.length == 0) {
            sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() + " <nome> para definir uma localização.");
            return;
        }

        String locationName = args[0].toLowerCase();
        Location location;

        if (sender.isPlayer()) {
            location = ((BukkitMember) sender).getPlayer().getLocation();
        } else {
            if (args.length < 4) {
                sender.sendMessage(
                        " §a» §fUse §a/" + cmdArgs.getLabel() + " <nome> <x> <y> <z> para definir uma localização.");
                return;
            }

            OptionalDouble x = StringFormat.parseDouble(args[1]);
            OptionalDouble y = StringFormat.parseDouble(args[2]);
            OptionalDouble z = StringFormat.parseDouble(args[3]);

            if (!x.isPresent()) {
                sender.sendMessage("§cO valor de x não é um número.");
                return;
            }

            if (!y.isPresent()) {
                sender.sendMessage("§cO valor de y não é um número.");
                return;
            }

            if (!z.isPresent()) {
                sender.sendMessage("§cO valor de z não é um número.");
                return;
            }

            World world = args.length >= 5 ? Bukkit.getWorld(args[4]) : Bukkit.getWorlds().get(0);

            if (world == null) {
                sender.sendMessage(
                        " §c» §fUse §a/" + cmdArgs.getLabel() + " <nome> <x> <y> <z> para definir uma localização.");
                return;
            }

            float yaw = 0;
            float pitch = 0;

            try {
                if (args.length >= 6) yaw = Float.parseFloat(args[5]);
                if (args.length >= 7) pitch = Float.parseFloat(args[6]);
            } catch (Exception ignored) {
            }

            location = new Location(world, x.getAsDouble(), y.getAsDouble(), z.getAsDouble(), yaw, pitch);
        }

        Bukkit.getPluginManager().callEvent(new LocationChangeEvent(locationName,
                BukkitCommon.getInstance().getLocationManager().getLocation(locationName), location));

        BukkitCommon.getInstance().getLocationManager().setLocation(locationName, location);
        sender.sendMessage("§aLocalização " + locationName + " definida com sucesso em " + location.getX() + ", " +
                location.getY() + ", " + location.getZ() + " do mundo " + location.getWorld().getName() + ".");
    }

    @CommandFramework.Command(name = "vanish", aliases = { "v" }, permission = "command.vanish", console = false)
    public void vanishCommand(CommandArgs cmdArgs) {
        Player player = ((BukkitMember) cmdArgs.getSenderAsMember(BukkitMember.class)).getPlayer();
        String[] args = cmdArgs.getArgs();

        Group hidePlayer;

        if (args.length == 0) {
            if (BukkitCommon.getInstance().getVanishManager().isPlayerVanished(player.getUniqueId())) {
                BukkitCommon.getInstance().getVanishManager().showPlayer(player);
                player.sendMessage("§dVocê está visível para todos os jogadores.");
                return;
            }

            hidePlayer = BukkitCommon.getInstance().getVanishManager().hidePlayer(player);
        } else {
            Group group = CommonPlugin.getInstance().getPermissionManager().getGroupByName(args[0]).orElse(null);

            if (group == null) {
                player.sendMessage("§cO grupo " + StringFormat.formatString(args[0]) + " não existe.");
                return;
            }

            hidePlayer = group;
        }

        if (hidePlayer.getId() >= cmdArgs.getSender().getServerGroup().getId()) {
            player.sendMessage("§cVocê não pode ficar invisível para um grupo superior ao seu.");
            return;
        }

        BukkitCommon.getInstance().getVanishManager().setPlayerVanishToGroup(player, hidePlayer);
        player.sendMessage(
                "§dVocê está invisível para " + StringFormat.formatString(hidePlayer.getGroupName()) + "s e abaixo.");
    }


    @CommandFramework.Command(name = "stop", aliases = { "fechar", "restart" }, permission = "command.stop")
    public void stopCommand(CommandArgs cmdArgs) {
        Sender sender = cmdArgs.getSender();

        String reason = Joiner.on(' ').join(cmdArgs.getArgs());

        if (reason.isEmpty()) {
            Bukkit.broadcastMessage("§cO servidor " + CommonPlugin.getInstance().getServerId() + " foi fechado.");
        } else {
            Bukkit.broadcastMessage(
                    "§cO servidor " + CommonPlugin.getInstance().getServerId() + " foi fechado por " + reason);
        }

        CommonPlugin.getInstance().getPluginPlatform().runTimer(new Runnable() {

            int tries = 0;

            @Override
            public void run() {
                if (Bukkit.getOnlinePlayers().size() == 0 || tries == 5) {
                    Bukkit.shutdown();
                    return;
                }

                CommonPlugin.getInstance().getMemberManager().getMembers().forEach(player -> {
                    if (player instanceof BukkitMember) {
                        BukkitMember bukkitMember = (BukkitMember) player;

                        ServerType serverType = player.getLastServerType();

                        if (serverType == null) {
                            serverType = player.getCurrentServerType() == null ||
                                    !player.getCurrentServerType().hasParent() ? ServerType.LOBBY :
                                    player.getCurrentServerType().getParent();
                        }

                        BukkitCommon.getInstance().sendPlayerToServer(bukkitMember.getPlayer(), serverType);
                    }
                });

                if (tries++ >= 3) {
                    Bukkit.getOnlinePlayers().forEach(player -> player.kickPlayer(
                            "§cO servidor foi fechado." + (reason.isEmpty() ? "" : "\n§c\n§cMotivo: " + reason)));
                    Bukkit.shutdown();
                    return;
                }
            }
        }, 0, 20);
    }

    @SuppressWarnings("deprecation")
    @CommandFramework.Command(name = "gamemode", aliases = { "gm" }, permission = "command.gamemode")
    public void gamemodeCommand(CommandArgs cmdArgs) {
        Sender sender = cmdArgs.getSender();
        String[] args = cmdArgs.getArgs();

        if (args.length == 0) {
            sender.sendMessage(" §a» §f/" + cmdArgs.getLabel() +
                    " <creative:adventure:survival:spectator> @optional:<player> para alterar o modo de jogo de um " +
                    "jogador.");
            return;
        }

        GameMode gameMode = null;
        OptionalInt optionalInt = StringFormat.parseInt(args[0]);

        if (optionalInt.isPresent()) {
            gameMode = GameMode.getByValue(optionalInt.getAsInt());
        } else {
            try {
                gameMode = GameMode.valueOf(args[0].toUpperCase());
            } catch (Exception ignored) {
            }
        }

        if (gameMode == null) {
            sender.sendMessage("§cO modo de jogo " + args[0] + " não existe.");
            return;
        }

        Player target =
                args.length == 1 && sender.isPlayer() ? cmdArgs.getSenderAsMember(BukkitMember.class).getPlayer() :
                        Bukkit.getPlayer(args[1]);

        if (target == null) {
            sender.sendMessage("§cO jogador " + args[1] + " não foi encontrado.");
            return;
        }

        target.setGameMode(gameMode);

        if (target.getUniqueId().equals(sender.getUniqueId())) {
            sender.sendMessage("§aSeu gamemode foi alterado para " + StringFormat.formatString(gameMode.name()) + ".");
        } else {
            sender.sendMessage("§aO gamemode do jogador " + target.getName() + " foi alterado para " +
                    StringFormat.formatString(gameMode.name()) + ".");
        }
    }

    @CommandFramework.Command(name = "clear", permission = "command.clear")
    public void clearCommand(CommandArgs cmdArgs) {
        Sender sender = cmdArgs.getSender();
        String[] args = cmdArgs.getArgs();

        if (args.length == 0) {
            if (cmdArgs.isPlayer()) {
                Player player = cmdArgs.getSenderAsMember(BukkitMember.class).getPlayer();

                player.getInventory().clear();
                player.getInventory().setArmorContents(new ItemStack[4]);
                player.getActivePotionEffects().clear();

                sender.sendMessage("§aSeu inventário foi limpo.");
            } else {
                sender.sendMessage("§cVocê precisa ser um jogador para executar este comando.");
            }
        } else {
            Player player = Bukkit.getPlayer(args[0]);

            if (player == null) {
                sender.sendMessage("§cO jogador " + args[0] + " não foi encontrado.");
                return;
            }

            player.getInventory().clear();
            player.getInventory().setArmorContents(new ItemStack[4]);
            player.getActivePotionEffects().clear();
            sender.sendMessage("§aO inventário do jogador " + player.getName() + " foi limpo.");
        }
    }

    @CommandFramework.Command(name = "inventorysee", aliases = { "invsee", "inv" }, console = false,
            permission = "command.invsee")
    public void invseeCommand(CommandArgs cmdArgs) {
        Sender sender = cmdArgs.getSender();
        String[] args = cmdArgs.getArgs();

        if (args.length == 0) {
            sender.sendMessage(
                    " §a» §fUse §a/" + cmdArgs.getLabel() + " <player>§f para abrir o inventário do player.");
            return;
        }

        Player player = Bukkit.getPlayer(args[0]);

        if (player == null) {
            sender.sendMessage("§cO jogador " + args[0] + " não está online.");
            return;
        }

        cmdArgs.getSenderAsMember(BukkitMember.class).getPlayer().openInventory(player.getInventory());
    }

    @CommandFramework.Command(name = "teleport", aliases = { "tp" }, permission = "command.teleport")
    public void teleportCommand(CommandArgs cmdArgs) {
        Sender sender = cmdArgs.getSender();
        String[] args = cmdArgs.getArgs();

        switch (args.length) {
        case 1: {
            Player target = Bukkit.getPlayer(args[0]);

            if (target == null) {
                if (sender.isPlayer()) {
                    CommonPlugin.getInstance().getPacketManager().waitPacket(MemberTeleportResponse.class,
                            CommonPlugin.getInstance().getServerData()
                                        .sendPacket(new MemberTeleportRequest(sender.getUniqueId(), args[0])), 200,
                            packet -> {
                                if (packet == null) {
                                    sender.sendMessage("§cO jogador " + args[0] + " não foi encontrado.");
                                    return;
                                }
                            });
                } else {
                    sender.sendMessage("§cO jogador " + args[0] + " não foi encontrado.");
                }
                return;
            }

            if (sender.isPlayer()) {
                cmdArgs.getSenderAsMember(BukkitMember.class).getPlayer().teleport(target);
                sender.sendMessage("§aVocê foi teleportado para o jogador " + target.getName() + ".");
//                staffLog("O jogador " + sender.getName() + " teleportou-se para o jogador " + target.getName() + ".");
            } else {
                sender.sendMessage("§cVocê precisa ser um jogador para executar este comando.");
            }
            break;
        }
        case 2: {
//            if (args[0].equalsIgnoreCase("location")) {
//                if (cmdArgs.isPlayer()) {
//                    Player player = cmdArgs.getSenderAsMember(BukkitMember.class).getPlayer();
//                    String locationName = args[1];
//
//                    if (BukkitCommon.getInstance().getLocationManager().hasLocation(locationName)) {
//                        org.bukkit.Location location = BukkitCommon.getInstance().getLocationManager()
//                                                                   .getLocation(locationName);
//
//                        player.teleport(location);
//                        sender.sendMessage("§aVocê foi teleportado para a localização " + locationName + ".");
////                        staffLog("O jogador " + sender.getName() + " teleportou-se para a localização " +
// locationName +
////                                 ".");
//                    } else {
//                        sender.sendMessage("§cA localização " + locationName + " não foi encontrada.");
//                    }
//                } else {
//                    sender.sendMessage("§cVocê precisa ser um jogador para executar este comando.");
//                }
//                return;
//            }

            Player player = Bukkit.getPlayer(args[0]);

            if (player == null) {
                sender.sendMessage("§cO jogador " + args[0] + " não foi encontrado.");
                return;
            }

            Player target = Bukkit.getPlayer(args[1]);

            if (target == null) {
                sender.sendMessage("§cO jogador " + args[1] + " não foi encontrado.");
                return;
            }

            player.teleport(target);
            sender.sendMessage(
                    "§aVocê teleportou o jogador " + player.getName() + " para o jogador " + target.getName() + ".");
//            staffLog(
//                    "O jogador " + sender.getName() + " teleportou o jogador " + player.getName() + " para o
//                    jogador " +
//                    target.getName() + ".");
            break;
        }
        case 3: {
            if (sender.isPlayer()) {
                Player player = cmdArgs.getSenderAsMember(BukkitMember.class).getPlayer();

                OptionalDouble optionalX = StringFormat.parseDouble(args[0]);
                OptionalDouble optionalY = StringFormat.parseDouble(args[1]);
                OptionalDouble optionalZ = StringFormat.parseDouble(args[2]);

                double x, y, z;

                if (args[0].equals("~")) {
                    x = player.getLocation().getX();
                } else {
                    if (optionalX.isPresent()) {
                        x = optionalX.getAsDouble();
                    } else {
                        sender.sendMessage("§cO formato do número " + args[0] + " é inválido.");
                        return;
                    }
                }

                if (args[1].equals("~")) {
                    y = player.getLocation().getY();
                } else {
                    if (optionalY.isPresent()) {
                        y = optionalY.getAsDouble();
                    } else {
                        sender.sendMessage("§cO formato do número " + args[1] + " é inválido.");
                        return;
                    }
                }

                if (args[2].equals("~")) {
                    z = player.getLocation().getZ();
                } else {
                    if (optionalZ.isPresent()) {
                        z = optionalZ.getAsDouble();
                    } else {
                        sender.sendMessage("§cO formato do número " + args[2] + " é inválido.");
                        return;
                    }
                }

                org.bukkit.Location location = new org.bukkit.Location(player.getWorld(), x, y, z);

                if (!location.getChunk().isLoaded()) {
                    location.getChunk().load();
                }

                NumberFormat numberFormat = new DecimalFormat("#.##");

                player.setFallDistance(-1f);
                player.teleport(location);
                sender.sendMessage("§aVocê foi teleportado para a localização " + numberFormat.format(x) + ", " +
                        numberFormat.format(y) + ", " + numberFormat.format(z) + ".");

//                staffLog("O jogador " + sender.getName() + " teleportou-se para a localização " +
//                         numberFormat.format(x) + ", " + numberFormat.format(y) + ", " + numberFormat.format(z) + "
//                         .");
            } else {
                sender.sendMessage("§cVocê precisa ser um jogador para executar este comando.");
            }
            break;
        }
        default: {
            sender.sendMessage(
                    "§cUse /" + cmdArgs.getLabel() + " <jogador> para teleportar-se para um jogador." + "\n§cUse /" +
                            cmdArgs.getLabel() + " <jogador> <jogador> para teleportar um jogador para outro jogador." +
                            "\n§cUse /" + cmdArgs.getLabel() + " <x> <y> <z> para teleportar-se para uma localização." +
                            "\n§cUse /" + cmdArgs.getLabel() + " location <locationName> para teleportar-se para uma " +
                            "localização salva.");
            break;
        }
        }
    }

    @CommandFramework.Command(name = "whitelist", aliases = { "wt" }, permission = "command.whitelist", runAsync = true)
    public void whitelistCommand(CommandArgs cmdArgs) {
        Sender sender = cmdArgs.getSender();
        String[] args = cmdArgs.getArgs();

        if (args.length == 0) {
            sender.sendMessage(
                    " §a» §fUse §a/" + cmdArgs.getLabel() + " <add|remove> <player>§f para gerenciar a whitelist.");
            sender.sendMessage(
                    " §a» §fUse §a/" + cmdArgs.getLabel() + " <on|off>§f para ativar ou desativar a whitelist.");
            sender.sendMessage(
                    " §a» §fUse §a/" + cmdArgs.getLabel() + " list§f para ver a lista de jogadores na whitelist.");
            return;
        }

        switch (args[0].toLowerCase()) {
        case "list": {
            if (Bukkit.getWhitelistedPlayers().isEmpty()) {
                sender.sendMessage("§cNenhum jogador está na whitelist.");
                return;
            }

            sender.sendMessage("§aJogadores na whitelist:");
            sender.sendMessage("§f" + Joiner.on("§f, §f")
                                            .join(Bukkit.getWhitelistedPlayers().stream().map(OfflinePlayer::getName)
                                                        .collect(Collectors.toList())));
            break;
        }
        case "on": {
            MinecraftServer.getServer().getPlayerList().setHasWhitelist(true);
            //CommonPlugin.getInstance().getServerData().setJoinEnabled(false);
            sender.sendMessage("§aA whitelist foi ativada.");
            break;
        }
        case "off": {
            MinecraftServer.getServer().getPlayerList().setHasWhitelist(false);
            //CommonPlugin.getInstance().getServerData().setJoinEnabled(true);
            sender.sendMessage("§aA whitelist foi desativada.");
            break;
        }
        case "remove":
        case "add": {
            if (args.length == 1) {
                sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() + " " + args[0] +
                        " <player>§f para adicionar/remover um jogador da whitelist.");
                return;
            }

            GameProfile gameProfile = MinecraftServer.getServer().getUserCache().getProfile(args[1]);

            if (gameProfile == null) {
                sender.sendMessage("§cO jogador " + args[1] + " não existe.");
                return;
            }

            if (args[0].equalsIgnoreCase("add")) {
                if (Bukkit.getWhitelistedPlayers().stream()
                          .anyMatch(offlinePlayer -> offlinePlayer.getName().equalsIgnoreCase(args[1]))) {
                    sender.sendMessage("§cO jogador " + args[1] + " já está na whitelist.");
                    return;
                }

                MinecraftServer.getServer().getPlayerList().addWhitelist(gameProfile);
                sender.sendMessage("§aO jogador " + args[1] + " foi adicionado na whitelist.");
            } else {
                if (Bukkit.getWhitelistedPlayers().stream()
                          .noneMatch(offlinePlayer -> offlinePlayer.getName().equalsIgnoreCase(args[1]))) {
                    sender.sendMessage("§cO jogador " + args[1] + " não está na whitelist.");
                    return;
                }

                MinecraftServer.getServer().getPlayerList().removeWhitelist(gameProfile);
                sender.sendMessage("§aO jogador " + args[1] + " foi removido da whitelist.");
            }
            break;
        }
        }
    }

    @CommandFramework.Completer(name = "gamemode", aliases = { "gm" })
    public List<String> gamemodeCompleter(CommandArgs cmdArgs) {
        List<String> returnList = new ArrayList<>();

        if (cmdArgs.getArgs().length == 1) {
            returnList.addAll(Arrays.stream(GameMode.values()).map(GameMode::name)
                                    .filter(name -> name.toLowerCase().startsWith(cmdArgs.getArgs()[0].toLowerCase()))
                                    .collect(Collectors.toList()));
        } else if (cmdArgs.getArgs().length == 2) {
            returnList.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName)
                                    .filter(name -> name.toLowerCase().startsWith(cmdArgs.getArgs()[1].toLowerCase()))
                                    .collect(Collectors.toList()));
        }

        return returnList;
    }

    @CommandFramework.Completer(name = "setlocation", aliases = { "setloc" })
    public List<String> setlocationCompleter(CommandArgs cmdArgs) {
        if (cmdArgs.getArgs().length == 1) {
            return BukkitCommon.getInstance().getLocationManager().getKeys().stream()
                               .filter(name -> name.toLowerCase().startsWith(cmdArgs.getArgs()[0].toLowerCase()))
                               .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
