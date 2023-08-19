package br.com.aspenmc.bukkit.listener;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bukkit.BukkitCommon;
import br.com.aspenmc.bukkit.entity.BukkitMember;
import br.com.aspenmc.bukkit.event.player.PlayerClanTagUpdateEvent;
import br.com.aspenmc.bukkit.event.player.tag.PlayerChangedTagEvent;
import br.com.aspenmc.bukkit.utils.scoreboard.ScoreboardAPI;
import br.com.aspenmc.clan.Clan;
import br.com.aspenmc.clan.ClanTag;
import br.com.aspenmc.entity.Member;
import br.com.aspenmc.manager.PermissionManager;
import br.com.aspenmc.permission.Tag;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;

public class TagListener implements Listener {

    private static final char[] CHAR_ARRAY = "abcdefghijklmnopqrstuvwxyz".toCharArray();

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            ScoreboardAPI.leaveCurrentTeamForOnlinePlayers(p);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        ScoreboardAPI.leaveCurrentTeamForOnlinePlayers(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerClanTagUpdate(PlayerClanTagUpdateEvent event) {
        if (!BukkitCommon.getInstance().isTagControl()) return;

        Player p = event.getPlayer();
        Member player = event.getMember();

        if (player == null) return;

        Tag tag = player.getTag().orElse(PermissionManager.NULL_TAG);

        String id = getTagId(tag, getClanId(player, event.isNewClanDisplayTagEnabled()), event.getNewClanTag());
        String oldId = getTagId(tag, getClanId(player, event.isOldClanDisplayTagEnabled()), event.getOldClanTag());

        String prefix = tag.getRealPrefix();

        for (Player o : Bukkit.getOnlinePlayers()) {
            ScoreboardAPI.leaveTeamToPlayer(o, oldId, p);
            ScoreboardAPI.joinTeam(ScoreboardAPI.createTeamIfNotExistsToPlayer(o, id, prefix, getSuffix(player)), p);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChangeTag(PlayerChangedTagEvent event) {
        if (!BukkitCommon.getInstance().isTagControl()) return;

        Player p = event.getPlayer();
        Member player = event.getMember();

        if (player == null) return;

        String id = getTagId(event.getNewTag(), getClanId(player), player.getPreferencesConfiguration().getClanTag());
        String oldId = getTagId(event.getOldTag(), getClanId(player),
                player.getPreferencesConfiguration().getClanTag());

        String prefix = event.getNewTag().getRealPrefix();
        String suffix = getSuffix(player);

        for (Player o : Bukkit.getOnlinePlayers()) {
            ScoreboardAPI.leaveTeamToPlayer(o, oldId, p);
            ScoreboardAPI.joinTeam(ScoreboardAPI.createTeamIfNotExistsToPlayer(o, id, prefix, suffix), p);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (!BukkitCommon.getInstance().isTagControl()) return;

        Player p = e.getPlayer();

        Member player = CommonPlugin.getInstance().getMemberManager().getMemberById(e.getPlayer().getUniqueId())
                                    .orElse(null);

        if (player == null) return;

        Tag tag = player.getTag().orElse(PermissionManager.NULL_TAG);

        String id = getTagId(tag, getClanId(player), player.getPreferencesConfiguration().getClanTag());

        String playerPrefix = tag.getRealPrefix();
        String playerSuffix = getSuffix(player);

        for (Player o : Bukkit.getOnlinePlayers()) {
            if (!o.getUniqueId().equals(p.getUniqueId())) {
                CommonPlugin.getInstance().getMemberManager().getMemberById(o.getUniqueId(), BukkitMember.class)
                            .ifPresent(m -> {
                                Tag t = m.getTag().orElse(PermissionManager.NULL_TAG);

                                ScoreboardAPI.joinTeam(ScoreboardAPI.createTeamIfNotExistsToPlayer(p,
                                        getTagId(t, getClanId(m), m.getPreferencesConfiguration().getClanTag()),
                                        t.getRealPrefix(), getSuffix(m)), o);
                            });
            }
            ScoreboardAPI.joinTeam(ScoreboardAPI.createTeamIfNotExistsToPlayer(o, id, playerPrefix, playerSuffix), p);
        }
    }

    private String getSuffix(Member member) {
        return member.getPreferencesConfiguration().isClanDisplayTagEnabled() && member.hasClan() ?
                member.getPreferencesConfiguration().getClanTag().getColor() + " [" +
                        member.getClan().map(Clan::getClanAbbreviation).orElse("-/-") + "]" : "";
    }

    public static String getTagId(Tag tag, int clanId, ClanTag clanTag) {
        StringBuilder stringBuilder = new StringBuilder();

        if (tag == null) {
            stringBuilder.append("aaa");
        } else {
            stringBuilder.append(getId(tag.getId()));
        }

        if (clanId != -1) {
            stringBuilder.append(getId(clanId));
            stringBuilder.append(getId(clanTag.ordinal()));
        }

        return stringBuilder.toString();
    }

    public static String getId(int id) {
        StringBuilder stringBuilder = new StringBuilder();
        int firstId = 0, secondId = id;

        if (secondId >= CHAR_ARRAY.length) {
            firstId = secondId / CHAR_ARRAY.length;
            secondId = secondId % CHAR_ARRAY.length;
        }

        return stringBuilder.append(CHAR_ARRAY[firstId]).append(CHAR_ARRAY[secondId]).toString();
    }

    public static int getClanId(boolean clanDisplayTagEnabled, int indexOf) {
        return clanDisplayTagEnabled ? indexOf : -1;
    }

    public static int getClanId(Member member) {
        return getClanId(member.getPreferencesConfiguration().isClanDisplayTagEnabled(),
                member.getClan().map(clan -> CommonPlugin.getInstance().getClanManager().indexOf(clan)).orElse(-1));
    }

    public static int getClanId(Member member, boolean clanDisplayTagEnabled) {
        return getClanId(clanDisplayTagEnabled,
                member.getClan().map(clan -> CommonPlugin.getInstance().getClanManager().indexOf(clan)).orElse(-1));
    }
}
