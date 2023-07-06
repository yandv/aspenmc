package br.com.aspenmc;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Modifier;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class CommonConst {

    public static final ExecutorService PRINCIPAL_EXECUTOR = Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder().setNameFormat("HighMC - Executor").build());

    public static final Gson GSON = new GsonBuilder()
            .excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.STATIC).create();

    public static final Gson GSON_PRETTY = new GsonBuilder().setPrettyPrinting().create();

    public static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    public static final DateFormat FULL_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    public static final NumberFormat DECIMAL_FORMAT = new DecimalFormat("#.##");

    public static final UUID CONSOLE_ID = UUID.fromString("f78a4d8d-d51b-4b39-98a3-230f2de0c670");

    public static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,16}$");

    public static final Random RANDOM = new Random();

    public static final String SERVER_PACKET_CHANNEL = "server-packet";

    public static final String ADMIN_MODE_PERMISSION = "command.admin";

    public static final String SERVER_FULL_PERMISSION = "server.full";

    public static final String SKIN_FETCHER = "https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false";

    public static final String WEBSITE = "www.aspenmc.com.br";
    public static final String DISCORD = "discord.gg/aspenmc";

    public static final String PRINCIPAL_DIRECTORY = "C:/Users/ALLAN/Desktop/allan/Servidores/AspenMC/";

    public static Double getCpuUse() {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
            AttributeList list = mbs.getAttributes(name, new String[]{"ProcessCpuLoad"});

            if (list.isEmpty()) {
                return Double.NaN;
            }

            Attribute att = (Attribute) list.get(0);
            Double value = (Double) att.getValue();

            if (value == -1.0) {
                return Double.NaN;
            }
            return (value * 1000.0) / 10.0;
        } catch (Exception e) {
            return 0.0;
        }
    }
}
