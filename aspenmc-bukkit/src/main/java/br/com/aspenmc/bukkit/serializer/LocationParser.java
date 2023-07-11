package br.com.aspenmc.bukkit.serializer;

import java.lang.reflect.Type;

import br.com.aspenmc.utils.json.JsonBuilder;
import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class LocationParser implements JsonDeserializer<Location>, JsonSerializer<Location> {

    @Override
    public JsonElement serialize(Location location, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonBuilder().addProperty("world", location.getWorld().getName()).addProperty("x", location.getX())
                                .addProperty("y", location.getY()).addProperty("z", location.getZ())
                                .addProperty("yaw", location.getYaw()).addProperty("pitch", location.getPitch())
                                .build();
    }

    @Override
    public Location deserialize(JsonElement jsonElement, Type type,
            JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return new Location(Bukkit.getWorld(jsonObject.get("world").getAsString()), jsonObject.get("x").getAsDouble(),
                jsonObject.get("y").getAsDouble(), jsonObject.get("z").getAsDouble(),
                jsonObject.get("yaw").getAsFloat(), jsonObject.get("pitch").getAsFloat());
    }
}

