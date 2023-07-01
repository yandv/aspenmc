package br.com.aspenmc.bukkit.utils;

import org.bukkit.entity.Player;

/**
 * Class to watch a touch
 *
 * @author yandv
 */

public interface TouchHandler<T> {

    public void onTouch(T type, Player player, boolean right);

    public enum TouchType {

        LEFT,
        RIGHT;

    }
}
