package br.com.aspenmc.backend.data;

import br.com.aspenmc.permission.Group;
import br.com.aspenmc.permission.Tag;

import java.util.List;
import java.util.Optional;

public interface PermissionData {

    /**
     * Creates a group in the database
     *
     * @param group The group to create
     */

    void createGroup(Group group);

    /**
     * Creates a tag in the database
     *
     * @param tag The tag to create
     */

    void createTag(Tag tag);

    /**
     * Retrieves a group from the database
     *
     * @param groupName The name of the group to retrieve
     * @return The group if it exists
     */

    Optional<Group> retrieveGroupByName(String groupName);

    /**
     * Retrieves a tag from the database
     *
     * @param tagName The name of the tag to retrieve
     * @return The tag if it exists
     */

    Optional<Tag> retrieveTagByName(String tagName);

    /**
     * Retrieves a group from the database
     *
     * @param groupId The id of the group to retrieve
     * @return The group if it exists
     */

    Optional<Group> retrieveGroupById(int groupId);

    /**
     * Retrieves a tag from the database
     *
     * @param tagId The id of the tag to retrieve
     * @return The tag if it exists
     */

    Optional<Tag> retrieveTagById(int tagId);

    /**
     * Retrieves all groups from the database
     *
     * @return All groups
     */

    List<Group> retrieveAllGroups();

    /**
     * Retrieves all tags from the database
     *
     * @return All tags
     */

    List<Tag> retrieveAllTags();

    /**
     * Deletes a group from the database
     *
     * @param group The group to delete
     */

    void deleteGroup(Group group);

    /**
     * Deletes a tag from the database
     *
     * @param tag The tag to delete
     */

    void deleteTag(Tag tag);

    /**
     * Updates a group in the database
     *
     * @param group  The group to update
     * @param fields The fields to update
     */

    void updateGroup(Group group, String... fields);

    /**
     * Updates a tag in the database
     *
     * @param tag    The tag to update
     * @param fields The fields to update
     */

    void updateTag(Tag tag, String... fields);
}
