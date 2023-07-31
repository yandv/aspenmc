package br.com.aspenmc.entity.member.configuration;

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
    private boolean adminOnLogin = true;
    private boolean spectatorsEnabled = true;

    private boolean tellEnabled = true;
    private boolean chatEnabled = true;

    @Getter(AccessLevel.NONE)
    private transient Member member;

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

    private void save() {
        if (member != null) {
            member.save("preferencesConfiguration");
        }
    }

    public void loadConfiguration(Member member) {
        this.member = member;
    }

}
