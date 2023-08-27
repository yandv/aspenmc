package br.com.aspenmc.discord;

import br.com.aspenmc.CommonConst;
import br.com.aspenmc.CommonPlatform;
import br.com.aspenmc.CommonPlugin;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.UUID;

public class DiscordPlatform implements CommonPlatform {

    @Override
    public void broadcast(String... messages) {

    }

    @Override
    public void broadcast(TextComponent... components) {

    }

    @Override
    public String getNameById(UUID playerId) {
        return null;
    }

    @Override
    public UUID getUniqueId(String playerName) {
        return null;
    }

    @Override
    public void runAsync(Runnable runnable) {
        CommonConst.PRINCIPAL_EXECUTOR.execute(runnable);
    }

    @Override
    public void runAsyncTimer(Runnable runnable, long delay, long period) {

    }

    @Override
    public void runAsyncLater(Runnable runnable, long delay) {

    }

    @Override
    public void runSync(Runnable runnable) {
        runnable.run();
    }

    @Override
    public void runLater(Runnable runnable, long delay) {

    }

    @Override
    public void runTimer(Runnable runnable, long delay, long period) {

    }
}
