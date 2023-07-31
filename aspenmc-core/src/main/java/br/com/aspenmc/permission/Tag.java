package br.com.aspenmc.permission;

import lombok.AllArgsConstructor;
import lombok.Getter;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.utils.string.StringFormat;
import net.md_5.bungee.api.ChatColor;

import java.util.Arrays;
import java.util.Set;

@AllArgsConstructor
@Getter
public class Tag {

    private final int id;

    private String tagName;

    private String tagPrefix;

    private Set<String> aliases;

    private boolean translateable;

    public Tag(int id, String tagName, String tagPrefix, Set<String> aliases) {
        this(id, tagName, tagPrefix, aliases, false);
    }

    public void addAliases(String... aliases) {
        this.aliases.addAll(Arrays.asList(aliases));
        CommonPlugin.getInstance().getPermissionData().updateTag(this, "aliases");
    }

    public void removeAliases(String... aliases) {
        Arrays.asList(aliases).forEach(this.aliases::remove);
        CommonPlugin.getInstance().getPermissionData().updateTag(this, "aliases");
    }

    public void setTagPrefix(String tagPrefix) {
        this.tagPrefix = tagPrefix;
        CommonPlugin.getInstance().getPermissionData().updateTag(this, "tagPrefix");
    }

    public String getReductionName() {
        String color = getColor();

        if (color.length() == 0) {
            color = "§7";
        }

        return color + "[" + tagName.charAt(0) + "]";
    }

    public String getRealPrefix() {
        return ChatColor.stripColor(tagPrefix).length() > 0 ? tagPrefix + " " : tagPrefix;
    }

    public String getPrefixOrName() {
        return ChatColor.stripColor(tagPrefix).length() > 4 ? tagPrefix : tagPrefix + tagName;
    }

    public String getColoredName() {
        return StringFormat.getLastColors(tagPrefix) + tagName;
    }

    public String getColor() {
        return StringFormat.getLastColors(tagPrefix);
    }
}
