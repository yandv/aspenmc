package br.com.aspenmc.manager;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.entity.Member;
import br.com.aspenmc.entity.member.gamer.Gamer;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MemberManager {

    private final Map<UUID, Member> memberMap;
    private final Map<UUID, Map<String, Gamer>> gamerMap;

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

    public <T extends Gamer> void loadGamer(UUID uniqueId, String gamerId, T gamer) {
        try {
            gamerWriteLock.lock();
            this.gamerMap.computeIfAbsent(uniqueId, k -> new HashMap<>()).put(gamerId, gamer);
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

    public Optional<Gamer> getGamerById(UUID uniqueId, String gamerId) {
        return this.gamerMap.getOrDefault(uniqueId, Collections.emptyMap()).values().stream()
                            .filter(g -> g.getId().equalsIgnoreCase(gamerId)).findFirst();
    }

    public <T extends Gamer> Optional<T> getGamerById(UUID uniqueId, Class<T> clazz, String gamerId) {
        return this.gamerMap.getOrDefault(uniqueId, Collections.emptyMap()).values().stream()
                            .filter(g -> g.getId().equalsIgnoreCase(gamerId)).findFirst().map(clazz::cast);
    }

    public Optional<? extends Member> getOrLoadByName(String playerName) {
        Member member = this.memberMap.values().stream().filter(m -> m.getName().equalsIgnoreCase(playerName))
                                      .findFirst().orElse(null);

        if (member == null) {
            return CommonPlugin.getInstance().getMemberData().loadMemberByName(playerName, true).map(m -> {
                m.loadConfiguration();
                return m;
            });
        }


        return Optional.of(member);
    }

    public <T extends Member> Optional<T> getOrLoadById(UUID uniqueId, Class<T> clazz) {
        if (this.memberMap.containsKey(uniqueId)) {
            return Optional.of(clazz.cast(this.memberMap.get(uniqueId)));
        }

        return CommonPlugin.getInstance().getMemberData().loadMemberById(uniqueId, clazz);
    }

    public Collection<? extends Member> getMembers() {
        return this.memberMap.values();
    }

    public Collection<? extends Gamer> getGamers(UUID uniqueId) {
        return this.gamerMap.getOrDefault(uniqueId, Collections.emptyMap()).values();
    }

    public Collection<? extends Gamer> getGamers(Collection<UUID> ids, String gamerId) {
        Set<Gamer> gamerSet = new HashSet<>();

        for (UUID id : ids) {
            getGamerById(id, gamerId).ifPresent(gamerSet::add);
        }

        return gamerSet;
    }

    public <T extends Gamer> Collection<T> getGamers(Collection<UUID> ids, Class<T> gamerClass, String gamerId) {
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

            if (this.gamerMap.containsKey(uniqueId)) {
                Map<String, Gamer> gamerMap = this.gamerMap.get(uniqueId);

                gamerMap.remove(gamerId);

                if (gamerMap.isEmpty()) {
                    this.gamerMap.remove(uniqueId);
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
