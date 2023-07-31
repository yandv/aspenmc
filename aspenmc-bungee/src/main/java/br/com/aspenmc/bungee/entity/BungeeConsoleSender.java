package br.com.aspenmc.bungee.entity;

import br.com.aspenmc.bungee.BungeeMain;
import br.com.aspenmc.CommonConst;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.entity.Sender;
import br.com.aspenmc.language.Language;
import br.com.aspenmc.permission.Group;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.UUID;

public class BungeeConsoleSender implements Sender {

    private final CommandSender sender = BungeeMain.getInstance().getProxy().getConsole();

    @Override
    public String getName() {
        return "CONSOLE";
    }

    @Override
    public String getRealName() {
        return "CONSOLE";
    }

    @Override
    public UUID getUniqueId() {
        return CommonConst.CONSOLE_ID;
    }

    @Override
    public void performCommand(String command) {
        ProxyServer.getInstance().getPluginManager().dispatchCommand(sender, command);
    }

    @Override
    public void sendMessage(String... messages) {
        for (String message : messages) {
            sender.sendMessage(message);
        }
    }

    @Override
    public void sendMessage(TextComponent... messages) {
        sender.sendMessage(messages);
    }

    @Override
    public boolean isPlayer() {
        return false;
    }

    @Override
    public Language getLanguage() {
        return CommonPlugin.getInstance().getDefaultLanguage();
    }

    @Override
    public boolean hasPermission(String permission) {
        return true;
    }

    @Override
    public Group getServerGroup() {
        return CommonPlugin.getInstance().getPermissionManager().getHighGroup();
    }
}
