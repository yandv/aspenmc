package br.com.aspenmc.bukkit.manager;

import br.com.aspenmc.bukkit.BukkitCommon;
import br.com.aspenmc.bukkit.serializer.LocationParser;
import com.comphenix.net.bytebuddy.jar.asm.commons.Remapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LocationManager {

    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(Location.class, new LocationParser())
                                                      .create();

    private final Map<String, Location> locationMap;

    public LocationManager() {
        this.locationMap = new HashMap<>();

        if (BukkitCommon.getInstance().getConfig().contains("locations")) {
            for (String key : BukkitCommon.getInstance().getConfig().getConfigurationSection("locations")
                                          .getKeys(false)) {
                this.locationMap.put(key.toLowerCase(),
                        GSON.fromJson(BukkitCommon.getInstance().getConfig().getString("locations." + key),
                                Location.class));
            }
        }
    }

    public void setLocation(String locationName, Location location) {
        this.locationMap.put(locationName.toLowerCase(), location);
        BukkitCommon.getInstance().getConfig().set("locations." + locationName.toLowerCase(), GSON.toJson(location));
        BukkitCommon.getInstance().saveConfig();
    }

    public Location getLocation(String locationName) {
        if (!locationMap.containsKey(locationName.toLowerCase())) {
            locationMap.put(locationName.toLowerCase(), Bukkit.getWorlds().get(0).getSpawnLocation());
        }

        return locationMap.get(locationName.toLowerCase());
    }

    public Optional<Location> getLocationAsOptional() {
        return Optional.ofNullable(locationMap.get("spawn"));
    }

    public Collection<String> getKeys() {
        return locationMap.keySet();
    }
}


