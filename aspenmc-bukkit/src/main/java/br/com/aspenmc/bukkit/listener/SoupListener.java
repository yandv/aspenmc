package br.com.aspenmc.bukkit.listener;

import br.com.aspenmc.bukkit.utils.item.ItemBuilder;
import br.com.aspenmc.bukkit.utils.menu.MenuInventory;
import br.com.aspenmc.bukkit.utils.menu.click.ClickType;
import br.com.aspenmc.bukkit.utils.menu.click.MenuClickHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;

public class SoupListener implements Listener {

    private static final MenuClickHandler GET_ITEM_MENU_CLICK_HANDLER = clickArgs -> {
        clickArgs.setCancelled(false);
    };

    @SuppressWarnings("deprecation")
    public SoupListener() {
        ItemStack soup = new ItemStack(Material.MUSHROOM_SOUP);
        ShapelessRecipe cocoa = new ShapelessRecipe(soup);
        ShapelessRecipe cactus = new ShapelessRecipe(soup);
        ShapelessRecipe pumpkin = new ShapelessRecipe(soup);
        ShapelessRecipe melon = new ShapelessRecipe(soup);
        ShapelessRecipe flower = new ShapelessRecipe(soup);
        ShapelessRecipe nether = new ShapelessRecipe(soup);

        cocoa.addIngredient(Material.BOWL);
        cocoa.addIngredient(Material.INK_SACK, 3);

        cactus.addIngredient(Material.BOWL);
        cactus.addIngredient(Material.CACTUS);

        pumpkin.addIngredient(Material.BOWL);
        pumpkin.addIngredient(1, Material.PUMPKIN_SEEDS);

        melon.addIngredient(Material.BOWL);
        melon.addIngredient(1, Material.MELON_SEEDS);

        nether.addIngredient(Material.BOWL);
        nether.addIngredient(Material.getMaterial(372));

        flower.addIngredient(Material.BOWL);
        flower.addIngredient(Material.RED_ROSE);
        flower.addIngredient(Material.YELLOW_FLOWER);

        Bukkit.addRecipe(cocoa);
        Bukkit.addRecipe(cactus);
        Bukkit.addRecipe(pumpkin);
        Bukkit.addRecipe(melon);
        Bukkit.addRecipe(nether);
        Bukkit.addRecipe(flower);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = player.getItemInHand();

        if (itemStack.getType() == Material.MUSHROOM_SOUP && event.getAction().name().contains("RIGHT") &&
                (player.getHealth() < player.getMaxHealth() || player.getFoodLevel() < 20)) {
            event.setCancelled(true);
            int restores = 7;

            player.setHealth(Math.min(player.getHealth() + restores, player.getMaxHealth()));
            player.setFoodLevel(Math.min(player.getFoodLevel() + restores, 20));
            player.setExhaustion(20);
            player.setSaturation(20);

            player.setItemInHand(new ItemBuilder().type(Material.BOWL).build());
        }
    }

    @EventHandler
    public void onPlayerInteractInv(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.getClickedBlock().getType() != Material.WALL_SIGN &&
                event.getClickedBlock().getType() != Material.SIGN_POST) {
            return;
        }

        Player player = event.getPlayer();

        Sign sign = (Sign) event.getClickedBlock().getState();
        String[] lines = sign.getLines();

