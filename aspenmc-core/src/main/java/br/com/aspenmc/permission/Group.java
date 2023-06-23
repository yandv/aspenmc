package br.com.aspenmc.permission;

import lombok.AllArgsConstructor;
import lombok.Getter;
import br.com.aspenmc.CommonPlugin;

import java.util.List;
import java.util.Optional;

@Getter
@AllArgsConstructor
public class Group {

    private final int id;

    private final String groupName;

    private List<String> permissions;

    private String tag;

    private boolean defaultGroup;

    public void setTag(Tag tag) {
        if (tag == null) {
            this.tag = null;
        } else {
            this.tag = tag.getTagName().toLowerCase();
        }

        CommonPlugin.getInstance().getPermissionData().updateGroup(this, "tag");
    }

    public void setDefaultGroup(boolean defaultGroup) {
        this.defaultGroup = defaultGroup;
        CommonPlugin.getInstance().getPermissionData().updateGroup(this, "defaultGroup");
    }

    public boolean hasTag() {
        return tag != null && !tag.isEmpty();
    }

    public Optional<Tag> getGroupTag() {
        if (tag == null || tag.isEmpty()) {
            return Optional.empty();
        }

        return CommonPlugin.getInstance().getPermissionManager().getTagByName(tag);
    }
}
