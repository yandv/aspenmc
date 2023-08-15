package br.com.aspenmc.backend.data;

import br.com.aspenmc.clan.Clan;
import br.com.aspenmc.entity.Member;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ClanData {


    <T extends Clan> CompletableFuture<T> getClanById(UUID clanId, Class<T> clazz);

    <T extends Clan> CompletableFuture<T> getClanByName(String clanName, String clanAbbreviation, Class<T> clazz);

    <T extends Clan> T createClan(T clan);

    CompletableFuture<UUID> getClanId();

    void deleteClan(Clan clan);

    void updateClan(Clan clan, String... fields);
}
