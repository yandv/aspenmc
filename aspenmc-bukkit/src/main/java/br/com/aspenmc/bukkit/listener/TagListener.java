package br.com.aspenmc.bukkit.listener;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bukkit.BukkitCommon;
import br.com.aspenmc.bukkit.entity.BukkitMember;
import br.com.aspenmc.bukkit.event.player.tag.PlayerChangedTagEvent;
import br.com.aspenmc.bukkit.utils.scoreboard.ScoreboardAPI;
import br.com.aspenmc.entity.Member;
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
    public void onPlayerChangeTag(PlayerChangedTagEvent event) {
        if (!BukkitCommon.getInstance().isTagControl()) return;

        Player p = event.getPlayer();
        Member player = event.getMember();

        if (player == null) return;

        String id = getTagId(event.getNewTag());
        String oldId = getTagId(event.getOldTag());

        String prefix = event.getNewTag().getRealPrefix();

        for (Player o : Bukkit.getOnlinePlayers()) {
            ScoreboardAPI.leaveTeamToPlayer(o, oldId, p);
            ScoreboardAPI.joinTeam(ScoreboardAPI.createTeamIfNotExistsToPlayer(o, id, prefix, ""), p);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (!BukkitCommon.getInstance().isTagControl()) return;

        Player p = e.getPlayer();

        Member player = CommonPlugin.getInstance().getMemberManager().getMemberById(e.getPlayer().getUniqueId())
                                    .orElse(null);

        if (player == null) return;

        Tag tag = player.getTag().orElse(null);
        String prefix = tag == null ? "§7" : tag.getRealPrefix();

        String id = getTagId(tag);

        for (Player o : Bukkit.getOnlinePlayers()) {
            ScoreboardAPI.joinTeam(ScoreboardAPI.createTeamIfNotExistsToPlayer(o, id, prefix, ""), p);
        }

        for (Player o : Bukkit.getOnlinePlayers()) {
            if (!o.getUniqueId().equals(p.getUniqueId())) {
                BukkitMember bp = CommonPlugin.getInstance().getMemberManager()
                                              .getMemberById(o.getUniqueId(), BukkitMember.class).orElse(null);

                if (bp == null) {
                    o.kickPlayer("§cSua conta não foi carregada.");
                    continue;
                }

                tag = bp.getTag().orElse(null);
                prefix = tag == null ? "§7" : tag.getRealPrefix();

                ScoreboardAPI.joinTeam(ScoreboardAPI.createTeamIfNotExistsToPlayer(p, getTagId(tag), prefix, ""), o);
            }
        }
    }

    public static String getTagId(Tag tag) {
        return tag == null ? "aaa" : getId(tag.getId());
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
}
