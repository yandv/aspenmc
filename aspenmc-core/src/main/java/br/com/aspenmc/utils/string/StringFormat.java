package br.com.aspenmc.utils.string;

import br.com.aspenmc.CommonConst;
import com.google.common.base.Preconditions;
import net.md_5.bungee.api.ChatColor;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringFormat {

    public static List<String> formatForLore(String text) {
        return getLore(30, text);
    }

    public static List<String> getLore(int max, String text) {
        List<String> lore = new ArrayList<>();
        text = ChatColor.translateAlternateColorCodes('&', text);
        String[] split = text.split(" ");
        String color = "";
        text = "";

        for (int i = 0; i < split.length; i++) {
            if (ChatColor.stripColor(text).length() >= max || ChatColor.stripColor(text).endsWith(".") ||
                    ChatColor.stripColor(text).endsWith("!")) {
                lore.add(text);

                if (text.endsWith(".") || text.endsWith("!")) {
                    lore.add("");
                }
                text = color;
            }

            String toAdd = split[i];

            if (toAdd.contains("§")) {
                color = getLastColors(toAdd.toLowerCase());
            }

            if (toAdd.contains("\n")) {
                toAdd = toAdd.substring(0, toAdd.indexOf("\n"));
                split[i] = split[i].substring(toAdd.length() + 1);
                lore.add(text + (text.length() == 0 ? "" : " ") + toAdd);
                text = color;
                i--;
            } else {
                text += (ChatColor.stripColor(text).length() == 0 ? "" : " ") + toAdd.trim();
            }
        }
        lore.add(text);
        return lore;
    }

    public static String maskIp(String ip) {
        return "***.***.***" + ip.substring(ip.lastIndexOf("."));
    }

    public static String createProgressBar(char character, char has, char need, int amount, double current,
            double max) {
        StringBuilder bar = new StringBuilder();
        double percentage = (current / max);
        double count = amount * percentage;

        if (count > 0) {
            bar.append("§").append(has);

            for (int a = 0; a < count; a++)
                bar.append(character);
        }

        if (amount - count > 0) {
            bar.append("§").append(need);

            for (int a = 0; a < amount - count; a++)
                bar.append(character);
        }

        return bar.toString();
    }

    public static String createProgressBar(char character, char need, int amount, double current, double max) {
        return createProgressBar(character, 'a', need, amount, current, max);
    }

    public static String createProgressBar(char character, int amount, double current, double max) {
        return createProgressBar(character, 'a', 'c', amount, current, max);
    }

    public static boolean isColor(ChatColor color) {
        return !(color == ChatColor.BOLD || color == ChatColor.ITALIC || color == ChatColor.UNDERLINE ||
                color == ChatColor.STRIKETHROUGH || color == ChatColor.MAGIC || color == ChatColor.RESET);
    }

    public static String getLastColors(String input) {
        StringBuilder result = new StringBuilder();
        int length = input.length();

        // Search backwards from the end as it is faster
        for (int index = length - 1; index > -1; index--) {
            char section = input.charAt(index);
            if (section == ChatColor.COLOR_CHAR && index < length - 1) {
                char c = input.charAt(index + 1);
                ChatColor color = ChatColor.getByChar(c);

                if (color != null) {
                    result.insert(0, color.toString());

                    // Once we find a color or reset we can stop searching
                    if (!isColor(color) || color.equals(ChatColor.RESET)) {
                        break;
                    }
                }
            }
        }

        return result.toString();
    }

    public static String formatToRoman(int number) {
        String[] roman = { "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X" };
        return roman[number - 1];
    }

    public static String formatString(String separator, Number... numbers) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < numbers.length; i++) {
            stringBuilder.append(CommonConst.DECIMAL_FORMAT.format(numbers[i].doubleValue()));

            if (i != numbers.length - 1) {
                stringBuilder.append(separator);
            }
        }

        return stringBuilder.toString().trim();
    }

    public static String formatString(Enum<?> toFormat) {
        return formatString(toFormat.name().replace("_", " "));
    }

    public static String centerString(String string) {
        return centerString(string, 154);
    }

    public static String centerString(String string, int center) {
        if (string == null || string.equals("")) {
            return "";
        }
        string = string.replace("&", "§");

        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for (char c : string.toCharArray()) {
            if (c == '§') {
                previousCode = true;
                continue;
            } else if (previousCode == true) {
                previousCode = false;
                if (c == 'l' || c == 'L') {
                    isBold = true;
                    continue;
                } else {
                    isBold = false;
                }
            } else {
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = center - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;
        StringBuilder stringBuilder = new StringBuilder();
        while (compensated < toCompensate) {
            stringBuilder.append(" ");
            compensated += spaceLength;
        }
        return stringBuilder.toString() + string;
    }

    public static String join(List<String> input, String separator) {
        if (input == null || input.size() <= 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < input.size(); i++) {

            sb.append(input.get(i));

            if (i != input.size() - 1) {
                sb.append(separator);
            }
        }

        return sb.toString();
    }

    public static String join(String[] input, String separator) {
        if (input == null || input.length <= 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < input.length; i++) {

            sb.append(input[i]);

            if (i != input.length - 1) {
                sb.append(separator);
            }
        }

        return sb.toString();
    }

    public static String format(int time) {
        if (time >= 3600) {
            int hours = (time / 3600), minutes = (time % 3600) / 60, seconds = (time % 3600) % 60;
            return (hours < 10 ? "0" : "") + hours + ":" + (minutes < 10 ? "0" : "") + minutes + ":" +
                    (seconds < 10 ? "0" : "") + seconds;
        } else {
            int minutes = (time / 60), seconds = (time % 60);
            return minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
        }
    }

    public static String formatTime(int time) {
        return formatTime(time, TimeFormat.SHORT);
    }

    public static String formatTime(int time, TimeFormat timeFormat) {
        int days = (time / 3600) / 24, hours = (time / 3600) % 24, minutes = (time % 3600) / 60, seconds =
                (time % 3600) % 60;

        switch (timeFormat) {
        case SIMPLIFIED:
            if (days > 0) {
                return days + " dia" + (days == 1 ? "" : "s");
            } else if (hours > 0) {
                return hours + " hora" + (hours == 1 ? "" : "s");
            } else if (minutes > 0) {
                return minutes + " minuto" + (minutes == 1 ? "" : "s");
            } else {
                return seconds + " segundo" + (seconds == 1 ? " " : "s");
            }
        case SHORT_SIMPLIFIED:
            if (days > 0) {
                return days + "d";
            } else if (hours > 0) {
                return hours + "h";
            } else if (minutes > 0) {
                return minutes + "m";
            } else {
                return seconds + "s";
            }
        case SHORT:
            return (days > 0 ? days + "d" + (hours > 0 || minutes > 0 || seconds > 0 ? " " : "") : "") +
                    (hours > 0 ? hours + "h" + (seconds > 0 || minutes > 0 ? " " : "") : "") +
                    (minutes > 0 ? minutes + "m" + (seconds > 0 ? " " : "") : "") +
                    (seconds == 0 && (days > 0 || hours > 0 || minutes > 0) ? "" : seconds + "s");
        case NORMAL:
            return (days > 0 ?
                    days + " dia" + (days == 1 ? "" : "s") + (hours > 0 || minutes > 0 || seconds > 0 ? " " : "") :
                    "") +
                    (hours > 0 ? hours + " hora" + (days == 1 ? "" : "s") + (seconds > 0 || minutes > 0 ? " " : "") :
                            "") +
                    (minutes > 0 ? minutes + " minuto" + (minutes == 1 ? "" : "s") + (seconds > 0 ? " " : "") : "") +
                    (seconds == 0 && (days > 0 || hours > 0 || minutes > 0) ? "" :
                            seconds + " segundo" + (seconds == 1 ? "" : "s"));
        case DOUBLE_DOT:
            return "" + (hours > 0 ? (hours >= 10 ? hours : "0" + hours) + ":" : "") +
                    (minutes >= 10 ? minutes : "0" + minutes) + ":" + (seconds >= 10 ? seconds : "0" + seconds);
        default:
            return "";
        }
    }

    public static String formatString(String string) {
        if (string.isEmpty()) {
            return string;
        }

        char[] stringArray = string.toLowerCase().toCharArray();
        stringArray[0] = Character.toUpperCase(stringArray[0]);
        return new String(stringArray);
    }

    public static String formatToCamelCase(String string) {
        if (string.isEmpty()) {
            return string;
        }

        boolean camelCase = true;
        StringBuilder stringBuilder = new StringBuilder();

        for (char test : string.toCharArray()) {
            if (camelCase) {
                stringBuilder.append(Character.toUpperCase(test));
                camelCase = false;
                continue;
            }

            if (test == ' ') {
                camelCase = true;
            }

            stringBuilder.append(Character.toLowerCase(test));
        }

        return stringBuilder.toString().trim();
    }

    public static String getName(Enum<?> e) {
        String name = e.name();
        String[] names = name.split("_");

        for (int i = 0; i < names.length; i++)
            names[i] = i == 0 ? formatString(names[i]) : names[i].toUpperCase();

        return join(names, " ");
    }

    public static String getName(String string) {
        return toReadable(string);
    }

    public static String toReadable(String string) {
        String[] names = string.split("_");

        for (int i = 0; i < names.length; i++)
            names[i] = names[i].charAt(0) + names[i].substring(1).toLowerCase();

        return join(names, " ");
    }

    public static OptionalLong parseLong(String string) {
        try {
            Long integer = Long.parseLong(string);
            return OptionalLong.of(integer);
        } catch (NumberFormatException ex) {
            return OptionalLong.empty();
        }
    }

    public static OptionalLong parseLong(Object object) {
        return parseLong(String.valueOf(object));
    }

    public static OptionalInt parseInt(String string) {
        try {
            Integer integer = Integer.parseInt(string);
            return OptionalInt.of(integer);
        } catch (NumberFormatException ex) {
            return OptionalInt.empty();
        }
    }

    public static OptionalInt parseInt(Object object) {
        return parseInt(String.valueOf(object));
    }

    public static OptionalDouble parseDouble(String string) {
        try {
            Double integer = Double.parseDouble(string);
            return OptionalDouble.of(integer);
        } catch (NumberFormatException ex) {
            return OptionalDouble.empty();
        }
    }

    public static OptionalDouble parseDouble(Object object) {
        return parseDouble(String.valueOf(object));
    }

    public static boolean parseBoolean(String string) {
        return string.equalsIgnoreCase("true");
    }

    public static String getPositionFormat(int position) {
        return (position == 1 ? "§a" : position == 2 ? "§e" : position == 3 ? "§c" : "§7") + position + "° ";
    }

    public static String formatRomane(int level) {
        switch (level) {
        case 1:
            return "I";
        case 2:
            return "II";
        case 3:
            return "III";
        case 4:
            return "IV";
        case 5:
            return "V";
        default:
            return String.valueOf(level);
        }
    }

    public static String formatTime(long time) {
        if (time <= 0L) {
            return "";
        }

        time += 2L;

        long day = TimeUnit.SECONDS.toDays(time);
        long hours = TimeUnit.SECONDS.toHours(time) - day * 24L;
        long minutes = TimeUnit.SECONDS.toMinutes(time) - TimeUnit.SECONDS.toHours(time) * 60L;
        long seconds = TimeUnit.SECONDS.toSeconds(time) - TimeUnit.SECONDS.toMinutes(time) * 60L;

        StringBuilder sb = new StringBuilder();
        if (day > 0L) {
            sb.append(day).append(" ").append("dia").append(day == 1 ? "" : "s");

            if (hours > 0L) {
                sb.append(" ");
            }
        }
        if (hours > 0L) {
            sb.append(hours).append(" ").append("hora").append(hours == 1 ? "" : "s");
            if (minutes > 0L) {
                sb.append(" ");
            }
        }
        if (minutes > 0L) {
            sb.append(minutes).append(" ").append("minuto").append(minutes == 1 ? "" : "s");
            if (seconds > 0L) {
                sb.append(" ");
            }
        }

        if (seconds > 0L) {
            sb.append(seconds).append(" ").append("segundo").append(seconds == 1 ? "" : "s");
        }

        String diff = sb.toString().trim();

        return diff.isEmpty() ? "0 dia" : diff;
    }

    public static long getTimeFromString(String time, boolean future) {
        Pattern timePattern = Pattern.compile("(?:([0-9]+)\\s*y[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*mo[a-z]*[,\\s]*)?" +
                                                      "(?:([0-9]+)\\s*w[a-z]*[,\\s]*)?" +
                                                      "(?:([0-9]+)\\s*d[a-z]*[,\\s]*)?" +
                                                      "(?:([0-9]+)\\s*h[a-z]*[,\\s]*)?" +
                                                      "(?:([0-9]+)\\s*m[a-z]*[,\\s]*)?" +
                                                      "(?:([0-9]+)\\s*(?:s[a-z]*)?)?", Pattern.CASE_INSENSITIVE);

        Matcher m = timePattern.matcher(time);

        int years = 0;
        int months = 0;
        int weeks = 0;
        int days = 0;
        int hours = 0;
        int minutes = 0;
        int seconds = 0;

        boolean found = false;

        while (m.find()) {
            if (m.group() == null || m.group().isEmpty()) {
                continue;
            }

            for (int i = 0; i < m.groupCount(); i++) {
                if (m.group(i) != null && !m.group(i).isEmpty()) {
                    found = true;
                    break;
                }
            }

            if (found) {
                if (m.group(1) != null && !m.group(1).isEmpty()) {
                    years = Integer.parseInt(m.group(1));
                }

                if (m.group(2) != null && !m.group(2).isEmpty()) {
                    months = Integer.parseInt(m.group(2));
                }

                if (m.group(3) != null && !m.group(3).isEmpty()) {
                    weeks = Integer.parseInt(m.group(3));
                }

                if (m.group(4) != null && !m.group(4).isEmpty()) {
                    days = Integer.parseInt(m.group(4));
                }

                if (m.group(5) != null && !m.group(5).isEmpty()) {
                    hours = Integer.parseInt(m.group(5));
                }

                if (m.group(6) != null && !m.group(6).isEmpty()) {
                    minutes = Integer.parseInt(m.group(6));
                }

                if (m.group(7) != null && !m.group(7).isEmpty()) {
                    seconds = Integer.parseInt(m.group(7));
                }

                break;
            }
        }

        Preconditions.checkArgument(found, "Illegal Date");

        Calendar c = new GregorianCalendar();

        if (years > 0) {
            c.add(Calendar.YEAR, years * (future ? 1 : -1));
        }
        if (months > 0) {
            c.add(Calendar.MONTH, months * (future ? 1 : -1));
        }
        if (weeks > 0) {
            c.add(Calendar.WEEK_OF_YEAR, weeks * (future ? 1 : -1));
        }
        if (days > 0) {
            c.add(Calendar.DAY_OF_MONTH, days * (future ? 1 : -1));
        }
        if (hours > 0) {
            c.add(Calendar.HOUR_OF_DAY, hours * (future ? 1 : -1));
        }
        if (minutes > 0) {
            c.add(Calendar.MINUTE, minutes * (future ? 1 : -1));
        }
        if (seconds > 0) {
            c.add(Calendar.SECOND, seconds * (future ? 1 : -1));
        }

        return c.getTimeInMillis();
    }
}
