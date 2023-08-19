package br.com.aspenmc.entity.member.status;

import com.google.common.base.Strings;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

@Getter
public enum League {

    NOVATO("§8", "-", "Novato", 500), INTERMEDIARIO("§8", "☰", "Intermediário", 1000),

    SILVER("§7", "✶I", "Prata", 2000), SILVER_II("§7", "✶II", "Prata", 4000), SILVER_III("§7", "✶III", "Prata", 5000),
    SILVER_IV("§7", "✶IV", "Prata", 6000), SILVER_V("§7", "✶V", "Prata", 8000),

    GOLD("§6", "✳I", "Ouro", 10000), GOLD_II("§6", "✳II", "Ouro", 15000), GOLD_III("§6", "✳III", "Ouro", 17000),
    GOLD_IV("§6", "✳IV", "Ouro", 19000), GOLD_V("§6", "✳V", "Ouro", 23000),

    DIAMOND("§b", "✦I", "Diamante", 28000), DIAMOND_II("§b", "✦II", "Diamante", 31000),
    DIAMOND_III("§b", "✦III", "Diamante", 35000),

    EMERALD("§2", "✥I", "Esmeralda", 41000), EMERALD_II("§2", "✥II", "Esmeralda", 48000),
    EMERALD_III("§3", "✥III", "Esmeralda", 55000),

    MASTER("§d", "✫I", "Mestre", 63000), MASTER_II("§d", "✫II", "Mestre", 69000),

    LEGEND("§5", "♅I", "Lenda", 73000), LEGEND_II("§5", "♅II", "Lenda", 79000),

    SUPREME("§c", "♆I", "Supremo", 85000), SUPREME_II("§c", "♆II", "Supremo", 95000),

    MASTERPIECE("§4", "☬", "Masterpiece", 150000);

    private String color;
    private String symbol;
    private String name;
    private int maxXp;

    private League(String color, String symbol, String name, int maxXp) {
        this.symbol = symbol;
        this.color = color;
        this.name = name;
        this.maxXp = maxXp;
    }

    public String getColoredName() {
        return color + name;
    }

    public String getColoredSymbol() {
        return color + symbol;
    }

    public String getColoredConstraint() {
        return color + name + " " + symbol;
    }

    public League getNextLeague() {
        return ordinal() + 1 <= values()[values().length - 1].ordinal() ? League.values()[ordinal() + 1] :
                values()[values().length - 1];
    }

    public League getPreviousLeague() {
        return ordinal() - 1 >= 0 ? values()[ordinal() - 1] : values()[0];
    }

    public int getTotalXp(int currentXp) {
        int totalLeagueXp = currentXp;

        for (League previous : League.values()) { //  porra tava achando q era infitio
            if (previous.ordinal() < ordinal()) totalLeagueXp -= previous.getMaxXp();
        }

        return totalLeagueXp;
    }
}
