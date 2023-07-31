package br.com.aspenmc.bukkit.listener;

import br.com.aspenmc.BukkitConst;
import br.com.aspenmc.bukkit.BukkitCommon;
import br.com.aspenmc.bukkit.utils.item.ActionItemStack;
import br.com.aspenmc.bukkit.utils.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Optional;

public class ActionItemListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL) return;

        if (event.getItem() == null || event.getItem().getType() == Material.AIR) {
            return;
        }

        ItemStack stack = event.getItem();
        Optional<ActionItemStack.ActionHandler> optional = ActionItemStack.getActionHandlerByStack(stack);

        if (!optional.isPresent()) {
            return;
        }

        ActionItemStack.ActionHandler handler = optional.get();

        event.setCancelled(!handler.onClick(
                new ActionItemStack.ActionArgs(event.getPlayer(), event.getItem(), null, event.getClickedBlock(),
                                               event.getAction().name().contains("RIGHT") ?
                                               ActionItemStack.ActionType.RIGHT_CLICK :
                                               ActionItemStack.ActionType.LEFT_CLICK,
                                               ActionItemStack.getNbtCompound(stack))));
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();

        if (player.getItemInHand() == null || player.getItemInHand().getType() == Material.AIR) return;

        ItemStack stack = player.getItemInHand();
        Optional<ActionItemStack.ActionHandler> optional = ActionItemStack.getActionHandlerByStack(stack);

        if (!optional.isPresent()) return;

        ActionItemStack.ActionHandler handler = optional.get();

        event.setCancelled(!handler.onClick(
                new ActionItemStack.ActionArgs(event.getPlayer(), event.getPlayer().getItemInHand(), event.getRightClicked(), null,
                                               ActionItemStack.ActionType.RIGHT_CLICK,
                                               ActionItemStack.getNbtCompound(stack))));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getItemInHand() == null || event.getItemInHand().getType() == Material.AIR) {
            return;
        }

        ItemStack stack = event.getItemInHand();

        try {
            if (ActionItemStack.containsActionHandler(stack)) {
                Block b = event.getBlockPlaced();
                String id = ActionItemStack.getIdByStack(stack);

                b.setMetadata(BukkitConst.HANDLER_FIELD,
                              new FixedMetadataValue(BukkitCommon.getInstance(), id));
                b.getDrops().clear();
                b.getDrops().add(ActionItemStack.createItemStack(id, new ItemBuilder().type(event.getBlock().getType())
                                                                                      .durability(event.getBlock()
                                                                                                       .getData())
                                                                                      .build(), consumer -> {}));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block b = event.getBlock();

        if (!b.hasMetadata(BukkitConst.HANDLER_FIELD)) {
            return;
        }

        b.getDrops().clear();
        b.getDrops().add(ActionItemStack.createItemStack(b.getMetadata(BukkitConst.HANDLER_FIELD).get(0).asString(),
                                                         new ItemStack(event.getBlock().getType(), 1,
                                                                       event.getBlock().getData()), consumer -> {}));
    }
}