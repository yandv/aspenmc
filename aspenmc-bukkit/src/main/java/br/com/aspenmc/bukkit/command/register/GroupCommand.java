package br.com.aspenmc.bukkit.command.register;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bukkit.command.BukkitCommandFramework;
import br.com.aspenmc.bukkit.entity.BukkitMember;
import br.com.aspenmc.command.CommandArgs;
import br.com.aspenmc.command.CommandFramework;
import br.com.aspenmc.command.CommandHandler;
import br.com.aspenmc.entity.sender.Sender;
import br.com.aspenmc.entity.sender.member.Member;
import br.com.aspenmc.packet.type.member.MemberGroupChange;
import br.com.aspenmc.permission.Group;
import br.com.aspenmc.permission.GroupInfo;
import br.com.aspenmc.permission.Tag;
import br.com.aspenmc.utils.string.MessageBuilder;
import br.com.aspenmc.utils.string.StringFormat;
import com.google.common.base.Joiner;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class GroupCommand implements CommandHandler {

    @CommandFramework.Command(name = "permission", aliases = { "perm" }, permission = "command.permission")
    public void permissionCommand(CommandArgs cmdArgs) {
        Sender sender = cmdArgs.getSender();
        String[] args = cmdArgs.getArgs();

        if (args.length == 0) {
            sender.sendMessage(
                    " §a» §fUse §a/" + cmdArgs.getLabel() + " <player> §fpara ver as permissões de um jogador.");
            sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() +
                                       " <player> clear §fpara limpar as permissões de um jogador.");
            sender.sendMessage(
                    " §a» §fUse §a/" + cmdArgs.getLabel() + " <player> <add:remove> <permission> §fpara adicionar ou " +
                            "remover uma permissão de um jogador.");
            return;
        }

        Member member = CommonPlugin.getInstance().getMemberManager().getOrLoadByName(args[0]).orElse(null);

        if (member == null) {
            sender.sendMessage("§cO jogador \"" + args[0] + "\" não existe.");
            return;
        }

        if (args.length == 1) {
            boolean allPermissions = member.getPermissions().containsKey("*") ||
                    member.getGroups().stream().anyMatch(group -> group.getPermissions().contains("*"));
            sender.sendMessage(
                    "§aPermissões do jogador " + member.getName() + (allPermissions ? " §e(todas as permissões)" : ""));

            if (!member.getPermissions().isEmpty()) {
                sender.sendMessage(
                        "  §fPermissões individuais: §a" + Joiner.on(", ").join(member.getPermissions().keySet()));
            }
            if (!member.getGroups().isEmpty()) {
                member.getGroups().stream().filter(group -> !group.getPermissions().isEmpty()).forEach(group -> {
                    sender.sendMessage("  §fPermissões " + group.getGroupTag().map(Tag::getRealPrefix)
                                                                .orElse(group.getGroupName()) + ": §a" +
                                               Joiner.on(", ").join(group.getPermissions()));
                });
            }
            return;
        }

        if (args[1].equalsIgnoreCase("clear")) {
            if (!sender.hasPermission("command.permission.clear")) {
                sender.sendMessage("§cVocê não tem permissão para limpar as permissões de um jogador.");
                return;
            }

            member.clearPermissions();
            sender.sendMessage("§aAs permissões do jogador " + member.getName() + " foram limpas.");
            return;
        }

        if (!sender.hasPermission("command.permission.add") && !sender.hasPermission("command.permission.remove")) {
            sender.sendMessage("§cVocê não tem permissão para adicionar ou remover permissões de um jogador.");
            return;
        }

        if (args.length == 2) {
            sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() +
                                       " <player> <add:remove> <permission> §fpara adicionar ou remover uma " +
                                       "permissão de um jogador.");
            return;
        }

        if (!Arrays.asList("add", "remove").contains(args[1].toLowerCase())) {
            sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() +
                                       " <player> <add:remove> <permission> §fpara adicionar ou remover uma " +
                                       "permissão de um jogador.");
            return;
        }

        boolean add = args[1].equalsIgnoreCase("add");

        if (add) {
            if (member.addPermission(args[2])) {
                sender.sendMessage("§aA permissão " + args[2] + " foi adicionada ao jogador " + member.getName() + ".");
            } else {
                sender.sendMessage("§cO jogador " + member.getName() + " já possui a permissão " + args[2] + ".");
            }
        } else {
            if (member.removePermission(args[2])) {
                sender.sendMessage("§aA permissão " + args[2] + " foi removida do jogador " + member.getName() + ".");
            } else {
                sender.sendMessage("§cO jogador " + member.getName() + " não possui a permissão " + args[2] + ".");
            }
        }
    }

    @CommandFramework.Command(name = "group", aliases = { "groups", "grupo", "grupos" }, permission = "command.group")
    public void groupCommand(CommandArgs cmdArgs) {
        Sender sender = cmdArgs.getSender();
        String[] args = cmdArgs.getArgs();

        if (args.length == 0) {
            sender.sendMessage(
                    " §a» §fUse §a/" + cmdArgs.getLabel() + " criar <id> <groupName> §fpara criar um grupo.");
            sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() + " delete <groupName> §fpara deletar um grupo.");
            sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() +
                                       " settag <groupName> <tag> §fpara definir uma tag para um grupo.");
            sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() +
                                       " permission <groupName:player> <add:remove> <permission> §fpara adicionar ou " +
                                       "remover uma permissão de " + "um grupo.");
            sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() +
                                       " setdefault <groupName> <true:false> §fpara definir um grupo como padrão.");
            sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() +
                                       " setpaid <groupName> <true:false> §fpara definir um grupo como vip.");
            sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() +
                                       " setstaff <nome> <true:false> §fpara definir se um grupo é da equipe ou não.");

            sender.sendMessage("");
            sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() + " <player>§f para ver o grupo de um jogador.");
            sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() +
                                       " <player> set <group>§f para definir o grupo de um jogador.");
            sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() +
                                       " <player> add <group>§f para adicionar um grupo a um jogador.");
            sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() +
                                       " <player> remove <group>§f para remover um grupo de um jogador.");
            return;
        }

        switch (args[0].toLowerCase()) {
        case "permission": {
            if (args.length == 1) {
                sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() +
                                           " permission <groupName> <add:remove> <permission> §fpara adicionar ou " +
                                           "remover uma permissão " + "de um grupo.");
                break;
            }

            Optional<Group> optionalGroup = CommonPlugin.getInstance().getPermissionManager().getGroupByName(args[1]);

            if (!optionalGroup.isPresent()) {
                sender.sendMessage("§cO grupo §f" + args[1] + " §cnão existe.");
                return;
            }

            Group group = optionalGroup.get();

            if (args.length <= 3) {
                sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() + " permission " + group.getGroupName() +
                                           " <add:remove> <permission> §fpara adicionar ou remover uma permissão de " +
                                           "um grupo.");
                break;
            }

            switch (args[2].toLowerCase()) {
            case "add": {
                if (group.hasPermission(args[3])) {
                    sender.sendMessage(
                            "§cO grupo §f" + group.getGroupName() + " §cjá possui a permissão §f" + args[3] + "§c.");
                    return;
                }

                group.addPermission(args[3]);
                sender.sendMessage(
                        "§aA permissão §f" + args[3] + " §afoi adicionada ao grupo §f" + group.getGroupName() + "§a.");
                break;
            }
            case "remove": {
                if (!group.hasPermission(args[3])) {
                    sender.sendMessage(
                            "§cO grupo §f" + group.getGroupName() + " §cnão possui a permissão §f" + args[3] + "§c.");
                    return;
                }

                group.removePermission(args[3]);
                sender.sendMessage(
                        "§aA permissão §f" + args[3] + " §afoi removida do grupo §f" + group.getGroupName() + "§a.");
                break;
            }
            default: {
                sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() + " permission " + group.getGroupName() +
                                           " <add:remove> <permission> §fpara adicionar ou remover uma permissão de " +
                                           "um grupo.");
                break;
            }
            }
            break;
        }
        case "list": {
            if (CommonPlugin.getInstance().getPermissionManager().getGroups().isEmpty()) {
                sender.sendMessage("§cNão há nenhum grupo registrado.");
                return;
            }

            sender.sendMessage("§eGrupos: §f" + CommonPlugin.getInstance().getPermissionManager().getGroups().size());

            CommonPlugin.getInstance().getPermissionManager().getGroups().stream()
                        .sorted(Comparator.comparingInt(Group::getId)).forEach(group -> {
                            StringBuilder hoverText = new StringBuilder();

                            hoverText.append("§fGrupo: §7").append(group.getGroupName()).append("\n");
                            hoverText.append("§fID: §7").append(group.getId()).append("\n\n");
                            hoverText.append("§fTag: §7").append(group.getGroupTag().map(Tag::getRealPrefix).orElse(
                                    "Sem tag"))
                                     .append("\n\n");
                            hoverText.append("§fPermissões: §7");

                            if (group.getPermissions().isEmpty()) {
                                hoverText.append("Sem permissões");
                            } else {
                                hoverText.append(Joiner.on(", ")
                                                       .join(Arrays.copyOfRange(group.getPermissions().toArray(new String[0]), 0,
                                                                                Math.min(5,
                                                                                         group.getPermissions().size()))));

                                if (group.getPermissions().size() > 5) {
                                    hoverText.append(" e outras +").append(group.getPermissions().size() - 5).append(
                                            " permissões");
                                }
                            }

                            sender.sendMessage(
                                    new MessageBuilder(" §a» §f").append("§f" + group.getGroupName(),
                                                                         hoverText.toString())
                                                                 .create());
                        });
            break;
        }
        case "delete": {
            if (args.length < 2) {
                sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() + " delete <nome> §fpara deletar um grupo.");
                break;
            }

            Optional<Group> optionalGroup = CommonPlugin.getInstance().getPermissionManager().getGroupByName(args[1]);

            if (!optionalGroup.isPresent()) {
                sender.sendMessage("§cO grupo \"" + args[1] + "\" não existe.");
                return;
            }

            Group group = optionalGroup.get();

            if (group.isDefaultGroup()) {
                sender.sendMessage(
                        "§cO grupo \"" + group.getGroupName() + "\" é o grupo padrão e não pode ser deletado.");
                return;
            }

            sender.sendMessage("§aO grupo \"" + group.getGroupName() + "\" foi deletado com sucesso.");
            CommonPlugin.getInstance().getPermissionManager().unloadGroup(group.getGroupName());
            CommonPlugin.getInstance().getPermissionService().deleteGroup(group);
            break;
        }
        case "criar":
        case "create": {
            if (args.length == 1) {
                sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() + " criar <id> <nome> §fpara criar um grupo.");
                return;
            }

            OptionalInt optionalInt = StringFormat.parseInt(args[1]);

            if (!optionalInt.isPresent()) {
                sender.sendMessage("§cO id \"" + args[1] + "\" não é um número.");
                break;
            }

            int groupId = optionalInt.getAsInt();

            if (CommonPlugin.getInstance().getPermissionManager().getGroupById(groupId).isPresent()) {
                sender.sendMessage("§cO id \"" + groupId + "\" já está em uso.");
                break;
            }

            if (args.length == 2) {
                sender.sendMessage(
                        " §a» §fUse §a/" + cmdArgs.getLabel() + " criar " + args[1] + " <nome> §fpara criar um grupo.");
                return;
            }

            String groupName = args[2];

            if (CommonPlugin.getInstance().getPermissionManager().getGroupByName(groupName).isPresent()) {
                sender.sendMessage("§cO grupo \"" + groupName + "\" já existe.");
                break;
            }

            Group group = new Group(groupId, StringFormat.formatString(groupName), new HashSet<>(), null, false, false,
                                    false);

            sender.sendMessage("§aO grupo \"" + groupName + "\" foi criado com sucesso.");
            CommonPlugin.getInstance().getPermissionManager().loadGroup(group);
            CommonPlugin.getInstance().getPermissionService().createGroup(group);
            break;
        }
        case "settag": {
            if (args.length == 1) {
                sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() +
                                           " settag <nome> <tag> §fpara definir uma tag para um grupo.");
                break;
            }

            Optional<Group> optionalGroup = CommonPlugin.getInstance().getPermissionManager().getGroupByName(args[1]);

            if (!optionalGroup.isPresent()) {
                sender.sendMessage("§cO grupo \"" + args[1] + "\" não existe.");
                return;
            }

            Group group = optionalGroup.get();

            if (args.length == 2) {
                sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() + " settag " + group.getGroupName() +
                                           " <tag> §fpara definir uma tag para um grupo.");
                return;
            }

            Optional<Tag> optionalTag = CommonPlugin.getInstance().getPermissionManager().getTagByName(args[2]);

            if (!optionalTag.isPresent()) {
                sender.sendMessage("§cA tag \"" + args[2] + "\" não existe.");
                return;
            }

            Tag tag = optionalTag.get();

            group.setTag(tag);
            sender.sendMessage(
                    "§aA tag do grupo \"" + group.getGroupName() + "\" foi alterada para \"" + tag.getTagName() +
                            "\".");
            break;
        }
        case "removetag": {
            if (args.length < 2) {
                sender.sendMessage(
                        " §a» §fUse §a/" + cmdArgs.getLabel() + " removetag <nome> §fpara remover a tag de um grupo.");
                break;
            }

            Optional<Group> optionalGroup = CommonPlugin.getInstance().getPermissionManager().getGroupByName(args[1]);

            if (!optionalGroup.isPresent()) {
                sender.sendMessage("§cO grupo \"" + args[1] + "\" não existe.");
                return;
            }

            Group group = optionalGroup.get();

            if (!group.hasTag()) {
                sender.sendMessage("§cO grupo \"" + group.getGroupName() + "\" não possui uma tag.");
                return;
            }

            group.setTag(null);
            sender.sendMessage("§aA tag do grupo \"" + group.getGroupName() + "\" foi removida.");
            break;
        }
        case "setdefault": {
            if (args.length == 1) {
                sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() +
                                           " setdefault <nome> <true:false> §fpara definir se um grupo é o grupo " +
                                           "padrão.");
                break;
            }

            Optional<Group> optionalGroup = CommonPlugin.getInstance().getPermissionManager().getGroupByName(args[1]);

            if (!optionalGroup.isPresent()) {
                sender.sendMessage("§cO grupo \"" + args[1] + "\" não existe.");
                return;
            }

            Group group = optionalGroup.get();

            if (args.length == 2) {
                sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() + " setdefault " + group.getGroupName() +
                                           " <true:false> §fpara definir se um grupo é o grupo padrão.");
                return;
            }

            boolean defaultGroup = StringFormat.parseBoolean(args[2]);

            group.setDefaultGroup(defaultGroup);
            sender.sendMessage("§aO grupo \"" + group.getGroupName() + "\" " + (defaultGroup ? "agora é" : "não é") +
                                       " o grupo padrão.");
            break;
        }
        case "setpaid": {
            if (args.length == 1) {
                sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() +
                                           " setpaid <nome> <true:false> §fpara definir se um grupo é vip ou não.");
                break;
            }

            Optional<Group> optionalGroup = CommonPlugin.getInstance().getPermissionManager().getGroupByName(args[1]);

            if (!optionalGroup.isPresent()) {
                sender.sendMessage("§cO grupo \"" + args[1] + "\" não existe.");
                return;
            }

            Group group = optionalGroup.get();

            if (args.length == 2) {
                sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() + " setpaid " + group.getGroupName() +
                                           " <true:false> §fpara definir se um grupo é vip ou não.");
                return;
            }

            boolean paidGroup = StringFormat.parseBoolean(args[2]);

            group.setPaidGroup(paidGroup);
            sender.sendMessage("§aO grupo \"" + group.getGroupName() + "\" " + (paidGroup ? "agora é" : "agora não é") +
                                       " um grupo vip.");
            break;
        }
        case "setstaff": {
            if (args.length == 1) {
                sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() +
                                           " setstaff <nome> <true:false> §fpara definir se um grupo é da equipe ou " +
                                           "não.");
                break;
            }

            Optional<Group> optionalGroup = CommonPlugin.getInstance().getPermissionManager().getGroupByName(args[1]);

            if (!optionalGroup.isPresent()) {
                sender.sendMessage("§cO grupo \"" + args[1] + "\" não existe.");
                return;
            }

            Group group = optionalGroup.get();

            if (args.length == 2) {
                sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() + " setstaff " + group.getGroupName() +
                                           " <true:false> §fpara definir se um grupo é da equipe ou não.");
                return;
            }

            boolean staff = StringFormat.parseBoolean(args[2]);

            group.setStaff(staff);
            sender.sendMessage("§aO grupo \"" + group.getGroupName() + "\" " + (staff ? "agora é" : "agora não é") +
                                       " um grupo da equipe.");
            break;
        }
        default: {
            Member member = CommonPlugin.getInstance().getMemberManager().getOrLoadByName(args[0]).orElse(null);

            if (member == null) {
                sender.sendMessage("§cO jogador \"" + args[0] + "\" não existe.");
                return;
            }

            if (args.length == 1) {
                GroupInfo groupInfo = member.getGroupInfo(member.getServerGroup()).get();

                sender.sendMessage("§aGrupo do " + member.getName());
                sender.sendMessage("  §fGrupo atual: §a" +
                                           (member.getServerGroup().getGroupTag().map(Tag::getColoredName)
                                                  .orElse(member.getServerGroup().getGroupName())));
                if (!groupInfo.isPermanent()) {
                    sender.sendMessage("  §fExpira em: §a" + StringFormat.formatTime(
                            (groupInfo.getExpiresAt() - System.currentTimeMillis()) / 1000));
                }

                for (Map.Entry<String, GroupInfo> entry : member.getGroupMap().entrySet()) {
                    if (entry.getKey().equalsIgnoreCase(member.getServerGroup().getGroupName())) {
                        continue;
                    }

                    member.sendMessage("");
                }

                return;
            }

            switch (args[1].toLowerCase()) {
            case "set": {
                if (args.length == 2) {
                    sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() + " " + sender.getName() +
                                               " set <group>§f para setar um grupo ao jogador.");
                    return;
                }

                OptionalInt optionalInt = StringFormat.parseInt(args[2]);
                Optional<Group> groupOptional = optionalInt.isPresent() ?
                        CommonPlugin.getInstance().getPermissionManager().getGroupById(optionalInt.getAsInt()) :
                        CommonPlugin.getInstance().getPermissionManager().getGroupByName(args[2]);

                if (!groupOptional.isPresent()) {
                    sender.sendMessage("§cO grupo " + args[2] + " não existe.");
                    return;
                }

                Group group = groupOptional.get();

                member.setServerGroup(group, sender);
                member.setTag(member.getDefaultTag());
                sender.sendMessage(
                        "§aO grupo do jogador " + member.getName() + " foi alterado para " + group.getGroupName() +
                                ".");
                member.sendMessage("§aVocê recebeu o grupo " + group.getGroupName() + ".");
                CommonPlugin.getInstance().getPluginPlatform().runAsync(
                        () -> CommonPlugin.getInstance().getServerService().sendPacket(
                                new MemberGroupChange(member.getUniqueId(), group.getGroupName(), -1L, -1L,
                                                      MemberGroupChange.GroupAction.SET)));
                break;
            }
            case "add": {
                if (args.length == 2) {
                    sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() + " " + sender.getName() +
                                               " add <group>§f para setar um grupo ao jogador.");
                    return;
                }

                OptionalInt optionalInt = StringFormat.parseInt(args[2]);
                Optional<Group> groupOptional = optionalInt.isPresent() ?
                        CommonPlugin.getInstance().getPermissionManager().getGroupById(optionalInt.getAsInt()) :
                        CommonPlugin.getInstance().getPermissionManager().getGroupByName(args[2]);

                if (!groupOptional.isPresent()) {
                    sender.sendMessage("§cO grupo " + args[2] + " não existe.");
                    return;
                }

                Group group = groupOptional.get();
                long expiresAt = args.length == 3 ? -1L : StringFormat.getTimeFromString(args[3], true);

                member.addServerGroup(group, sender, expiresAt);
                member.setTag(member.getDefaultTag());
                sender.sendMessage(
                        "§aO grupo do jogador " + member.getName() + " foi alterado para " + group.getGroupName() +
                                ".");
                member.sendMessage("§aVocê recebeu o grupo " + group.getGroupName() + " com duração de " +
                                           StringFormat.formatTime((expiresAt - System.currentTimeMillis()) / 1000));
                CommonPlugin.getInstance().getPluginPlatform().runAsync(
                        () -> CommonPlugin.getInstance().getServerService().sendPacket(
                                new MemberGroupChange(member.getUniqueId(), group.getGroupName(), expiresAt,
                                                      expiresAt == -1L ? -1L : expiresAt - System.currentTimeMillis(),
                                                      MemberGroupChange.GroupAction.ADD)));
                break;
            }
            case "remove": {
                if (args.length == 2) {
                    sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() + " " + sender.getName() +
                                               " remove <group>§f para setar um grupo ao jogador.");
                    return;
                }

                OptionalInt optionalInt = StringFormat.parseInt(args[2]);
                Optional<Group> groupOptional = optionalInt.isPresent() ?
                        CommonPlugin.getInstance().getPermissionManager().getGroupById(optionalInt.getAsInt()) :
                        CommonPlugin.getInstance().getPermissionManager().getGroupByName(args[2]);

                if (!groupOptional.isPresent()) {
                    sender.sendMessage("§cO grupo " + args[2] + " não existe.");
                    return;
                }

                Group group = groupOptional.get();

                if (!member.getServerGroup().equals(group)) {
                    sender.sendMessage(
                            "§cO jogador " + member.getName() + " não está no grupo " + group.getGroupName() + ".");
                    return;
                }

                member.removeServerGroup(group);
                member.setTag(member.getDefaultTag());
                sender.sendMessage(
                        "§aO grupo do jogador " + member.getName() + " foi alterado para " + group.getGroupName() +
                                ".");
                member.sendMessage("§cO seu grupo " + group.getGroupName() + " foi removido.");
                CommonPlugin.getInstance().getPluginPlatform().runAsync(
                        () -> CommonPlugin.getInstance().getServerService().sendPacket(
                                new MemberGroupChange(member.getUniqueId(), group.getGroupName(), -1, -1,
                                                      MemberGroupChange.GroupAction.REMOVE)));
                break;
            }
            default: {
                sender.sendMessage(" §a» ");
                break;
            }
            }
        }
        }
    }

    @CommandFramework.Command(name = "tag")
    public void tagCommand(CommandArgs cmdArgs) {
        Sender sender = cmdArgs.getSender();
        String[] args = cmdArgs.getArgs();

        if (args.length == 0) {
            if (sender instanceof Member) {
                Member member = (Member) sender;
                List<Tag> tagList = CommonPlugin.getInstance().getPermissionManager().getTags().stream()
                                                .filter(member::hasTag).sorted(Comparator.comparingInt(Tag::getId))
                                                .collect(Collectors.toList());

                if (tagList.isEmpty()) {
                    sender.sendMessage(sender.t("command.tag.no-tags"));
                    return;
                }

                MessageBuilder messageBuilder = new MessageBuilder(member.t("command.tag.list-prefix"));

                for (int i = 0; i < tagList.size(); i++) {
                    Tag tag = tagList.get(i);

                    messageBuilder.append(new MessageBuilder(tag.getColoredName()).setHoverEvent(
                            member.t("command.tag.example", "%tagPrefix%", tag.getTagPrefix(), "%player%",
                                     sender.getName())).setClickEvent("/tag " + tag.getTagName()));

                    if (i == tagList.size() - 1) {
                        messageBuilder.append("§f.");
                    } else {
                        messageBuilder.append("§f, ");
                    }
                }

                sender.sendMessage(messageBuilder.create());
            }
            return;
        }

        switch (args[0].toLowerCase()) {
        case "help":
        case "ajuda": {
            if (sender.hasPermission("command.tag.create")) {
                sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() +
                                           " criar <tagName> <tagId> <tagPrefix> §fpara criar uma tag.");
            }

            if (sender.hasPermission("command.tag.delete")) {
                sender.sendMessage(
                        " §a» §fUse §a/" + cmdArgs.getLabel() + " deletar <tagName> §fpara deletar uma tag.");
            }

            if (sender.hasPermission("command.tag.setprefix")) {
                sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() +
                                           " setprefix <tagName> <tagPrefix> §fpara definir o prefixo de uma tag.");
            }
            break;
        }
        case "listar":
        case "list": {
            if (!sender.hasPermission("command.tag.list")) {
                sender.sendMessage("§cVocê não possui permissão para executar este comando.");
                return;
            }

            if (CommonPlugin.getInstance().getPermissionManager().getTags().isEmpty()) {
                sender.sendMessage("§cNão há nenhuma tag criada.");
                return;
            }

            sender.sendMessage("§aTags disponíveis: §f" +
                                       CommonPlugin.getInstance().getPermissionManager().getTags().stream()
                                                   .map(Tag::getTagName).collect(Collectors.joining(", ")));
            break;
        }
        case "criar":
        case "create": {
            if (!sender.hasPermission("command.tag.create")) {
                sender.sendMessage("§cVocê não possui permissão para executar este comando.");
                return;
            }


            if (args.length == 1) {
                sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() +
                                           " criar <tagName> <tagId> <tagPrefix> §fpara criar uma tag.");
                return;
            }

            String tagName = args[1];

            if (CommonPlugin.getInstance().getPermissionManager().getTagByName(tagName).isPresent()) {
                sender.sendMessage("§cA tag \"" + tagName + "\" já existe.");
                return;
            }

            if (args.length == 2) {
                sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() + " criar " + tagName +
                                           " <tagId> <tagPrefix> §fpara criar uma tag.");
                return;
            }

            OptionalInt optionalInt = StringFormat.parseInt(args[2]);

            if (!optionalInt.isPresent()) {
                sender.sendMessage("§cO id \"" + args[2] + "\" não é um número.");
                return;
            }

            int tagId = optionalInt.getAsInt();

            if (args.length == 3) {
                sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() + " criar " + tagName + " " + tagId +
                                           " <tagPrefix> §fpara criar uma tag.");
                return;
            }

            String tagPrefix = Joiner.on(' ').join(Arrays.copyOfRange(args, 3, args.length)).replace('&', '§');

            Tag tag = new Tag(tagId, StringFormat.formatString(tagName), tagPrefix, new HashSet<>());
            sender.sendMessage("§aA tag §e\"" + tag.getTagName() + "\"§a com o prefixo §e\"" + tag.getTagPrefix() +
                                       "§e\" e id §e\"" + tag.getId() + "\"§a foi criada.");
            CommonPlugin.getInstance().getPermissionManager().loadTag(tag);
            CommonPlugin.getInstance().getPermissionService().createTag(tag);
            break;
        }
        case "delete": {
            if (!sender.hasPermission("command.tag.delete")) {
                sender.sendMessage("§cVocê não possui permissão para executar este comando.");
                return;
            }

            if (args.length == 1) {
                sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() + " delete <tagName> §fpara remover uma tag.");
                return;
            }

            Tag tag = CommonPlugin.getInstance().getPermissionManager().getTagByName(args[1]).orElse(null);

            if (tag == null) {
                sender.sendMessage("§cA tag \"" + args[1] + "\" não existe.");
                return;
            }

            sender.sendMessage("§aA tag §e\"" + tag.getTagName() + "\"§a foi deletada.");
            CommonPlugin.getInstance().getPermissionManager().unloadTag(tag.getTagName());
            CommonPlugin.getInstance().getPermissionService().deleteTag(tag);
            break;
        }
        case "setprefix": {
            if (!sender.hasPermission("command.tag.setprefix")) {
                sender.sendMessage("§cVocê não possui permissão para executar este comando.");
                return;
            }

            if (args.length == 1) {
                sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() +
                                           " setprefix <tagName> <tagPrefix> §fpara definir o prefixo de uma tag.");
                return;
            }

            Tag tag = CommonPlugin.getInstance().getPermissionManager().getTagByName(args[1]).orElse(null);

            if (tag == null) {
                sender.sendMessage("§cA tag \"" + args[1] + "\" não existe.");
                return;
            }

            if (args.length == 2) {
                sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() + " setprefix " + tag.getTagName() +
                                           " <tagPrefix> §fpara definir o prefixo de uma tag.");
                return;
            }

            String tagPrefix = Joiner.on(' ').join(Arrays.copyOfRange(args, 2, args.length)).replace('&', '§');

            tag.setTagPrefix(tagPrefix);
            sender.sendMessage(
                    "§aO prefixo da tag §e\"" + tag.getTagName() + "\"§a foi alterado para §e\"" + tag.getTagPrefix() +
                            "§e\".");
            break;
        }
        default: {
            Tag tag = CommonPlugin.getInstance().getPermissionManager().getTagByName(args[0]).orElse(null);

            if (tag == null) {
                sender.sendMessage("§cA tag \"" + args[0] + "\" não existe.");
                return;
            }

            if (args.length == 1 || !sender.hasPermission("command.tag.forcetag")) {
                if (!sender.isPlayer()) {
                    sender.sendMessage("§cUso correto: /tag <tag> <jogador>");
                    return;
                }

                if (!((Member) sender).hasTag(tag)) {
                    sender.sendMessage("§cVocê não possui a tag " + tag.getColoredName() + "§c.");
                    return;
                }

                if (((Member) sender).setTag(tag)) {
                    sender.sendMessage("§aSua tag foi alterada para " + tag.getColoredName() + "§a.");
                }
                return;
            }

            Member member = CommonPlugin.getInstance().getMemberManager().getMemberByName(args[1]).orElse(null);

            if (member == null) {
                sender.sendMessage("§cO jogador \"" + args[1] + "\" não existe.");
                return;
            }

            if (((BukkitMember) member).setTag(tag, true)) {
                sender.sendMessage(
                        "§aA tag de " + member.getName() + " foi alterada para " + tag.getColoredName() + "§a.");
                member.sendMessage("§aSua tag foi alterada para " + tag.getColoredName() + "§a.");
            } else {
                sender.sendMessage("§cNão foi possível alterar a tag de " + member.getName() + "§c.");
            }
        }
        }
    }

    @CommandFramework.Completer(name = "group")
    public List<String> groupCompleter(CommandArgs cmdArgs) {
        String[] args = cmdArgs.getArgs();
        List<String> completer = new ArrayList<>();


        if (args.length == 1) {
            completer.addAll(Arrays.asList("criar", "delete", "permission", "settag", "setdefault", "setpaid"));
            completer.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
            case "permission":
            case "settag":
            case "setdefault":
            case "setpaid":
            case "removetag":
            case "delete": {
                completer.addAll(
                        CommonPlugin.getInstance().getPermissionManager().getGroups().stream().map(Group::getGroupName)
                                    .collect(Collectors.toList()));
                break;
            }
            case "criar":
                break;
            default: {
                completer.add("set");
                completer.add("add");
                completer.add("remove");
                break;
            }
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("permission")) {
                completer.add("add");
                completer.add("remove");
            } else {
                if (args[2].equalsIgnoreCase("set") || args[2].equalsIgnoreCase("add") ||
                        args[2].equalsIgnoreCase("remove")) {
                    completer.addAll(CommonPlugin.getInstance().getPermissionManager().getGroups().stream()
                                                 .map(Group::getGroupName).collect(Collectors.toList()));
                }
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("permission")) {
                if (args[2].equalsIgnoreCase("add")) {
                    completer.addAll(BukkitCommandFramework.INSTANCE.getCommands().stream().map(Command::getPermission)
                                                                    .collect(Collectors.toList()));
                } else {
                    completer.addAll(CommonPlugin.getInstance().getPermissionManager().getGroupByName(args[1])
                                                 .map(Group::getPermissions).orElse(new HashSet<>()));
                }
            }
        }

        return completer.stream().filter(complete -> complete.toLowerCase().startsWith(args[args.length - 1]))
                        .collect(Collectors.toList());
    }

    @CommandFramework.Completer(name = "permission")
    public List<String> permissionCompleter(CommandArgs cmdArgs) {
        String[] args = cmdArgs.getArgs();
        List<String> completer = new ArrayList<>();


        if (args.length == 1) {
            completer.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
        } else if (args.length == 2) {
            completer.addAll(Arrays.asList("clear", "add", "remove"));
        } else if (args.length == 3) {
            if (args[2].equalsIgnoreCase("add") || args[2].equalsIgnoreCase("remove")) {
                completer.addAll(BukkitCommandFramework.INSTANCE.getCommands().stream().map(Command::getPermission)
                                                                .collect(Collectors.toList()));
            }
        }

        return completer.stream().filter(complete -> complete.toLowerCase().startsWith(args[args.length - 1]))
                        .collect(Collectors.toList());
    }
}
