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
import br.com.aspenmc.utils.string.StringFormat;
import com.google.common.base.Joiner;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ClanCommand implements CommandHandler {

    @CommandFramework.Command(name = "clan.info", aliases = { "claninfo" }, console = false)
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
            sender.sendMessage(sender.t("command.clan.usage",
                    " §a» §fUse §a/clan criar <tag> <nome> §apara criar um clan." + "\n" +
                            " §a» §fUse §a/clan info §fpara ver informações sobre o seu clan." + "\n" +
                            " §a» §fUse §a/clan sair §fpara sair do seu clan." + "\n" +
                            " §a» §fUse §a/clan convidar <jogador> §fpara convidar um jogador para o seu clan." + "\n" +
                            " §a» §fUse §a/clan aceitar <jogador> §fpara aceitar um convite para um clan." + "\n" +
                            " §a» §fUse §a/clan recusar <jogador> §fpara recusar um convite para um clan." + "\n" +
                            " §a» §fUse §a/clan expulsar <jogador> §fpara expulsar um jogador do seu clan." + "\n" +
                            " §a» §fUse §a/clan promover <jogador> §fpara promover um jogador do seu clan." + "\n" +
                            " §a» §fUse §a/clan rebaixar <jogador> §fpara rebaixar um jogador do seu clan."));
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
                    sender.sendMessage(sender.t("command.clan.clan-creation-failed",
                            "§cO sistema não conseguiu criar o seu clan no momento, tente novamente mais tarde..."));
                    throwable.printStackTrace();
                    return;
                }

                UUID id = clanId.join();
                Clan otherClan = clanByName.join();

                if (otherClan != null) {
                    sender.sendMessage(sender.t("command.clan.create-clan-already-exists",
                            "§cO nome %clanName% ou a tag %clanAbbreviation% não estão disponíveis.", "%clanName%",
                            otherClan.getClanName(), "%clanAbbreviation%", otherClan.getClanAbbreviation()));
                    return;
                }

                Clan clan = CommonPlugin.getInstance().getClanService()
                                        .createClan(new Clan(id, clanName, clanAbbreviation, sender));

                sender.setClan(clan);

                CommonPlugin.getInstance().getClanManager().loadClan(clan);
                clan.sendMessage("clan-system.clan-created-sucess",
                        "§aO clan %clanName% (%clanAbbreviation%) foi criado.", "%clanName%", clanName,
                        "%clanAbbreviation%", clanAbbreviation);
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
                sender.sendMessage(sender.t("command.clan.leave-clan-owner",
                        "§cCaso você deseja sair do clan, use /clan disband."));
                break;
            }

            clan.leave(sender);
            break;
        }
        case "promover":
        case "promote": {
            if (args.length == 1) {
                sender.sendMessage(sender.t("command.clan.promote-usage",
                        "§aUse §f/clan promote <jogador> §apara promover um jogador do seu clan."));
                break;
            }

            Clan clan = sender.getClan().orElse(null);

            if (clan == null) {
                sender.sendMessage(sender.t("clan-system.dont-have-clan"));
                break;
            }

            ClanMember clanMember = clan.getMemberByName(args[1]);

            if (clanMember == null) {
                sender.sendMessage(sender.t("command.clan.member-not-found",
                        "§cO jogador %player% não foi encontrado no seu clan.", "%player%", args[1]));
                break;
            }

            if (clanMember.getRole().ordinal() >= ClanRole.ADMIN.ordinal()) {
                sender.sendMessage(sender.t("command.clan.promote-already-admin",
                        "§cO jogador %player% já é um administrador do clan.", "%player%", args[1]));
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
                sender.sendMessage(sender.t("command.clan.demote-usage",
                        "§aUse §f/clan demote <jogador> §apara rebaixar um jogador do seu clan."));
                break;
            }

            Clan clan = sender.getClan().orElse(null);

            if (clan == null) {
                sender.sendMessage(sender.t("clan-system.dont-have-clan"));
                break;
            }

            ClanMember clanMember = clan.getMemberByName(args[1]);

            if (clanMember == null) {
                sender.sendMessage(sender.t("command.clan.member-not-found",
                        "§cO jogador %player% não foi encontrado no seu clan.", "%player%", args[1]));
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
                sender.sendMessage(sender.t("clan-system.dont-have-clan", "§cVocê não está em um clan."));
                break;
            }

            ClanMember clanMember = clan.getMemberByName(args[1]);

            if (clanMember == null) {
                sender.sendMessage(sender.t("command.clan.member-not-found",
                        "§cO jogador %player% não foi encontrado no seu clan.", "%player%", args[1]));
                break;
            }

            if (clanMember.getRole().ordinal() >= ClanRole.ADMIN.ordinal()) {
                sender.sendMessage(
                        sender.t("command.clan.no-permission", "§cVocê não tem permissão para fazer isso no clan."));
                return;
            }

            Member member = CommonPlugin.getInstance().getMemberManager()
                                        .getOrLoadById(clanMember.getPlayerId(), MemberVoid.class).orElse(null);

            if (member == null) {
                sender.sendMessage(sender.t("command.clan.member-not-found",
                        "§cO jogador %player% não foi encontrado no seu clan.", "%player%", args[1]));
                break;
            }

            clan.leave(member);
            break;
        }
        case "disband":
        case "deletar": {
            Clan clan = sender.getClan().orElse(null);

            if (clan == null) {
                sender.sendMessage(sender.t("clan-system.dont-have-clan", "§cVocê não está em um clan."));
                break;
            }

            if (clan.getClanRole(sender.getUniqueId()) != ClanRole.OWNER) {
                sender.sendMessage(sender.t("command.clan.disband-clan-not-owner",
                        "§cVocê não é o dono do clan, portanto não pode deletá-lo."));
                break;
            }

            clan.disband();
            break;
        }
        case "aceitar": {
            if (args.length == 1) {
                sender.sendMessage(sender.t("command.clan.accept-usage",
                        "§aUse §f/clan aceitar <clan> §apara aceitar um convite de um clan."));
                break;
            }

            Member member = CommonPlugin.getInstance().getMemberManager().getMemberByName(args[1]).orElse(null);

            if (member == null) {
                sender.sendMessage(
                        sender.t("command.clan.accept-member-not-found", "§cO jogador %player% não foi encontrado.",
                                "%player%", args[1]));
                break;
            }

            ClanManager.ClanInvite clanInvite = CommonPlugin.getInstance().getClanManager()
                                                            .getInvite(sender, member.getUniqueId());

            if (clanInvite == null) {
                sender.sendMessage(
                        sender.t("command.clan.accept-not-invited", "§cVocê não foi convidado para o clan %clan%.",
                                "%clan%", args[1]));
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
                sender.sendMessage(sender.t("command.clan.invite-disabled",
                        "§cO jogador %player% não está recebendo convites para clan no momento.", "%player%",
                        args[arg]));
                break;
            }

            if (CommonPlugin.getInstance().getClanManager().hasInvite(member, sender.getUniqueId())) {
                sender.sendMessage(sender.t("command.clan.invite-already-invited",
                        "§cO jogador %player% já foi convidado para o seu clan.", "%player%", args[arg]));
                break;
            }

            Clan clan = sender.getClan().orElse(null);

            if (clan == null) {
                sender.sendMessage(sender.t("clan-system.dont-have-clan", "§cVocê não está em um clan."));
                break;
            }

            CommonPlugin.getInstance().getClanManager().invite(sender, member, a -> {
                CommonPlugin.getInstance().getClanManager().removeInvite(member, sender.getUniqueId());

                if (!a) {
                    sender.sendMessage(sender.t("command.clan.invite-denied",
                            "§cO jogador " + member.getName() + " recusou seu pedido para clan."));
                    return;
                }

                Clan realClan = CommonPlugin.getInstance().getClanManager().getClanById(clan.getClanId()).orElse(null);

                if (realClan == null) {
                    sender.sendMessage(
                            sender.t("command.clan.invite-expired", "§cO pedido para entrar no clan expirou."));
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
            member.sendMessage(member.t("command.clan.invite-received", "%clanName%", clan.getClanName()));
        }
        }
    }
}
