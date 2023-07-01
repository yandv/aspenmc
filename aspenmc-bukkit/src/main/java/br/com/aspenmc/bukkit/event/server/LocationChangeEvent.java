package br.com.aspenmc.bukkit.event.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.minehurt.bukkit.event.NormalEvent;
import me.minehurt.bukkit.utils.Location;

@AllArgsConstructor
@Getter
public class LocationChangeEvent extends NormalEvent {

    private String configName;
    private Location oldLocation;
    private Location location;
}
