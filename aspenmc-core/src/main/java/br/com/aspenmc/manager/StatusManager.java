package br.com.aspenmc.manager;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.entity.sender.member.status.League;
import br.com.aspenmc.entity.sender.member.status.Status;
import br.com.aspenmc.entity.sender.member.status.StatusType;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class StatusManager {

    private Map<StatusType, Map<UUID, Status>> statusMap;

    @Setter
    private LeagueChange leagueChangeObserver;

    public StatusManager() {
        this.statusMap = new HashMap<>();
    }

    public void loadStatus(Status status) {
        statusMap.computeIfAbsent(status.getStatusType(), k -> new HashMap<>())
                 .computeIfAbsent(status.getUniqueId(), k -> {
                     statusMap.get(status.getStatusType()).put(status.getUniqueId(), status);
                     return status;
                 });
    }

    public void apply(UUID uniqueId, StatusType statusType, Consumer<Status> statusConsumer) {
        if (hasLoadedStatus(uniqueId, statusType)) {
            statusConsumer.accept(getStatusById(uniqueId, statusType));
        } else {
            CommonPlugin.getInstance().getStatusService().getStatusById(uniqueId, statusType)
                        .whenComplete((status, throwable) -> {
                            if (status != null) {
                                loadStatus(status);
                                statusConsumer.accept(status);
                            }
                        });
        }
    }

    public Status getStatusById(UUID uniqueId, StatusType statusType) {
        if (!statusMap.containsKey(statusType)) {
            return null;
        }

        return statusMap.get(statusType).get(uniqueId);
    }

    public Status getOrLoadById(UUID uniqueId, StatusType statusType) {
        if (!statusMap.containsKey(statusType)) {
            statusMap.put(statusType, new HashMap<>());
        }

        if (!statusMap.get(statusType).containsKey(uniqueId)) {
            Status status = CommonPlugin.getInstance().getStatusService().getStatusById(uniqueId, statusType).join();
            statusMap.get(statusType).put(uniqueId, status);
            return status;
        }

        return statusMap.get(statusType).get(uniqueId);
    }

    public void unloadStatus(UUID uniqueId, StatusType statusType) {
        if (!statusMap.containsKey(statusType)) {
            return;
        }

        statusMap.get(statusType).remove(uniqueId);

        if (statusMap.get(statusType).isEmpty()) statusMap.remove(statusType);
    }

    public void unloadStatus(UUID uniqueId) {
        for (StatusType statusType : statusMap.keySet()) {
            unloadStatus(uniqueId, statusType);
        }
    }

    public boolean hasLoadedStatus(UUID uniqueId, StatusType statusType) {
        return statusMap.containsKey(statusType) && statusMap.get(statusType).containsKey(uniqueId);
    }

    public void onLeagueChange(Status status, League oldLeague, League newLeague) {
        if (leagueChangeObserver != null) {
            leagueChangeObserver.onLeagueChange(status, oldLeague, newLeague);
        }
    }

    public interface LeagueChange {

        void onLeagueChange(Status status, League oldLeague, League newLeague);
    }
}
