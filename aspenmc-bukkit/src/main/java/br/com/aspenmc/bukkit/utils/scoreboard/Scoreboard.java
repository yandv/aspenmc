package br.com.aspenmc.bukkit.utils.scoreboard;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Team;

public class Scoreboard extends Objective {

    @Getter
    private Player player;
    private int index = 15;

    public Scoreboard(Player player, String title) {
        super(player.getScoreboard(), DisplaySlot.SIDEBAR);
        this.player = player;
        setDisplayName(title);
    }

    public void add(String text) {
        add(index--, text);
    }

    public static void main(String[] args) {
        String text = " Registre sua conta:";
        String prefix = "", suffix = "";

        if (text.length() <= 16) {
            prefix = text;
            for (int i = prefix.length(); i > 0; i--) {
				if (prefix.substring(0, i).endsWith("§")) {
					prefix = prefix.substring(0, (i - 1));
				} else {
					break;
				}
            }
        } else {
            prefix = text.substring(0, 16);

            for (int i = prefix.length(); i > 0; i--) {
				if (prefix.substring(0, i).endsWith("§")) {
					prefix = prefix.substring(0, (i - 1));
				} else {
					break;
				}
            }

            String color = ChatColor.getLastColors(prefix);

			if (color.startsWith("§f")) {
				color = color.substring(2);
			}

            suffix = color + text.substring(16);

            if (!suffix.startsWith("§")) {
                ChatColor byChar = ChatColor.getByChar(suffix.charAt(0));

                if (byChar != null) {
                    suffix = byChar + suffix.substring(1);
                }
            }

			if (suffix.length() > 16) {
				suffix = suffix.substring(0, 16);
			}
        }
    }

    public void add(int index, String text) {
        Team team = getScoreboard().getTeam("score-" + index);
        String prefix = "", suffix = "";

        if (team == null) {
            team = getScoreboard().registerNewTeam("score-" + index);
            String score = ChatColor.values()[index - 1].toString();
            getObjective().getScore(score).setScore(index);

            if (!team.hasEntry(score)) {
                team.addEntry(score);
            }
        }

        if (text.length() <= 16) {
            prefix = text;
            for (int i = prefix.length(); i > 0; i--) {
				if (prefix.substring(0, i).endsWith("§")) {
					prefix = prefix.substring(0, (i - 1));
				} else {
					break;
				}
            }
        } else {
            prefix = text.substring(0, 16);

            for (int i = prefix.length(); i > 0; i--) {
				if (prefix.substring(0, i).endsWith("§")) {
					prefix = prefix.substring(0, (i - 1));
				} else {
					break;
				}
            }

            String color = ChatColor.getLastColors(prefix);

			if (color.startsWith("§f")) {
				color = color.substring(2);
			}

            suffix = color + text.substring(16);

            if (!suffix.startsWith("§")) {
                ChatColor byChar = ChatColor.getByChar(suffix.charAt(0));

                if (byChar != null) {
                    suffix = byChar + suffix.substring(1);
                }
            }

			if (suffix.length() > 16) {
				suffix = suffix.substring(0, 16);
			}
        }

        team.setPrefix(prefix);
        team.setSuffix(suffix);
    }

    public void remove(int index) {
        Team team = getScoreboard().getTeam("score-" + index);

        if (team != null) {
            String score = ChatColor.values()[index - 1].toString();

			if (!team.hasEntry(score)) {
				team.addEntry(score);
			}

            team.unregister();
            getScoreboard().resetScores(score);
        }
    }
}
