package br.com.aspenmc.bukkit.utils.item;

import br.com.aspenmc.BukkitConst;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@Getter
public class ActionItemStack {

    private static final Map<String, ActionHandler> ACTION_HANDLER_MAP = new HashMap<>();
    private static final Consumer<NbtCompound> EMPTY_CONSUMER = compound -> {
    };

    private static Constructor<?> NBT_CONSTRUCTOR;

    static {
        try {
            NBT_CONSTRUCTOR = MinecraftReflection.getCraftItemStackClass().getDeclaredConstructor(ItemStack.class);
            NBT_CONSTRUCTOR.setAccessible(true);
        } catch (NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }
    }

    private String id;

    private ItemStack itemStack;
    private ActionHandler actionHandler;

    private boolean placeable;

    public ActionItemStack(ItemStack itemStack, String id, ActionHandler actionHandler, boolean placeable) {
        this.itemStack = itemStack;
        this.id = id;
        this.actionHandler = actionHandler;
        this.placeable = placeable;
        registerActionHandler(id, actionHandler);
    }

    public ActionItemStack(ItemStack itemStack, String id, ActionHandler actionHandler) {
        this(itemStack, id, actionHandler, false);
    }

    public ActionItemStack(ItemStack itemStack, ActionHandler actionHandler) {
        this(itemStack, String.valueOf(ACTION_HANDLER_MAP.size() + 1), actionHandler);
    }

    public boolean isSimilar(ItemStack itemStack) {
        return this.itemStack.isSimilar(itemStack);
    }

    public ItemStack getItemStack() {
        return createItemStack(id, itemStack, EMPTY_CONSUMER);
    }

    public ItemStack getItemStack(Consumer<NbtCompound> consumer) {
        return createItemStack(id, itemStack, consumer);
    }

    public static void registerItemStack(ActionItemStack actionItemStack) {
        registerActionHandler(actionItemStack.getId(), actionItemStack.getActionHandler());
    }

    public static ItemStack createItemStack(String id, ItemStack itemStack, Consumer<NbtCompound> consumer) {
        try {
            ItemStack newStack = (ItemStack) NBT_CONSTRUCTOR.newInstance(itemStack);
            NbtCompound compound = (NbtCompound) NbtFactory.fromItemTag(newStack);

            compound.put(BukkitConst.HANDLER_FIELD, id);
            consumer.accept(compound);

            return newStack;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static NbtCompound getNbtCompound(ItemStack itemStack) {
        try {
            return (NbtCompound) NbtFactory.fromItemTag((ItemStack) NBT_CONSTRUCTOR.newInstance(itemStack));
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException |
                 InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void registerActionHandler(String id, ActionHandler actionHandler) {
        if (ACTION_HANDLER_MAP.containsKey(id)) {
            throw new IllegalArgumentException("ActionHandler with id '" + id + "' already registered!" +
                                               " Please use another id or unregister the old one.");
        }

        ACTION_HANDLER_MAP.put(id, actionHandler);
    }

    public static boolean containsActionHandler(String id) {
        return ACTION_HANDLER_MAP.containsKey(id);
    }

    public static boolean containsActionHandler(ItemStack itemStack) {
        return getActionHandlerByStack(itemStack).isPresent();
    }

    public static Optional<ActionHandler> getActionHandlerByStack(ItemStack itemStack) {
        try {
            ItemStack newStack = (ItemStack) NBT_CONSTRUCTOR.newInstance(itemStack);
            NbtCompound compound = (NbtCompound) NbtFactory.fromItemTag(newStack);

            if (compound.containsKey(BukkitConst.HANDLER_FIELD)) {
                return Optional.ofNullable(ACTION_HANDLER_MAP.get(compound.getString(BukkitConst.HANDLER_FIELD)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public static String getIdByStack(ItemStack itemStack) {
        NbtCompound compound = getNbtCompound(itemStack);

        if (compound == null || !compound.containsKey(BukkitConst.HANDLER_FIELD)) {
            return null;
        }

        return compound.getString(BukkitConst.HANDLER_FIELD);
    }

    public interface ActionHandler {

        boolean onClick(ActionArgs args);
    }

    @AllArgsConstructor
    @Getter
    public static class ActionArgs {

        private Player player;
        private ItemStack itemStack;
        private Entity entity;
        private Block block;

        private ActionType actionType;

        private NbtCompound compound;

        public boolean isBlockClick() {
            return block != null;
        }
    }

    public enum ActionType {

        RIGHT_CLICK,
        LEFT_CLICK;
    }
}
