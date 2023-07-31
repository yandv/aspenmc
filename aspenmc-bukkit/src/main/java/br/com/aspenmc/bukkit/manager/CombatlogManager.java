package br.com.aspenmc.bukkit.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatlogManager {

	private Map<UUID, CombatLog> combatMap;

	public CombatlogManager() {
		this.combatMap = new HashMap<>();
	}

	public CombatLog addCombatLog(UUID uuid, CombatLog combatLog) {
		if (hasCombatLog(uuid, combatLog.getTime()))
			return combatMap.get(uuid);

		this.combatMap.put(uuid, combatLog);
		return combatLog;
	}

	public CombatLog getCombatLog(UUID uuid) {
		return combatMap.containsKey(uuid) ? combatMap.get(uuid) : null;
	}

	public void removeCombatLog(UUID uuid) {
		CombatLog combatLog = combatMap.get(uuid);

		if (combatLog == null)
			return;

		UUID otherPlayer = combatLog.getCombatLogged();
		CombatLog otherPlayerCombatLog = combatMap.get(otherPlayer);

		if (otherPlayerCombatLog != null) {
			if (otherPlayerCombatLog.getCombatLogged() == uuid)
				combatMap.remove(otherPlayer);
		}

		combatMap.remove(uuid);
	}

	public boolean hasCombatLog(UUID uniqueId) {
		return combatMap.containsKey(uniqueId) && combatMap.get(uniqueId).getTime() > System.currentTimeMillis();
	}

	public boolean hasCombatLog(UUID uniqueId, long newTime) {
		return combatMap.containsKey(uniqueId) && combatMap.get(uniqueId).getTime() - System.currentTimeMillis() > newTime - System.currentTimeMillis();
	}

	public Map<UUID, CombatLog> getCombatMap() {
		return combatMap;
	}

	public static class CombatLog {

		private UUID combatLogged;
		private long time;

		public CombatLog(UUID combatLogged) {
			this.combatLogged = combatLogged;
			this.time = System.currentTimeMillis() + 10000;
		}

		public CombatLog(UUID combatLogged, long time) {
			this.combatLogged = combatLogged;
			this.time = System.currentTimeMillis() + time;
		}

		public UUID getCombatLogged() {
			return combatLogged;
		}

		public long getTime() {
			return time;
		}

		public void hitted(UUID uuid) {
			this.combatLogged = uuid;
			this.time = System.currentTimeMillis() + 10000;
		}
	}

}
