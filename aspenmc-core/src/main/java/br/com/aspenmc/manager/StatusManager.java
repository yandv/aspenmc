package br.com.aspenmc.manager;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.entity.member.status.Status;
import br.com.aspenmc.entity.member.status.StatusType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatusManager {

    private Map<StatusType, Map<UUID, Status>> statusMap;

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
            return statusMap.get(statusType).put(uniqueId,
                    CommonPlugin.getInstance().getStatusData().getStatusById(uniqueId, statusType).join());
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
}
