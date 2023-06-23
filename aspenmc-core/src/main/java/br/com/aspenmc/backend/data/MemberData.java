package br.com.aspenmc.backend.data;

import br.com.aspenmc.entity.Member;
import br.com.aspenmc.entity.member.MemberVoid;
import org.bson.conversions.Bson;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface MemberData {

    /**
     * Loads a member by their UUID
     *
     * @param playerId The UUID of the member
     * @param clazz The class of the member instance
     * @return Optional of the member instance
     * @param <T> The type of the member instance
     */

    <T extends Member> Optional<T> loadMemberById(UUID playerId, Class<T> clazz);

    /**
     * Loads a member by their UUID
     *
     * @param playerId The UUID of the member
     * @return Optional of the member as MemberVoid
     */

    default Optional<MemberVoid> loadMemberById(UUID playerId) {
        return loadMemberById(playerId, MemberVoid.class);
    }

    /**
     * Loads a member by their name
     *
     * @param playerId The name of the member
     * @param clazz The class of the member instance
     * @return CompletableFuture of the member instance
     * @param <T> The type of the member instance
     */

    <T extends Member> CompletableFuture<T> loadMemberAsFutureById(UUID playerId, Class<T> clazz);

    /**
     * Loads a member by their name
     *
     * @param playerId The name of the member
     * @return CompletableFuture of the member as MemberVoid
     */

    default CompletableFuture<MemberVoid> loadMemberAsFutureById(UUID playerId) {
        return loadMemberAsFutureById(playerId, MemberVoid.class);
    }

    /**
     * Loads a member by their name
     *
     * @param playerName The name of the member
     * @param clazz The class of the member instance
     * @param ignoreCase Whether or not to ignore case
     * @return Optional of the member instance
     * @param <T> The type of the member instance
     */

    <T extends Member> Optional<T> loadMemberByName(String playerName, Class<T> clazz, boolean ignoreCase);

    /**
     * Loads a member by their name
     *
     * @param playerName The name of the member
     * @param ignoreCase Whether or not to ignore case
     * @return Optional of the member as MemberVoid
     */

    default Optional<MemberVoid> loadMemberByName(String playerName, boolean ignoreCase) {
        return loadMemberByName(playerName, MemberVoid.class, ignoreCase);
    }

    /**
     * Loads a list of members by filters
     *
     * @param clazz The class of the member instance
     * @param filters The filters to apply
     * @return List of members
     * @param <T> The type of the member instance
     */

    <T extends Member> List<T> loadMember(Class<T> clazz, Bson filters);

    /**
     * Loads a list of members by filters
     *
     * @param filters The filters to apply
     * @return List of members as MemberVoid
     */

    default List<MemberVoid> loadMember(Bson filters) {
        return loadMember(MemberVoid.class, filters);
    }

    /**
     * Load a member by their name
     *
     * @param playerName The name of the member
     * @param clazz The class of the member instance
     * @param ignoreCase Whether or not to ignore case
     * @return CompletableFuture of the member instance
     * @param <T> The type of the member instance
     */

    <T extends Member> CompletableFuture<T> loadMemberAsFutureByName(String playerName, Class<T> clazz, boolean ignoreCase);

    /**
     * Load a member by their name
     *
     * @param playerName The name of the member
     * @param ignoreCase Whether or not to ignore case
     * @return CompletableFuture of the member as MemberVoid
     */

    default CompletableFuture<MemberVoid> loadMemberAsFutureByName(String playerName, boolean ignoreCase) {
        return loadMemberAsFutureByName(playerName, MemberVoid.class, ignoreCase);
    }

    /**
     * Updates a member
     *
     * @param member The member to update
     * @param fields The fields to update
     */

    void updateMember(Member member, String... fields);

    /**
     * Save a member in the database
     *
     * @param member The member to create
     */

    void createMember(Member member);

    /**
     * Permanent delete a member from the database
     *
     * @param playerId The UUID of the member
     */

    void deleteMember(UUID uniqueId);
}
