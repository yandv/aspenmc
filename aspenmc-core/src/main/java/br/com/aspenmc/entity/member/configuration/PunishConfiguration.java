package br.com.aspenmc.entity.member.configuration;

import lombok.Getter;
import br.com.aspenmc.entity.Member;
import br.com.aspenmc.punish.Punish;
import br.com.aspenmc.punish.PunishType;

import java.util.*;

public class PunishConfiguration {

    @Getter
    private Map<PunishType, List<Punish>> punishMap;

    private transient Member member;

    public PunishConfiguration() {
        this.punishMap = new HashMap<>();
    }

    public Collection<Punish> getPunishs(PunishType punishType) {
        return punishMap.getOrDefault(punishType, new ArrayList<>());
    }

    public Punish getCurrentPunish(PunishType punishType) {
        return punishMap.computeIfAbsent(punishType, k -> new ArrayList<>()).stream()
                        .filter(punish -> !punish.isRevoked() && !punish.hasExpired()).findFirst().orElse(null);
    }

    public void punish(Punish punish) {
        punishMap.computeIfAbsent(punish.getPunishType(), k -> new ArrayList<>()).add(punish);
        save();
    }

    public void revoke(PunishType punishType, UUID uniqueId, String punisherName, String reason) {
        Punish currentPunish = getCurrentPunish(punishType);

        if (currentPunish != null) {
            currentPunish.revoke(uniqueId, punisherName, reason);
            save();
        }
    }

    public int getCount(PunishType punishType) {
        if (punishMap.containsKey(punishType)) {
            return punishMap.get(punishType).size();
        }

        return 0;
    }

    public void loadConfiguration(Member member) {
        this.member = member;
    }

    private void save() {
        if (member != null) {
            member.save("punishConfiguration");
        }
    }
}
