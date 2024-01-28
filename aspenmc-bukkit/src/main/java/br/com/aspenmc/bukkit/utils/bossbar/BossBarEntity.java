package br.com.aspenmc.bukkit.utils.bossbar;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bukkit.BukkitMain;
import br.com.aspenmc.bukkit.utils.bossbar.entity.WitherBoss;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftReflection;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.logging.Level;

@RequiredArgsConstructor
public abstract class BossBarEntity {

    @Getter
    protected String title;
    @Getter
    protected float health;
    @Getter
    protected int id;
    @NonNull
    @Getter
    private Player player;
    private BukkitTask task;

    public boolean setTitle(String title) {
        if (!Objects.equals(this.title, title)) {
            this.title = title;
            return true;
        }

        return false;
    }

    public boolean setHealth(float percent) {
        float minHealth = (this instanceof WitherBoss ? 151F : 1F);
        float maxHealth = (this instanceof WitherBoss ? 300F : 200F);
        float newHealth = Math.max(minHealth, (percent / 100F) * maxHealth);

        if (!Objects.equals(this.health, newHealth)) {
            this.health = newHealth;
            return true;
        }

        return false;
    }

    protected void sendPacket(Player player, PacketContainer packet) {
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public boolean isAlive() {
        return id > 0;
    }

    public void setAlive(boolean alive) {
        if (alive) {
            try {
                Class<?> clazz = MinecraftReflection.getEntityClass();
                Field field = clazz.getDeclaredField("entityCount");
                field.setAccessible(true);
                int id = field.getInt(null);
                field.set(null, id + 1);
                this.id = id;
            } catch (Exception ex) {
                CommonPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to get entity count.", ex);
            }
        } else {
            this.id = -1;
        }
    }

    public abstract void spawn();

    public abstract void remove();

    public abstract void update();

    public abstract void move(PlayerMoveEvent event);

    public void startTask(BukkitRunnable runnable) {
        if (task == null) task = runnable.runTaskTimer(BukkitMain.getInstance(), 20L, 20L);
    }

    public void cancelTask() {
        if (task != null) task.cancel();
    }

    public boolean hasTask() {
        return task != null;
    }
}