package br.com.aspenmc.bukkit.entity;

import br.com.aspenmc.CommonConst;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.entity.sender.Sender;
import br.com.aspenmc.language.Language;
import br.com.aspenmc.permission.Group;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class BukkitConsoleSender implements Sender {

    private final CommandSender sender = Bukkit.getConsoleSender();

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
        Bukkit.dispatchCommand(sender, command);
    }

    @Override
    public void sendMessage(String... messages) {
        for (String message : messages) {
            sender.sendMessage(message);
        }
    }

    @Override
    public void sendMessage(TextComponent... messages) {
        for (TextComponent message : messages) {
            sender.sendMessage(message.toLegacyText());
        }
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
