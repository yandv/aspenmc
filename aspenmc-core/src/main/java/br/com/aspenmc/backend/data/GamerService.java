package br.com.aspenmc.backend.data;

import br.com.aspenmc.entity.member.gamer.Gamer;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface GamerService {

    <E, T extends Gamer<E>> CompletableFuture<T> loadGamer(UUID uniqueId, String gamerId, Class<T> gamerClass) ;

    @SuppressWarnings("unchecked")
    <E> CompletableFuture<List<? extends Gamer<E>>> loadGamer(UUID uniqueId, Map.Entry<String, Class<? extends Gamer<E>>>... classes);

    <E, T extends Gamer<E>> CompletableFuture<List<T>> ranking(String fieldName, int page, int maxPerPage, String gamerId, Class<T> gamerClass);

    void save(Gamer<?> gamer, String[] fields);
}
