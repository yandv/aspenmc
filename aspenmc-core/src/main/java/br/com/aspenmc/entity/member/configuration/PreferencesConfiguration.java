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

    private boolean adminRemoveItems;
    private boolean adminOnLogin;
    private boolean spectatorsEnabled;

    private boolean tellEnabled;

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

    private void save() {
        if (member != null) {
            member.save("preferencesConfiguration");
        }
    }

    public void loadConfiguration(Member member) {
        this.member = member;
    }

}
