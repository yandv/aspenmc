package br.com.aspenmc.bungee.manager;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import br.com.aspenmc.CommonConst;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bungee.BungeeMain;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.config.Configuration;

import java.util.*;

public class MotdManager {

    private Map<String, List<Motd>> motdListMap;

    public MotdManager() {
        this.motdListMap = new HashMap<>();

        this.motdListMap.put("server-not-found", Collections.singletonList(new Motd("server-not-found",
                                                                                    BungeeMain.getInstance().getConfig()
                                                                                              .getSection(
                                                                                                      "motds.server-not-found"))));
        this.motdListMap.put("maintenance", Collections.singletonList(
                new Motd("server-not-found", BungeeMain.getInstance().getConfig().getSection("motds.maintenance"))));

        this.motdListMap.put("random", new ArrayList<>());

        for (String server : BungeeMain.getInstance().getConfig().getSection("motds.random").getKeys()) {
            Configuration section = BungeeMain.getInstance().getConfig().getSection("motds.random." + server);

            if (section == null) {
                this.motdListMap.get("random").add(new Motd(server, null));
            } else {
                for (String motd : section.getKeys()) {
                    this.motdListMap.get("random").add(new Motd(motd, section));
                }
            }
        }

        System.out.println(CommonConst.GSON.toJson(this.motdListMap));
    }

    public Motd getServerNotFound() {
        return this.motdListMap.get("server-not-found").get(0);
    }

    public Motd getMaintenance() {
        return this.motdListMap.get("maintenance").get(0);
    }

    public Motd getRandomMotd() {
        return this.motdListMap.get("random").get(CommonConst.RANDOM.nextInt(this.motdListMap.get("random").size()));
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public class Motd {

        private String name;
        private String header;
        private String footer;

        public Motd(String name, Configuration section) {
            this.name = name;

            if (section == null) {
                this.header = "";
                this.footer = "";
                CommonPlugin.getInstance().debug("Motd " + name + " is null");
            } else {
                if (section.contains("header")) {
                    this.header = ChatColor.translateAlternateColorCodes('&', section.getString("header"));
                } else {
                    this.header = "";
                }

                if (section.contains("footer")) {
                    this.footer = ChatColor.translateAlternateColorCodes('&', section.getString("footer"));
                } else {
                    this.footer = "";
                }
            }
        }

        public String getAsString() {
            return header + "\n" + footer;
        }
    }
}
