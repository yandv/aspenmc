package br.com.aspenmc.backend.data;

import br.com.aspenmc.entity.member.Skin;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface SkinData {


	Optional<Skin> loadData(String playerName);

	CompletableFuture<Skin> loadUserData(String playerName);

	void save(Skin skin, int seconds);

	String[] loadSkinById(UUID uuid);

	CompletableFuture<Skin> loadSkinById(UUID uuid, String skinName);

}
