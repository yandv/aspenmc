package br.com.aspenmc.bukkit.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Optional;

@AllArgsConstructor
@Getter
public class Location {

    public static final Location NULL = new Location(
            Bukkit.getWorlds().stream().findFirst().map(World::getName).orElse("world"), 0, 0, 0, 0, 0);

    private String worldName;

    private double x, y, z;
    private float yaw, pitch;

    public org.bukkit.Location toLocation() {
        return new org.bukkit.Location(getWorld().orElse(null), x, y, z, yaw, pitch);
    }

    public Optional<World> getWorld() {
        return Optional.ofNullable(Bukkit.getWorld(worldName));
    }

    public static Location fromPlayer(Player player) {
        return fromLocation(player.getLocation());
    }

    public static Location fromLocation(org.bukkit.Location location) {
        return new Location(location.getWorld().getName(), location.getX(), location.getY(), location.getZ(),
                            location.getYaw(), location.getPitch());
    }
}
