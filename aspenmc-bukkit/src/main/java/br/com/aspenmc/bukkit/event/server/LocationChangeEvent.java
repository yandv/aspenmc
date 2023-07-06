package br.com.aspenmc.bukkit.event.server;

import br.com.aspenmc.bukkit.event.NormalEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;

@AllArgsConstructor
@Getter
public class LocationChangeEvent extends NormalEvent {

    private String configName;
    private Location oldLocation;
    private Location location;
}
