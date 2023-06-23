package br.com.aspenmc.backend.data;

import br.com.aspenmc.entity.member.gamer.Gamer;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface GamerData {

    <T extends Gamer> CompletableFuture<T> loadGamer(UUID uniqueId, String gamerId, Class<T> gamerClass) ;

    @SuppressWarnings("unchecked")
    CompletableFuture<List<? extends Gamer>> loadGamer(UUID uniqueId, Map.Entry<String, Class<? extends Gamer>>... classes);

    void save(Gamer gamer, String[] fields);
}
