package br.com.aspenmc.entity.member.status;

import br.com.aspenmc.CommonPlugin;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

@Getter
public class Status {

    private final UUID uniqueId;
    private final StatusType statusType;

    private League league;
    private int xp;

    private final Map<String, Long> statusMap;

    public Status(UUID uniqueId, StatusType statusType) {
        this.uniqueId = uniqueId;
        this.statusType = statusType;

        this.league = League.values()[0];
        this.xp = 0;

        this.statusMap = new HashMap<>();
    }

    public long get(StatusField id) {
        return this.statusMap.getOrDefault(id.getName(), 0L);
    }

    public void set(StatusField id, long value) {
        this.statusMap.put(id.getName(), value);
    }

    public void add(StatusField id, long value) {
        set(id, get(id) + value);
    }

    public void remove(StatusField id, long value) {
        set(id, get(id) - value);
    }

    public long get(String id) {
        return this.statusMap.getOrDefault(id, 0L);
    }

    public void set(String id, long value) {
        this.statusMap.put(id, value);
    }

    public void add(String id, long value) {
        set(id, get(id) + value);
    }

    public void remove(String id, long value) {
        set(id, get(id) - value);
    }

    public void setLeague(League league) {
        if (this.league == league) return;

        CommonPlugin.getInstance().getStatusManager().onLeagueChange(this, this.league, league);
        this.league = league;
        saveStatus(StatusField.LEAGUE, StatusField.XP);
    }

    public void setXp(int xp) {
        this.xp = xp;

        if (league.getTotalXp(xp) >= league.getMaxXp()) {
            setLeague(league.getNextLeague());
        } else if (league != League.values()[0]) {
            if (league.getTotalXp(xp) < 0) {
                setLeague(league.getPreviousLeague());
            }
        }
    }

    public int addXp(int xp) {
        if (xp < 0) xp = 0;

        setXp(getXp() + xp);
        return xp;
    }

    public int removeXp(int xp) {
        if (xp < 0) xp = 0;

        setXp(Math.max(getXp() - xp, 0));
        return xp;
    }

    public void saveStatus(StatusField... fields) {
        CommonPlugin.getInstance().getStatusService()
                    .saveStatus(this, Stream.of(fields).map(StatusField::toField).toArray(String[]::new));
    }

    private void save(String id) {
        CommonPlugin.getInstance().getStatusService().saveStatus(this, id);
    }
}
