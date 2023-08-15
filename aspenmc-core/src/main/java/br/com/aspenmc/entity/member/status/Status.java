package br.com.aspenmc.entity.member.status;

import br.com.aspenmc.CommonPlugin;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class Status {

    private final UUID uniqueId;
    private final StatusType statusType;

    private final Map<String, Long> statusMap;

    public Status(UUID uniqueId, StatusType statusType) {
        this.uniqueId = uniqueId;
        this.statusType = statusType;

        this.statusMap = new HashMap<>();
    }

    public long get(String id) {
        return this.statusMap.getOrDefault(id.toLowerCase(), 0L);
    }

    public void set(String id, long value) {
        this.statusMap.put(id.toLowerCase(), value);
        save(id);
    }

    public void add(String id, long value) {
        this.statusMap.put(id.toLowerCase(), this.get(id) + value);
        save(id);
    }

    private void save(String id) {
        CommonPlugin.getInstance().getStatusData().saveStatus(this, id.toLowerCase());
    }
}
