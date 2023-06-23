package br.com.aspenmc.backend.data;

import br.com.aspenmc.entity.Member;
import br.com.aspenmc.entity.Sender;
import br.com.aspenmc.punish.Punish;
import br.com.aspenmc.punish.PunishType;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface PunishData {

    CompletableFuture<Punish> createPunish(Member target, Sender sender, PunishType punishType, String reason, long expiresAt);

    CompletableFuture<Punish> getPunishById(String punishId);

    CompletableFuture<Map<PunishType, List<Punish>>> getPunish(int page, int limit, String[] filters, Object... values);

    default CompletableFuture<Map<PunishType, List<Punish>>> getPunish(String[] filters, Object... values) {
        return getPunish(1, 20, filters, values);
    }

    void updatePunish(Punish punish, String... fields);
}