        if (lines[1].toLowerCase().contains("sopas")) {
            Inventory soup = Bukkit.createInventory(null, 54, "§7Sopas");

            for (int i = 0; i < 54; ++i) {
                soup.setItem(i, new ItemStack(Material.MUSHROOM_SOUP));
            }

            player.openInventory(soup);
        } else if (lines[1].toLowerCase().contains("recraft")) {
            MenuInventory menuInventory = new MenuInventory("§7Recraft", InventoryType.HOPPER, true);

            menuInventory.setItem(1, new ItemStack(Material.BOWL, 64), GET_ITEM_MENU_CLICK_HANDLER);
            menuInventory.setItem(2, new ItemStack(Material.RED_MUSHROOM, 64), GET_ITEM_MENU_CLICK_HANDLER);
            menuInventory.setItem(3, new ItemStack(Material.BROWN_MUSHROOM, 64), GET_ITEM_MENU_CLICK_HANDLER);

            menuInventory.open(player);
        } else if (lines[1].toLowerCase().contains("cactus")) {
            MenuInventory menuInventory = new MenuInventory("§7Recraft", InventoryType.HOPPER, true);

            menuInventory.setItem(0, new ItemStack(Material.BOWL, 64), GET_ITEM_MENU_CLICK_HANDLER);
            menuInventory.setItem(1, new ItemStack(Material.CACTUS, 64), GET_ITEM_MENU_CLICK_HANDLER);

            menuInventory.setItem(3, new ItemStack(Material.CACTUS, 64), GET_ITEM_MENU_CLICK_HANDLER);
            menuInventory.setItem(4, new ItemStack(Material.BOWL, 64), GET_ITEM_MENU_CLICK_HANDLER);

            menuInventory.open(player);
        } else if (lines[1].toLowerCase().contains("cocoa")) {
            MenuInventory menuInventory = new MenuInventory("§7Recraft", 3, InventoryType.HOPPER, true);

            menuInventory.setItem(0, new ItemStack(Material.BOWL, 64), GET_ITEM_MENU_CLICK_HANDLER);
            menuInventory.setItem(1, new ItemStack(Material.INK_SACK, 64, (short) 3), GET_ITEM_MENU_CLICK_HANDLER);

            menuInventory.setItem(3, new ItemStack(Material.INK_SACK, 64, (short) 3), GET_ITEM_MENU_CLICK_HANDLER);
            menuInventory.setItem(4, new ItemStack(Material.BOWL, 64), GET_ITEM_MENU_CLICK_HANDLER);

            menuInventory.open(player);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSignChange(final SignChangeEvent event) {
        String line = event.getLine(0);

        if (line.equalsIgnoreCase("sopa") || line.equalsIgnoreCase("sopas") || line.equalsIgnoreCase("soup") ||
                line.equalsIgnoreCase("soups")) {
            event.setLine(0, "§4-§6-§e-§a-§b-§9-§5--§9-§b-§a-§e-§6-§4-");
            event.setLine(1, "§bSopas");
            event.setLine(2, " ");
            event.setLine(3, "§4-§6-§e-§a-§b-§9-§5--§9-§b-§a-§e-§6-§4-");
        } else if (line.equalsIgnoreCase("recraft") || line.equalsIgnoreCase("recrafts")) {
            event.setLine(0, "§4-§6-§e-§a-§b-§9-§5--§9-§b-§a-§e-§6-§4-");
            event.setLine(1, "§eRecraft");
            event.setLine(2, " ");
            event.setLine(3, "§4-§6-§e-§a-§b-§9-§5--§9-§b-§a-§e-§6-§4-");
        } else if (line.equalsIgnoreCase("cocoa") || line.equalsIgnoreCase("cocoabean")) {
            event.setLine(0, "§4-§6-§e-§a-§b-§9-§5--§9-§b-§a-§e-§6-§4-");
            event.setLine(1, "§cCocoabean");
            event.setLine(2, " ");
            event.setLine(3, "§4-§6-§e-§a-§b-§9-§5--§9-§b-§a-§e-§6-§4-");
        } else if (line.equalsIgnoreCase("cactu") || line.equalsIgnoreCase("cactus")) {
            event.setLine(0, "§4-§6-§e-§a-§b-§9-§5--§9-§b-§a-§e-§6-§4-");
            event.setLine(1, "§aCactus");
            event.setLine(2, " ");
            event.setLine(3, "§4-§6-§e-§a-§b-§9-§5--§9-§b-§a-§e-§6-§4-");
        }
    }
}
