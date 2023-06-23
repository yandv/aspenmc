package br.com.aspenmc.utils.mojang;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import br.com.aspenmc.CommonPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class UUIDFetcher {

	private List<String> apis = new ArrayList<>();

	private LoadingCache<String, UUID> cache = CacheBuilder.newBuilder().expireAfterWrite(1L, TimeUnit.DAYS)
			.expireAfterAccess(1L, TimeUnit.DAYS)
			.build(new CacheLoader<String, UUID>() {
				@Override
				public UUID load(String name) throws Exception {
					UUID uuid = CommonPlugin.getInstance().getPluginPlatform().getUniqueId(name);
					return request(name);
				}
			});

	public UUIDFetcher() {
		apis.add("https://api.mojang.com/users/profiles/minecraft/%s");
	}

	public UUID request(String name) {
		return request(0, apis.get(0), name);
	}

	public UUID request(int idx, String api, String name) {
		try {
			URLConnection con = new URL(String.format(api, name)).openConnection();

			JsonElement element = JsonParser.parseReader(
					new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)));

			if (element instanceof JsonObject) {
				JsonObject object = (JsonObject) element;
				if (object.has("error") && object.has("errorMessage")) {
					throw new Exception(object.get("errorMessage").getAsString());
				} else if (object.has("id")) {
					return UUIDParser.parse(object.get("id"));
				} else if (object.has("uuid")) {
					JsonObject uuid = object.getAsJsonObject("uuid");
					if (uuid.has("formatted")) {
						return UUIDParser.parse(object.get("formatted"));
					}
				}
			}
		} catch (Exception e) {
			idx++;

			if (idx < apis.size()) {
				api = apis.get(idx);
				return request(idx, api, name);
			}
		}

		return null;
	}

	/**
	 * Retrieve the UUID of a player
	 * If useServer is true, it will first try to get the UUID from the current server
	 *
	 * @param name The name of the player
	 * @param useServer Whether to use the server's UUID cache
	 * @return The UUID of the player
	 */

	public UUID getUniqueId(String name, boolean useServer) {
		if (useServer) {
			UUID uuid = CommonPlugin.getInstance().getPluginPlatform().getUniqueId(name);

			if (uuid != null) {
				return uuid;
			}
		}

		if (name != null && !name.isEmpty()) {
			if (name.matches("[a-zA-Z0-9_]{3,16}")) {
				try {
					return cache.get(name);
				} catch (Exception ignored) {
				}
			} else {
				return UUIDParser.parse(name);
			}
		}
		return null;
	}

	/**
	 * Retrieve the UUID of a player
	 * This will not use the server's UUID cache
	 *
	 * @param name The name of the player
	 * @return The UUID of the player
	 */

	public UUID getUniqueId(String name) {
		return getUniqueId(name, false);
	}

}
