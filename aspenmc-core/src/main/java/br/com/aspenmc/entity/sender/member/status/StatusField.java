package br.com.aspenmc.entity.sender.member.status;

public enum StatusField {

    KILLS("statusMap.kills"), DEATHS("statusMap.deaths"), KILLSTREAK("statusMap.killstreak"),
    MAX_KILLSTREAK("statusMap.max-killstreak"), WINS("statusMap.wins"), LOSSES("statusMap.losses"),
    WINSTREAK("statusMap.winstreak"), XP("xp"), LEAGUE("league");

    private final String name;
    private final String field;

    StatusField(String field) {
        this.name = field.contains(".") ? field.split("\\.")[1] : field;
        this.field = field;
    }

    public String getName() {
        return name;
    }

    public String toField() {
        return this.field;
    }
}