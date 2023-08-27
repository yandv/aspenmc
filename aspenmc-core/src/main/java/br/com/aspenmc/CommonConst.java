package br.com.aspenmc;

import br.com.aspenmc.utils.Zip;
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

    public static final ExecutorService PRINCIPAL_EXECUTOR = Executors.newFixedThreadPool(2,
            new ThreadFactoryBuilder().setNameFormat("AspenMC - Executor").build());

    public static final Gson GSON = new GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.STATIC)
                                                     .create();

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

    public static final String SKIN_FETCHER =
            "https://sessionserver.mojang" + ".com/session/minecraft/profile/%s?unsigned=false";

    public static final String WEBSITE = "www.aspenmc.com.br";
    public static final String DISCORD = "discord.gg/aspenmc";

    public static final String PRINCIPAL_DIRECTORY =
            System.getenv("PRINCIPAL_DIRECTORY") != null ? Zip.separatorsToUnix(System.getenv("PRINCIPAL_DIRECTORY")) :
                    "C:/Users/Administrator/Desktop/AspenMC/";
    public static final String DEFAULT_SKIN_NAME = "Sem pele";
    public static final String DEFAULT_SKIN_VALUE =
            "ewogICJ0aW1lc3RhbXAiIDogMTY3NzI0NTI0OTE5MCwKICAicHJvZmlsZUlkIiA6ICI4NzQ3ODgyNjc2NzI0OTk1ODU1ODMwN2FiMWI3ZDRjZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJUZXN0ZSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8yYzFjYzE5YzYzZDM4MmI3ZTI5MzhkZWE4NGZmZmYzODYxMmJkM2IwNjM3NzY4NzkwZTZkNTJkNzEwNDZhNGIyIgogICAgfQogIH0KfQ==";
    public static final String SIGNATURE =
            "qBgm2nqCyotJW0obDPws0w0iFjlyAK1kEyc3bAPukNPEu6mhdXi2VtVhSeQoW80DlTCn8Svcxzu2/8PGxYbT5/DkvHfA" +
                    "/yqgrgN6r3rSktC/AQMw6QxX/+h0r76ySO5VbcwPyhqekBcyu" +
                    "+EnuvOJ8nwdUKdVdZaHN4BYiHBtaKCwkG6GuhfrsDnxC5sjHa1GxkY9w9Wb83Zwn1lW" +
                    "+qFI8leeobYhPcmO9Y6a2B0u76yc55UoeHdxuuehtweeAKI3pKaCO0ckMBRMV4qhPbvWIFNJhNDTfjrR4JWwK4+tmq" +
                    "//3C470Cz4NQg0rNpe5yCBhxctn3yBJrs5M0fQKH559UdQ5wmdYufMtHy8HIa16jqn58UhJxN4P0A8KNwrL8qIOe67nCny" +
                    "+aATOWBo/IAywA4rITFsTAVCP5ViJyNOszEi4oj+/xbdUoDpqeLHJGJmef+PoP5oSvNfha/ZfTYXD+b4odN5SDema7xS" +
                    "/JLl774zDJCBPH47Y8fkY5tYdM/gk7lODMZHCRDCVErhXQqI4Bu9fY5z4Hnl8nUqQjKAn6UNjRA0xkxtL9SUPqD2l" +
                    "+OaUay9rJhcoyLNPr55v8P9qbHi1bg7zlcaXFMBcPiUdG8karSl8fhyfQ27AF94lF5L3kSH5yxa" +
                    "+ksOYYrXImRvDIsiFs45sqvFF0TnI8NQRYU=";

    public static Double getCpuUse() {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
            AttributeList list = mbs.getAttributes(name, new String[] { "ProcessCpuLoad" });

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
