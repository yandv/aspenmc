package br.com.aspenmc.manager;

import lombok.Getter;
import br.com.aspenmc.permission.Group;
import br.com.aspenmc.permission.Tag;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class PermissionManager {

    public static final Tag NULL_TAG = new Tag(0, "Membro", "§7");

    private final Map<String, Group> groupMap;
    private final Map<String, Tag> tagMap;

    public PermissionManager() {
        groupMap = new HashMap<>();
        tagMap = new HashMap<>();
    }

    public Optional<Tag> getDefaultTag() {
        return tagMap.values().stream().min(Comparator.comparingInt(Tag::getId));
    }

    public List<Group> getDefaultGroups() {
        return groupMap.values().stream().filter(Group::isDefaultGroup).collect(Collectors.toList());
    }

    public Optional<Tag> getTagById(int id) {
        return tagMap.values().stream().filter(tag -> tag.getId() == id).findFirst();
    }

    public Optional<Tag> getTagByName(String tagName) {
        return Optional.ofNullable(tagMap.get(tagName.toLowerCase()));
    }

    public Optional<Group> getGroupByName(String groupName) {
        return Optional.ofNullable(groupMap.get(groupName.toLowerCase()));
    }

    public Group getFirstLowerGroup(int id) {
        return groupMap.values().stream().filter(group -> group.getId() < id).findFirst()
                       .orElse(groupMap.values().stream().min((o1, o2) -> o1.getId() - o2.getId()).orElse(null));
    }

    public Group getFirstLowerGroup(Group group) {
        return getFirstLowerGroup(group == null ? 0 : group.getId());
    }

    public Group getHighGroup() {
        return groupMap.values().stream().max((o1, o2) -> o1.getId() - o2.getId()).orElse(null);
    }

    public Optional<Group> getGroupById(int groupId) {
        return groupMap.values().stream().filter(group -> group.getId() == groupId).findFirst();
    }

    public Collection<Group> getGroups() {
        return groupMap.values();
    }

    public Collection<Tag> getTags() {
        return tagMap.values();
    }

    public void loadGroup(Group group) {
        groupMap.put(group.getGroupName().toLowerCase(), group);
    }

    public void unloadGroup(String groupName) {
        groupMap.remove(groupName.toLowerCase());
    }

    public void loadTag(Tag tag) {
        tagMap.put(tag.getTagName().toLowerCase(), tag);
    }

    public void unloadTag(String tagName) {
        tagMap.remove(tagName.toLowerCase());
    }
}
