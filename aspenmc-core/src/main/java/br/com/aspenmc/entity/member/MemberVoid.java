package br.com.aspenmc.entity.member;

import com.google.common.base.Joiner;
import br.com.aspenmc.entity.Member;
import br.com.aspenmc.entity.member.configuration.LoginConfiguration;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.UUID;

public class MemberVoid extends Member {


    public MemberVoid(UUID uniqueId, String name, LoginConfiguration.AccountType accountType) {
        super(uniqueId, name, accountType);
    }

    @Override
    public void sendServer(String serverId) {

    }

    @Override
    public void performCommand(String command) {

    }

    @Override
    public void sendMessage(String... messages) {
        System.out.println("[MemberVoid - " + getName() + "] " + Joiner.on(' ').join(messages));
    }

    @Override
    public void sendMessage(TextComponent... messages) {

    }
}
