package br.com.aspenmc.bukkit.utils.menu;

import br.com.aspenmc.bukkit.utils.item.ItemBuilder;
import br.com.aspenmc.bukkit.utils.menu.click.MenuClickHandler;
import org.bukkit.inventory.ItemStack;

public class MenuItem {

    private ItemStack itemStack;
    private MenuClickHandler handler;

    public MenuItem(ItemStack itemStack, MenuClickHandler handler) {
        this.itemStack = itemStack;
        this.handler = handler;
    }

    public MenuItem(ItemStack itemStack) {
        this(itemStack, MenuClickHandler.EMPTY_HANDLER);
    }

    public MenuItem(ItemBuilder itemBuilder) {
        this(itemBuilder.build());
    }

    public MenuItem(ItemBuilder itemBuilder, MenuClickHandler handler) {
        this(itemBuilder.build(), handler);
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public MenuClickHandler getHandler() {
        return handler;
    }

    public void destroy() {
        itemStack = null;
        handler = null;
    }
}
