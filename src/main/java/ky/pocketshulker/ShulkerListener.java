package ky.pocketshulker;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.inventory.InventoryCloseEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.inventory.fake.FakeInventory;
import cn.nukkit.inventory.fake.FakeInventoryType;
import cn.nukkit.item.Item;

import java.util.Map;

public class ShulkerListener implements Listener {

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = event.getPlayer();
        Inventory inventory = event.getInventory();

        if (inventory instanceof FakeInventory) {
            ShulkerInventory shulkerInventory = Main.getInstance().getShulkerInventory(player);
            if (shulkerInventory != null) {
                shulkerInventory.onClose(player, (FakeInventory) inventory);
                Main.getInstance().removeShulkerInventory(player);
            }
        }
    }

    @EventHandler()
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Item item = event.getItem();
        PlayerInteractEvent.Action action = event.getAction();

        if (action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR || action == PlayerInteractEvent.Action.LEFT_CLICK_AIR) {
            if (item.getId().contains("shulker")) {
                ShulkerInventory shulkerInventory = new ShulkerInventory(item);
                Main.getInstance().setShulkerInventory(player, shulkerInventory);
                String shulkerTitle = item.hasCustomName() ? item.getCustomName() : "Shulker Box";
                FakeInventory inventory = new FakeInventory(FakeInventoryType.SHULKER_BOX, shulkerTitle);

                Map<Integer, Item> contents = shulkerInventory.getContents();
                for (Map.Entry<Integer, Item> entry : contents.entrySet()) {
                    inventory.setItem(entry.getKey(), entry.getValue());
                }

                Item updatedShulker = shulkerInventory.onOpen(player);
                int slot = player.getInventory().getHeldItemIndex();
                player.getInventory().setItem(slot, updatedShulker);

                player.addWindow(inventory);
                return;
            }
        }
    }
}
