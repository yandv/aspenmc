package br.com.aspenmc.manager;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.entity.sender.member.Member;
import br.com.aspenmc.entity.sender.member.MemberVoid;
import br.com.aspenmc.entity.sender.member.gamer.Gamer;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class MemberManager {

    private final Map<UUID, Member> memberMap;
    private final Map<String, Map<UUID, Gamer<?>>> gamerMap;

    private final Lock memberWriteLock = new ReentrantLock();
    private final Lock gamerWriteLock = new ReentrantLock();

    public MemberManager() {
        this.memberMap = new HashMap<>();
        this.gamerMap = new HashMap<>();
    }

    public void loadMember(Member member) {
        try {
            memberWriteLock.lock();
            memberMap.put(member.getUniqueId(), member);
        } finally {
            memberWriteLock.unlock();
        }
    }

    public <K, T extends Gamer<K>> void loadGamer(UUID uniqueId, String gamerId, T gamer) {
        try {
            gamerWriteLock.lock();
            this.gamerMap.computeIfAbsent(gamerId, k -> new HashMap<>()).put(uniqueId, gamer);
        } finally {
            gamerWriteLock.unlock();
        }
    }

    public Optional<Member> getMemberById(UUID uniqueId) {
        return Optional.ofNullable(this.memberMap.get(uniqueId));
    }

    public Optional<Member> getMemberByName(String playerName) {
        return this.memberMap.values().stream().filter(m -> m.getName().equalsIgnoreCase(playerName)).findFirst();
    }

    public <T extends Member> Optional<T> getMemberById(UUID uniqueId, Class<T> clazz) {
        return getMemberById(uniqueId).map(clazz::cast);
    }

    public Optional<Gamer<?>> getGamerById(UUID uniqueId, String gamerId) {
        return Optional.ofNullable(this.gamerMap.getOrDefault(gamerId, Collections.emptyMap()).get(uniqueId));
    }

    public <K, T extends Gamer<K>> Optional<T> getGamerById(UUID uniqueId, Class<T> clazz, String gamerId) {
        return getGamerById(uniqueId, gamerId).map(clazz::cast);
    }

    public Optional<? extends Member> getOrLoadByName(String playerName) {
        Member member = this.memberMap.values().stream().filter(m -> m.getName().equalsIgnoreCase(playerName))
                                      .findFirst().orElse(null);

        if (member == null) {
            member = CommonPlugin.getInstance().getMemberService().getMemberByName(playerName, true).join();

            if (member != null) {
                member.loadConfiguration();
            }
        }


        return Optional.ofNullable(member);
    }

    public Optional<? extends Member> getOrLoadById(UUID uniqueId) {
        if (this.memberMap.containsKey(uniqueId)) {
            return Optional.of(this.memberMap.get(uniqueId));
        }

        Member member = CommonPlugin.getInstance().getMemberService().getMemberById(uniqueId, MemberVoid.class).join();

        if (member == null) return Optional.empty();
        member.loadConfiguration();
        return Optional.of(member);
    }

    public <T extends Member> Optional<T> getOrLoadById(UUID uniqueId, Class<T> clazz) {
        if (this.memberMap.containsKey(uniqueId)) {
            return Optional.of(clazz.cast(this.memberMap.get(uniqueId)));
        }

        T member = CommonPlugin.getInstance().getMemberService().getMemberById(uniqueId, clazz).join();

        if (member == null) return Optional.empty();
        member.loadConfiguration();
        return Optional.of(member);
    }

    public Collection<? extends Member> getMembers() {
        return this.memberMap.values();
    }

    public <T extends Member> Collection<T> getMembers(Class<T> clazz) {
        return this.memberMap.values().stream().filter(clazz::isInstance).map(clazz::cast).collect(Collectors.toList());
    }


    public <K, T extends Gamer<K>> Collection<T> getGamers(String gamerId, Class<T> clazz) {
        Set<T> set = new HashSet<>();

        Map<UUID, Gamer<?>> uuidGamerMap = this.gamerMap.get(gamerId);

        if (uuidGamerMap == null) {
            return set;
        }

        for (Gamer<?> gamer : uuidGamerMap.values()) {
            if (clazz.isInstance(gamer)) {
                set.add(clazz.cast(gamer));
            }
        }

        return set;
    }

    public <K, T extends Gamer<K>> Collection<T> getGamers(Class<T> clazz) {
        Set<T> set = new HashSet<>();

        for (Map<UUID, Gamer<?>> uuidGamerMap : this.gamerMap.values()) {
            uuidGamerMap.values().stream().filter(clazz::isInstance).map(clazz::cast).findFirst().ifPresent(set::add);
        }

        return set;
    }

    public Collection<? extends Gamer<?>> getGamers(UUID uniqueId) {
        Set<Gamer<?>> gamerSet = new HashSet<>();

        for (Map<UUID, Gamer<?>> uuidGamerMap : this.gamerMap.values()) {
            Gamer<?> gamer = uuidGamerMap.get(uniqueId);

            if (gamer != null) {
                gamerSet.add(gamer);
            }
        }

        return gamerSet;
    }

    @SuppressWarnings("unchecked")
    public <K> Collection<? extends Gamer<K>> getGamers(UUID uniqueId, Class<K> entity) {
        Set<Gamer<K>> gamerSet = new HashSet<>();

        for (Map<UUID, Gamer<?>> uuidGamerMap : this.gamerMap.values()) {
            Gamer<?> gamer = uuidGamerMap.get(uniqueId);

            if (gamer != null && gamer.getEntityClass().equals(entity)) {
                gamerSet.add((Gamer<K>) gamer);
            }
        }

        return gamerSet;
    }

    public Collection<? extends Gamer<?>> getGamers(Collection<UUID> ids, String gamerId) {
        Set<Gamer<?>> gamerSet = new HashSet<>();

        for (UUID id : ids) {
            getGamerById(id, gamerId).ifPresent(gamerSet::add);
        }

        return gamerSet;
    }

    public <K, T extends Gamer<K>> Collection<T> getGamers(Collection<UUID> ids, Class<T> gamerClass, String gamerId) {
        Set<T> gamerSet = new HashSet<>();

        for (UUID id : ids) {
            getGamerById(id, gamerClass, gamerId).ifPresent(gamerSet::add);
        }

        return gamerSet;
    }

    public void unloadMember(UUID uniqueId) {
        try {
            memberWriteLock.lock();
            memberMap.remove(uniqueId);
        } finally {
            memberWriteLock.unlock();
        }
    }

    public void unloadGamer(UUID uniqueId, String gamerId) {
        try {
            gamerWriteLock.lock();

            if (this.gamerMap.containsKey(gamerId)) {
                Map<UUID, Gamer<?>> uuidGamerMap = this.gamerMap.get(gamerId);

                uuidGamerMap.remove(uniqueId);

                if (uuidGamerMap.isEmpty()) {
                    this.gamerMap.remove(gamerId);
                }
            }
        } finally {
            gamerWriteLock.unlock();
        }
    }

    public void unloadGamer(UUID uniqueId) {
        try {
            gamerWriteLock.lock();
            this.gamerMap.remove(uniqueId);
        } finally {
            gamerWriteLock.unlock();
        }
    }
}
