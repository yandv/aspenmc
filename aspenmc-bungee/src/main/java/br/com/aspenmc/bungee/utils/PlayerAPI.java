package br.com.aspenmc.bungee.utils;

import br.com.aspenmc.entity.sender.member.Skin;
import lombok.NonNull;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;

import java.lang.reflect.Field;
import java.util.UUID;

public class PlayerAPI {

//    public static void changePlayerName(PendingConnection connection, String newName) throws NoSuchFieldException,
//    IllegalAccessException {
//        InitialHandler initialHandler = (InitialHandler) connection;
//
//        Field field = InitialHandler.class.getDeclaredField("name");
//        field.setAccessible(true);
//        field.set(initialHandler, newName);
//    }

    public static void changePlayerId(PendingConnection connection, UUID uniqueId)
            throws NoSuchFieldException, IllegalAccessException {
        InitialHandler initialHandler = (InitialHandler) connection;

        Field field = InitialHandler.class.getDeclaredField("uniqueId");
        field.setAccessible(true);
        field.set(initialHandler, uniqueId);
    }

    public static void changePlayerMode(PendingConnection connection, boolean newOnlineMode)
            throws NoSuchFieldException, IllegalAccessException {
        InitialHandler initialHandler = (InitialHandler) connection;

        Field field = InitialHandler.class.getDeclaredField("onlineMode");
        field.setAccessible(true);
        field.set(initialHandler, newOnlineMode);
    }

    public static void changePlayerSkin(PendingConnection connection, @NonNull Skin skin)
            throws NoSuchFieldException, IllegalAccessException {
        InitialHandler initialHandler = (InitialHandler) connection;
        LoginResult loginProfile = initialHandler.getLoginProfile();

        LoginResult.Property property = new LoginResult.Property("textures", skin.getValue(), skin.getSignature());

        if (loginProfile == null) {
            LoginResult loginResult = new LoginResult(connection.getUniqueId().toString().replace("-", ""),
                    connection.getName(), new LoginResult.Property[] { property });

            Class<?> initialHandlerClass = connection.getClass();
            Field profileField = initialHandlerClass.getDeclaredField("loginProfile");
            profileField.setAccessible(true);
            profileField.set(connection, loginResult);
        } else {
            loginProfile.setProperties(new LoginResult.Property[] { property });
        }
    }

    public static Skin getPlayerSkin(PendingConnection pendingConnection, Skin orElse) {
        InitialHandler initialHandler = (InitialHandler) pendingConnection;
        LoginResult loginProfile = initialHandler.getLoginProfile();

        if (loginProfile == null) {
            return orElse;
        }

        LoginResult.Property[] properties = loginProfile.getProperties();

        if (properties == null) {
            return orElse;
        }

        for (LoginResult.Property property : properties) {
            if (property.getName().equals("textures")) {
                return new Skin(pendingConnection.getName(), pendingConnection.getUniqueId(), property.getValue(),
                        property.getSignature());
            }
        }

        return orElse;
    }
}
