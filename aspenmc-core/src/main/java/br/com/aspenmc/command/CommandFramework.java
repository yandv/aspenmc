package br.com.aspenmc.command;


import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.utils.ClassGetter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
 * Forked from https://github.com/mcardy/CommandFramework
 *
 */

public interface CommandFramework {

    Class<?> getJarClass();

    void registerCommands(CommandHandler commandHandler);

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Command {

        String name();

        String permission() default "";

        String[] aliases() default {};

        String description() default "";

        String usage() default "";

        boolean runAsync() default false;

        boolean console() default true;
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Completer {

        String name();

        String[] aliases() default {};
    }

    default CommandFramework loadCommands(String packageName) {
        for (Class<?> commandClass : ClassGetter.getClassesForPackage(getJarClass(), packageName))
            if (CommandHandler.class != commandClass) {
                if (CommandHandler.class.isAssignableFrom(commandClass)) {
                    try {
                        registerCommands((CommandHandler) commandClass.newInstance());
                    } catch (Exception ex) {
                        CommonPlugin.getInstance().getLogger()
                                    .warning("Error when loading command from " + commandClass.getSimpleName() + "!");
                        ex.printStackTrace();
                    }
                }
            }

        return this;
    }

    default CommandFramework loadCommands(Class<?> jarClass, String packageName) {
        for (Class<?> commandClass : ClassGetter.getClassesForPackage(jarClass, packageName))
            if (CommandHandler.class.isAssignableFrom(commandClass)) {
                try {
                    registerCommands((CommandHandler) commandClass.newInstance());
                } catch (Exception e) {
                    CommonPlugin.getInstance().getLogger()
                                .warning("Error when loading command from " + commandClass.getSimpleName() + "!");
                    e.printStackTrace();
                }
            }

        return this;
    }
}
