package br.com.aspenmc.backend.data;

import br.com.aspenmc.entity.Member;
import br.com.aspenmc.entity.member.MemberVoid;
import org.bson.conversions.Bson;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface MemberData {

    /**
     * Loads a member by their name
     *
     * @param playerId The name of the member
     * @param clazz    The class of the member instance
     * @param <T>      The type of the member instance
     * @return CompletableFuture of the member instance
     */

    <T extends Member> CompletableFuture<T> getMemberById(UUID playerId, Class<T> clazz);

    /**
     * Loads a member by their name
     *
     * @param playerId The name of the member
     * @return CompletableFuture of the member as MemberVoid
     */

    default CompletableFuture<MemberVoid> getMemberById(UUID playerId) {
        return getMemberById(playerId, MemberVoid.class);
    }

    /**
     * Load a member by their name
     *
     * @param playerName The name of the member
     * @param clazz      The class of the member instance
     * @param ignoreCase Whether or not to ignore case
     * @param <T>        The type of the member instance
     * @return CompletableFuture of the member instance
     */

    <T extends Member> CompletableFuture<T> getMemberByName(String playerName, Class<T> clazz, boolean ignoreCase);

    /**
     * Load a member by their name
     *
     * @param playerName The name of the member
     * @param ignoreCase Whether or not to ignore case
     * @return CompletableFuture of the member as MemberVoid
     */

    default CompletableFuture<MemberVoid> getMemberByName(String playerName, boolean ignoreCase) {
        return getMemberByName(playerName, MemberVoid.class, ignoreCase);
    }

    /**
     * Load members by a Bson filter
     *
     * @param bson  The filter
     * @param clazz The class of the member instance
     * @param <T>   The type of the member instance
     * @return CompletableFuture of the member instance
     */

    <T extends Member> CompletableFuture<List<T>> getMembers(Bson bson, Class<T> clazz);

    void updateMany(String fieldName, Object value, UUID... ids);

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
     * @param uniqueId The UUID of the member
     */

    void deleteMember(UUID uniqueId);
}
