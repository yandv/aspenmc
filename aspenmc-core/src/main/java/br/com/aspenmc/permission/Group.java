package br.com.aspenmc.permission;

import lombok.AllArgsConstructor;
import lombok.Getter;
import br.com.aspenmc.CommonPlugin;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Getter
@AllArgsConstructor
public class Group {

    private final int id;

    private final String groupName;

    private Set<String> permissions;

    private String tag;
    private boolean staff;

    private boolean defaultGroup;

    private boolean paidGroup;

    public void setTag(Tag tag) {
        if (tag == null) {
            this.tag = null;
        } else {
            this.tag = tag.getTagName().toLowerCase();
        }

        CommonPlugin.getInstance().getPermissionData().updateGroup(this, "tag");
    }

    public void setDefaultGroup(boolean defaultGroup) {
        if (this.defaultGroup == defaultGroup) return;

        this.defaultGroup = defaultGroup;
        CommonPlugin.getInstance().getPermissionData().updateGroup(this, "defaultGroup");
    }

    public void setPaidGroup(boolean paidGroup) {
        if (this.paidGroup == paidGroup) return;

        this.paidGroup = paidGroup;
        CommonPlugin.getInstance().getPermissionData().updateGroup(this, "paidGroup");
    }

    public void setStaff(boolean staff) {
        if (this.staff == staff) return;

        this.staff = staff;
        CommonPlugin.getInstance().getPermissionData().updateGroup(this, "staff");
    }

    public void addPermission(String permission) {
        permissions.add(permission.toLowerCase());
        CommonPlugin.getInstance().getPermissionData().updateGroup(this, "permissions");
    }

    public void removePermission(String permission) {
        permissions.remove(permission.toLowerCase());
        CommonPlugin.getInstance().getPermissionData().updateGroup(this, "permissions");
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission.toLowerCase());
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
