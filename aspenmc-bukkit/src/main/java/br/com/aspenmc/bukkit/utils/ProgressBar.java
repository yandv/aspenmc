package br.com.aspenmc.bukkit.utils;

import org.bukkit.ChatColor;

public class ProgressBar {

    public static String getProgressBar(double current, double max, int totalBars, char symbol,
            ChatColor completedColor, ChatColor notCompletedColor) {
        double percent = current / max;
        int progressBars = (int) (totalBars * percent);

        StringBuilder stringBuilder = new StringBuilder();

        if (progressBars > 0) {
            stringBuilder.append(completedColor);

            for (int i = 0; i < progressBars; i++) {
                stringBuilder.append(symbol);
            }
        }

        if (totalBars - progressBars > 0) {
            stringBuilder.append(notCompletedColor);
            for (int i = 0; i < Math.min(totalBars - progressBars, totalBars); i++) {
                stringBuilder.append(symbol);
            }
        }

        return stringBuilder.toString();
    }
}
