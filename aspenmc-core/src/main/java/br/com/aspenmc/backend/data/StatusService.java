package br.com.aspenmc.backend.data;

import br.com.aspenmc.entity.sender.member.status.Status;
import br.com.aspenmc.entity.sender.member.status.StatusType;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface StatusService {

    CompletableFuture<Collection<Status>> getStatusById(UUID uniqueId);

    CompletableFuture<List<Status>> getStatusById(UUID uniqueId, Set<StatusType> preloadedStatus);

    CompletableFuture<Status> getStatusById(UUID uniqueId, StatusType statusType);

    CompletableFuture<List<Status>> ranking(StatusType statusType, String fieldName, int page, int perPage);

    void saveStatus(Status status, String... fields);
}
