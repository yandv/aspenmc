package br.com.aspenmc.bungee.command.register;

import br.com.aspenmc.CommonConst;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.clan.Clan;
import br.com.aspenmc.clan.ClanMember;
import br.com.aspenmc.clan.ClanRole;
import br.com.aspenmc.command.CommandArgs;
import br.com.aspenmc.command.CommandFramework;
import br.com.aspenmc.command.CommandHandler;
import br.com.aspenmc.entity.Member;
import br.com.aspenmc.entity.member.MemberVoid;
import br.com.aspenmc.manager.ClanManager;
import br.com.aspenmc.utils.string.MessageBuilder;
import br.com.aspenmc.utils.string.StringFormat;
import com.google.common.base.Joiner;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ClanCommand implements CommandHandler {

    @CommandFramework.Command(name = "claninfo", aliases = { "clan.info" }, console = false)
    public void clanInfoCommand(CommandArgs cmdArgs) {
        Member sender = cmdArgs.getSenderAsMember();
        Clan clan = sender.getClan().orElse(null);

        if (clan == null) {
            sender.sendMessage(sender.t("clan-system.dont-have-clan"));
            return;
        }

        sender.sendMessage("§aClan " + clan.getClanName() + " (" + clan.getClanAbbreviation() + ")");
        sender.sendMessage("  §fLíder: §7" + clan.getOwner().getLastName());
        sender.sendMessage("  §fMembros: §7" + clan.getMemberCount() + "/" + clan.getMaxPlayers());
        sender.sendMessage("  §fParticipantes: §7" + clan.getMemberMap().values().stream().map(clanMember -> {
            return (CommonPlugin.getInstance().getMemberManager().getMemberById(clanMember.getPlayerId()).isPresent() ?
                    "§a" : "§c") + clanMember.getLastName();
        }).collect(Collectors.joining(", ")));
        sender.sendMessage("");
        sender.sendMessage("  §fCriado em: §7" + CommonConst.DATE_FORMAT.format(clan.getCreatedAt()) + " §8(há " +
                StringFormat.formatTime((System.currentTimeMillis() - clan.getCreatedAt()) / 1000) + " atrás)");
    }


    @CommandFramework.Command(name = "clan", console = false)
    public void clanCommand(CommandArgs cmdArgs) {
        Member sender = cmdArgs.getSenderAsMember();
        String[] args = cmdArgs.getArgs();

        if (args.length == 0) {
            sender.sendMessage(sender.t("command.clan.usage"));
            return;
        }

        switch (args[0].toLowerCase()) {
        case "create":
        case "criar": {
            if (args.length < 3) {
                sender.sendMessage("§aUse §f/clan criar <tag> <nome> §apara criar um clan.");
                break;
            }

            if (sender.hasClan()) {
                sender.sendMessage("§cVocê já possui um clan.");
                break;
            }

            String clanAbbreviation = args[1];

            if (clanAbbreviation.length() <= 2 || clanAbbreviation.length() > 6) {
                sender.sendMessage("§cA tag do clan deve ter entre 2 e 6 caracteres.");
                break;
            }

            String clanName = Joiner.on(' ').join(Arrays.copyOfRange(args, 2, args.length));

            if (clanName.length() <= 2 || clanName.length() > 16) {
                sender.sendMessage("§cO nome do clan deve ter entre 2 e 16 caracteres.");
                break;
            }

            CompletableFuture<UUID> clanId = CommonPlugin.getInstance().getClanService().getClanId();
            CompletableFuture<Clan> clanByName = CommonPlugin.getInstance().getClanService()
                                                             .getClanByName(clanName, clanAbbreviation, Clan.class);

            CompletableFuture.allOf(clanId, clanByName).whenComplete((unused, throwable) -> {
                if (throwable != null) {
                    sender.sendMessage(sender.t("command.clan.clan-creation-failed"));
                    throwable.printStackTrace();
                    return;
                }

                UUID id = clanId.join();
                Clan otherClan = clanByName.join();

                if (otherClan != null) {
                    sender.sendMessage(
                            sender.t("command.clan.create-clan-already-exists", "%clanName%", otherClan.getClanName(),
                                    "%clanAbbreviation%", otherClan.getClanAbbreviation()));
                    return;
                }

                Clan clan = CommonPlugin.getInstance().getClanService()
                                        .createClan(new Clan(id, clanName, clanAbbreviation, sender));

                sender.setClan(clan);

                CommonPlugin.getInstance().getClanManager().loadClan(clan);
                clan.sendMessage("clan-system.clan-created-sucess", "%clanName%", clanName, "%clanAbbreviation%",
                        clanAbbreviation);
            });

            break;
        }
        case "leave":
        case "sair": {
            Clan clan = sender.getClan().orElse(null);

            if (clan == null) {
                sender.sendMessage(sender.t("clan-system.dont-have-clan"));
                break;
            }

            if (clan.getClanRole(sender.getUniqueId()) == ClanRole.OWNER) {
                sender.sendMessage(sender.t("command.clan.leave-clan-owner"));
                break;
            }

            clan.leave(sender);
            break;
        }
        case "promover":
        case "promote": {
            if (args.length == 1) {
                sender.sendMessage(sender.t("command.clan.promote-usage"));
                break;
            }

            Clan clan = sender.getClan().orElse(null);

            if (clan == null) {
                sender.sendMessage(sender.t("clan-system.dont-have-clan"));
                break;
            }

            ClanMember clanMember = clan.getMemberByName(args[1]);

            if (clanMember == null) {
                sender.sendMessage(sender.t("command.clan.member-not-found", "%player%", args[1]));
                break;
            }

            if (clanMember.getRole().ordinal() >= ClanRole.ADMIN.ordinal()) {
                sender.sendMessage(sender.t("command.clan.promote-already-admin", "%player%", args[1]));
                return;
            }

            clanMember.setRole(ClanRole.values()[clanMember.getRole().ordinal() + 1]);
            sender.sendMessage(sender.t("command.clan.promote-success", "%player%", args[1], "%role%",
                    sender.t("clan-system.role." + clanMember.getRole().name().toLowerCase())));
            break;
        }
        case "rebaixar":
        case "demote": {
            if (args.length == 1) {
                sender.sendMessage(sender.t("command.clan.demote-usage"));
                break;
            }

            Clan clan = sender.getClan().orElse(null);

            if (clan == null) {
                sender.sendMessage(sender.t("clan-system.dont-have-clan"));
                break;
            }

            ClanMember clanMember = clan.getMemberByName(args[1]);

            if (clanMember == null) {
                sender.sendMessage(sender.t("command.clan.member-not-found", "%player%", args[1]));
                break;
            }

            if (clanMember.getRole().ordinal() == ClanRole.MEMBER.ordinal()) {
                sender.sendMessage(sender.t("command.clan.demote-already-member", "%player%", args[1]));
                return;
            }

            clanMember.setRole(ClanRole.MEMBER);
            sender.sendMessage(sender.t("command.clan.demote-success", "%player%", args[1], "%role%",
                    sender.t("clan-system.role." + clanMember.getRole().name().toLowerCase())));
            break;
        }
        case "expulsar": {
            if (args.length == 1) {
                sender.sendMessage(sender.t("command.clan.kick-usage",
                        "§aUse §f/clan expulsar <jogador> §apara expulsar um jogador do seu clan."));
                break;
            }

            Clan clan = sender.getClan().orElse(null);

            if (clan == null) {
                sender.sendMessage(sender.t("clan-system.dont-have-clan"));
                break;
            }

            ClanMember clanMember = clan.getMemberByName(args[1]);

            if (clanMember == null) {
                sender.sendMessage(sender.t("command.clan.member-not-found", "%player%", args[1]));
                break;
            }

            if (clanMember.getRole().ordinal() >= ClanRole.ADMIN.ordinal()) {
                sender.sendMessage(sender.t("command.clan.no-permission"));
                return;
            }

            Member member = CommonPlugin.getInstance().getMemberManager()
                                        .getOrLoadById(clanMember.getPlayerId(), MemberVoid.class).orElse(null);

            if (member == null) {
                sender.sendMessage(sender.t("command.clan.member-not-found", "%player%", args[1]));
                break;
            }

            clan.leave(member);
            break;
        }
        case "disband":
        case "deletar": {
            Clan clan = sender.getClan().orElse(null);

            if (clan == null) {
                sender.sendMessage(sender.t("clan-system.dont-have-clan"));
                break;
            }

            if (clan.getClanRole(sender.getUniqueId()) != ClanRole.OWNER) {
                sender.sendMessage(sender.t("command.clan.disband-clan-not-owner"));
                break;
            }

            clan.disband();
            break;
        }
        case "aceitar": {
            if (args.length == 1) {
                sender.sendMessage(sender.t("command.clan.accept-usage"));
                break;
            }

            Member member = CommonPlugin.getInstance().getMemberManager().getMemberByName(args[1]).orElse(null);

            if (member == null) {
                sender.sendMessage(sender.t("command.clan.accept-member-not-found", "%player%", args[1]));
                break;
            }

            ClanManager.ClanInvite clanInvite = CommonPlugin.getInstance().getClanManager()
                                                            .getInvite(sender, member.getUniqueId());

            if (clanInvite == null) {
                sender.sendMessage(sender.t("command.clan.accept-not-invited", "%clan%", args[1]));
                break;
            }

            clanInvite.result(true);
            break;
        }
        default:
        case "invite":
        case "convidar": {
            boolean invite = args[0].equalsIgnoreCase("invite") || args[0].equalsIgnoreCase("convidar");
            int arg = invite ? 1 : 0;

            if (args.length == arg) {
                sender.sendMessage(sender.t("command.clan.invite-usage",
                        "§aUse §f/clan convidar <jogador> §apara convidar um jogador para o seu clan."));
                break;
            }

            Member member = CommonPlugin.getInstance().getMemberManager().getMemberByName(args[arg]).orElse(null);

            if (member == null) {
                sender.sendMessage(sender.t("player-not-found", "%player%", args[arg]));
                break;
            }

            if (!invite) {
                ClanManager.ClanInvite clanInvite = CommonPlugin.getInstance().getClanManager()
                                                                .getInvite(sender, member.getUniqueId());

                if (clanInvite != null) {
                    clanInvite.result(true);
                    break;
                }
            }

            if (!member.getPreferencesConfiguration().isClanInvitesEnabled()) {
                sender.sendMessage(sender.t("command.clan.invite-disabled", "%player%", args[arg]));
                break;
            }

            if (CommonPlugin.getInstance().getClanManager().hasInvite(member, sender.getUniqueId())) {
                sender.sendMessage(sender.t("command.clan.invite-already-invited", "%player%", args[arg]));
                break;
            }

            Clan clan = sender.getClan().orElse(null);

            if (clan == null) {
                sender.sendMessage(sender.t("clan-system.dont-have-clan"));
                break;
            }

            CommonPlugin.getInstance().getClanManager().invite(sender, member, accept -> {
                CommonPlugin.getInstance().getClanManager().removeInvite(member, sender.getUniqueId());

                if (!accept) {
                    sender.sendMessage(sender.t("command.clan.invite-denied"));
                    return;
                }

                Clan realClan = CommonPlugin.getInstance().getClanManager().getClanById(clan.getClanId()).orElse(null);

                if (realClan == null) {
                    sender.sendMessage(sender.t("command.clan.invite-expired"));
                    return;
                }

                if (realClan.getMemberCount() >= realClan.getMaxPlayers()) {
                    sender.sendMessage(sender.t("command.clan.invite-full"));
                    return;
                }

                if (realClan.isClanMember(member.getUniqueId())) {
                    sender.sendMessage(sender.t("command.clan.invite-already-member", "%player%", member.getName()));
                    return;
                }

                realClan.join(member);
                member.setClan(realClan);
            });

            sender.sendMessage(sender.t("command.clan.invite-success", "%player%", args[arg]));
            member.sendMessage(
                    new MessageBuilder(member.t("command.clan.invite-received", "%clanName%", clan.getClanName()))
                            .setHoverEvent(
                                    "§aClique aqui para aceitar o pedido\n§apara entrar no clan " + clan.getClanName() +
                                            ".\n§a\n§aConvite recebido do " + sender.getName())
                            .setClickEvent("/clan aceitar " + sender.getName()).create());
        }
        }
    }
}
