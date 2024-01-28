package br.com.aspenmc.entity.sender.member.configuration;

import br.com.aspenmc.clan.ClanTag;
import br.com.aspenmc.entity.sender.member.Member;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;

@NoArgsConstructor
@Getter
public class PreferencesConfiguration {

    private boolean tellEnabled = true;
    private boolean chatEnabled = true;

    private boolean clanInvitesEnabled = true;

    private boolean clanDisplayTagEnabled;
    private ClanTag clanTag;

    /*
     * Staff
     */

    private boolean staffChatEnabled = true;
    private boolean seeingStaffChatEnabled = true;
    private boolean seeingReportsEnabled = true;
    private boolean seeingStafflogsEnabled = true;

    private boolean adminRemoveItems = true;
    private short adminOnLogin = 0;
    private boolean adminModeEnabled = false;
    private boolean spectatorsEnabled = false;

    private boolean alertsEnabled;

    @Getter(AccessLevel.NONE)
    private transient Member member;

    public ClanTag getClanTag() {
        if (clanTag == null) {
            return ClanTag.NONE;
        }

        return clanTag;
    }

    public void setSeeingReportsEnabled(boolean seeingReportsEnabled) {
        if (this.seeingReportsEnabled == seeingReportsEnabled) return;

        this.seeingReportsEnabled = seeingReportsEnabled;
        save("seeingReportsEnabled");
    }

    public void setSeeingStafflogsEnabled(boolean seeingStafflogsEnabled) {
        if (this.seeingStafflogsEnabled == seeingStafflogsEnabled) return;

        this.seeingStafflogsEnabled = seeingStafflogsEnabled;
        save("seeingStaffLogs");
    }

    public void setClanDisplayTagEnabled(boolean clanDisplayTagEnabled) {
        if (this.clanDisplayTagEnabled == clanDisplayTagEnabled) return;

        this.clanDisplayTagEnabled = clanDisplayTagEnabled;
        save("clanDisplayTagEnabled");
    }

    public void setClanTag(ClanTag clanTag) {
        if (this.clanTag == clanTag) return;

        this.clanTag = clanTag;
        save("clanTag");
    }

    public void setAlertsEnabled(boolean alertsEnabled) {
        this.alertsEnabled = alertsEnabled;
        save("alertsEnabled");
    }

    public void setAdminOnLogin(short adminOnLogin) {
        if (this.adminOnLogin == adminOnLogin) return;

        this.adminOnLogin = adminOnLogin;
        save("adminOnLogin");
    }

    public void setAdminModeEnabled(boolean adminModeEnabled) {
        if (this.adminModeEnabled == adminModeEnabled) return;

        this.adminModeEnabled = adminModeEnabled;
        save("adminModeEnabled");
    }

    public void setAdminRemoveItems(boolean adminRemoveItems) {
        if (this.adminRemoveItems == adminRemoveItems) return;

        this.adminRemoveItems = adminRemoveItems;
        save("adminRemoveItems");
    }

    public void setSpectatorsEnabled(boolean spectatorsEnabled) {
        if (this.spectatorsEnabled == spectatorsEnabled) return;

        this.spectatorsEnabled = spectatorsEnabled;
        save("spectatorsEnabled");
    }

    public void setStaffChatEnabled(boolean staffChatEnabled) {
        if (this.staffChatEnabled == staffChatEnabled) return;

        this.staffChatEnabled = staffChatEnabled;
        save("staffChatEnabled");
    }

    public void setSeeingStaffChatEnabled(boolean seeingStaffChatEnabled) {
        if (this.seeingStaffChatEnabled == seeingStaffChatEnabled) return;

        this.seeingStaffChatEnabled = seeingStaffChatEnabled;
        save("seeingStaffChatEnabled");
    }

    public void setTellEnabled(boolean tellEnabled) {
        if (this.tellEnabled == tellEnabled) return;

        this.tellEnabled = tellEnabled;
        save("tellEnabled");
    }

    public void setChatEnabled(boolean chatEnabled) {
        if (this.chatEnabled == chatEnabled) return;

        this.chatEnabled = chatEnabled;
        save("chatEnabled");
    }

    public void setClanInvitesEnabled(boolean clanInvitesEnabled) {
        if (this.clanInvitesEnabled == clanInvitesEnabled) return;

        this.clanInvitesEnabled = clanInvitesEnabled;
        save("clanInvitesEnabled");
    }

    public boolean isAdminOnLogin() {
        return adminOnLogin == 0 || (adminOnLogin == 1 && adminModeEnabled);
    }

    private void save(String... fields) {
        if (member != null) {
            member.save(Arrays.stream(fields).map(field -> "preferencesConfiguration." + field).toArray(String[]::new));
        }
    }

    public void loadConfiguration(Member member) {
        this.member = member;
    }
}
