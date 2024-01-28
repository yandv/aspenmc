package br.com.aspenmc.command;

/*
 * Forked from https://github.com/mcardy/CommandFramework
 *
 */

import br.com.aspenmc.entity.sender.Sender;
import br.com.aspenmc.entity.sender.member.Member;

public class CommandArgs {

    private final Sender sender;
    private final String label;
    private final String[] args;

    public CommandArgs(Sender sender, String label, String[] args, int subCommand) {
        String[] modArgs = new String[args.length - subCommand];
        System.arraycopy(args, subCommand, modArgs, 0, args.length - subCommand);

        StringBuilder buffer = new StringBuilder();
        buffer.append(label);

        for (int x = 0; x < subCommand; x++) {
            buffer.append(".").append(args[x]);
        }

        String cmdLabel = buffer.toString();
        this.sender = sender;
        this.label = cmdLabel;
        this.args = modArgs;
    }

    public Member getSenderAsMember() {
        return (Member) sender;
    }

    public <T extends Member> T getSenderAsMember(Class<T> t) {
        return t.cast(sender);
    }

    public Sender getSender() {
        return sender;
    }

    public String getLabel() {
        return label;
    }

    public String[] getArgs() {
        return args;
    }

    public boolean isPlayer() {
        return sender.isPlayer();
    }
}
