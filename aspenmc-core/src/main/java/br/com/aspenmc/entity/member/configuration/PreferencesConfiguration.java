package br.com.aspenmc.entity.member.configuration;

import br.com.aspenmc.clan.ClanTag;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import br.com.aspenmc.entity.Member;

@NoArgsConstructor
@Getter
public class PreferencesConfiguration {

    private boolean staffChatEnabled = true;
    private boolean seeingStaffChatEnabled = true;

    private boolean adminRemoveItems = true;
    private short adminOnLogin = 0;
    private boolean adminModeEnabled = false;
    private boolean spectatorsEnabled = true;

    private boolean tellEnabled = true;
    private boolean chatEnabled = true;

    private boolean clanInvitesEnabled = true;

    private boolean clanDisplayTagEnabled;
    private ClanTag clanTag;

    @Getter(AccessLevel.NONE)
    private transient Member member;

    public ClanTag getClanTag() {
        if (clanTag == null) {
            return ClanTag.NONE;
        }

        return clanTag;
    }

    public void setClanDisplayTagEnabled(boolean clanDisplayTagEnabled) {
        if (this.clanDisplayTagEnabled == clanDisplayTagEnabled) return;

        this.clanDisplayTagEnabled = clanDisplayTagEnabled;
        save();
    }

    public void setClanTag(ClanTag clanTag) {
        if (this.clanTag == clanTag) return;

        this.clanTag = clanTag;
        save();
    }

    public void setAdminOnLogin(short adminOnLogin) {
        if (this.adminOnLogin == adminOnLogin) return;

        this.adminOnLogin = adminOnLogin;
        save();
    }

    public void setAdminModeEnabled(boolean adminModeEnabled) {
        if (this.adminModeEnabled == adminModeEnabled) return;

        this.adminModeEnabled = adminModeEnabled;
        save();
    }

    public void setAdminRemoveItems(boolean adminRemoveItems) {
        if (this.adminRemoveItems == adminRemoveItems) return;

        this.adminRemoveItems = adminRemoveItems;
        save();
    }

    public void setSpectatorsEnabled(boolean spectatorsEnabled) {
        if (this.spectatorsEnabled == spectatorsEnabled) return;

        this.spectatorsEnabled = spectatorsEnabled;
        save();
    }

    public void setStaffChatEnabled(boolean staffChatEnabled) {
        if (this.staffChatEnabled == staffChatEnabled) return;

        this.staffChatEnabled = staffChatEnabled;
        save();
    }

    public void setSeeingStaffChatEnabled(boolean seeingStaffChatEnabled) {
        if (this.seeingStaffChatEnabled == seeingStaffChatEnabled) return;

        this.seeingStaffChatEnabled = seeingStaffChatEnabled;
        save();
    }

    public void setTellEnabled(boolean tellEnabled) {
        if (this.tellEnabled == tellEnabled) return;

        this.tellEnabled = tellEnabled;
        save();
    }

    public void setChatEnabled(boolean chatEnabled) {
        if (this.chatEnabled == chatEnabled) return;

        this.chatEnabled = chatEnabled;
        save();
    }

    public void setClanInvitesEnabled(boolean clanInvitesEnabled) {
        if (this.clanInvitesEnabled == clanInvitesEnabled) return;

        this.clanInvitesEnabled = clanInvitesEnabled;
        save();
    }

    public boolean isAdminOnLogin() {
        return adminOnLogin == 0 || (adminOnLogin == 1 && adminModeEnabled);
    }

    private void save() {
        if (member != null) {
            member.save("preferencesConfiguration");
        }
    }

    public void loadConfiguration(Member member) {
        this.member = member;
    }
}
