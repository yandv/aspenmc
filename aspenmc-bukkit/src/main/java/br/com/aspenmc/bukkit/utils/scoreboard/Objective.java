package br.com.aspenmc.bukkit.utils.scoreboard;

import com.google.common.base.Preconditions;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public abstract class Objective {

	private org.bukkit.scoreboard.Objective objective;
	private final Scoreboard scoreboard;

	public Objective(Scoreboard scoreboard, DisplaySlot slot) {
		this.scoreboard = scoreboard;

		this.objective = scoreboard.getObjective(slot.name().toLowerCase());

		if (this.objective == null)
			this.objective = scoreboard.registerNewObjective(slot.name().toLowerCase(), "dummy");
		else {
			scoreboard.clearSlot(slot);

			for (int index = 15; index > 0; index--) {
				Team team = getScoreboard().getTeam("score-" + index);

				if (team != null) {
					String score = ChatColor.values()[index - 1].toString();

					if (!team.hasEntry(score))
						team.addEntry(score);

					team.unregister();
					getScoreboard().resetScores(score);
				}
			}
		}

		this.objective.setDisplaySlot(slot);
	}

	public Scoreboard getScoreboard() {
		return scoreboard;
	}

	public org.bukkit.scoreboard.Objective getObjective() {
		return objective;
	}

	public void setDisplayName(String name) {
		Preconditions.checkArgument(name != null, "Parameter 'name' cannot be null");
		objective.setDisplayName(name.length() >= 32 ? name.substring(0, 32) : name);
	}

	public final void destroy() {
		objective.unregister();
		objective = null;
	}
}
