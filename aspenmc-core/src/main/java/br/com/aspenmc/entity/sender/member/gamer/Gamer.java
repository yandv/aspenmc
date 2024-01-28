package br.com.aspenmc.entity.sender.member.gamer;

import br.com.aspenmc.CommonPlugin;

import java.util.UUID;

public interface Gamer<T> {

    /**
     * Retrieve the unique id of the gamer
     * @return the unique id of the gamer
     */

    UUID getUniqueId();

    void loadEntity(T t);

    T getEntity();

    Class<T> getEntityClass();

    String getId();

    default void save(String... fields) {
        CommonPlugin.getInstance().getGamerService().save(this, fields);
    }

}
