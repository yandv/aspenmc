package br.com.aspenmc.bukkit.utils.scoreboard;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreHelper {

    private static final ScoreHelper SCORE_HELPER = new ScoreHelper();

    @Getter
    private final Map<UUID, Scoreboard> scoreMap = new HashMap<>();

    public void setScoreboard(Player player, Scoreboard scoreboard) {
        player.setScoreboard(scoreboard.getScoreboard());
        scoreMap.put(player.getUniqueId(), scoreboard);
    }

    public void setScoreboardName(Player player, String name) {
        if (scoreMap.containsKey(player.getUniqueId())) scoreMap.get(player.getUniqueId()).setDisplayName(name);
    }

    public void removeScoreboard(int index) {
        for (Player player : Bukkit.getOnlinePlayers())
            if (scoreMap.containsKey(player.getUniqueId())) scoreMap.get(player.getUniqueId()).remove(index);
    }

    public void removeScoreboard(Player player) {
        scoreMap.remove(player.getUniqueId());
    }

    public void removeScoreboard(Player player, int index) {
        if (scoreMap.containsKey(player.getUniqueId())) scoreMap.get(player.getUniqueId()).remove(index);
    }

    public void addScoreboard(Player player, String value) {
        if (scoreMap.containsKey(player.getUniqueId())) scoreMap.get(player.getUniqueId()).add(value);
    }

    public void addScoreboard(Player player, int index, String value) {
        if (scoreMap.containsKey(player.getUniqueId())) scoreMap.get(player.getUniqueId()).add(index, value);
    }

    public void updateScoreboard(int index, String value) {
        for (Player player : Bukkit.getOnlinePlayers())
            addScoreboard(player, index, value);
    }

    public void updateScoreboard(Player player, int index, String value) {
        addScoreboard(player, index, value);
    }

    public Scoreboard getScoreboard(Player player) {
        return scoreMap.get(player.getUniqueId());
    }

    public static ScoreHelper getInstance() {
        return SCORE_HELPER;
    }

}
