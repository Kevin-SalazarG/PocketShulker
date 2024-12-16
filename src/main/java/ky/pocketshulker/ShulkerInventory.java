package ky.pocketshulker;

import cn.nukkit.Player;
import cn.nukkit.inventory.fake.FakeInventory;
import cn.nukkit.item.Item;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.nbt.tag.StringTag;
import cn.nukkit.item.enchantment.Enchantment;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class ShulkerInventory {
    private final Item shulker;

    public ShulkerInventory(Item item) {
        this.shulker = item;
    }

    public Map<Integer, Item> getContents() {
        Map<Integer, Item> contents = new HashMap<>();

        CompoundTag tag = shulker.getNamedTag();
        if (tag == null || !tag.contains("Items")) {
            return contents;
        }

        ListTag<CompoundTag> itemsTag = tag.getList("Items", CompoundTag.class);

        for (int i = 0; i < itemsTag.size(); i++) {
            CompoundTag itemTag = itemsTag.get(i);

            String id = itemTag.getString("Name");
            int meta = itemTag.getShort("Damage");
            int count = itemTag.getByte("Count");
            int slot = itemTag.getByte("Slot");

            CompoundTag tagCompound = itemTag.getCompound("tag");

            Item inventoryItem;
            if (tagCompound != null) {
                ListTag<CompoundTag> enchantmentsTag = tagCompound.getList("ench", CompoundTag.class);
                Map<Enchantment, Integer> enchantments = new HashMap<>();
                for (CompoundTag enchantmentTag : enchantmentsTag.getAll()) {
                    int lvl = enchantmentTag.getShort("lvl");
                    int enchantmentId = enchantmentTag.getShort("id");
                    Enchantment enchantment = Enchantment.get(enchantmentId);
                    enchantments.put(enchantment, lvl);
                }

                String customName = "";
                List<String> lore = new ArrayList<>();

                CompoundTag displayTag = tagCompound.getCompound("display");
                if (displayTag != null) {
                    if (displayTag.contains("Name")) {
                        customName = displayTag.getString("Name");
                    }

                    if (displayTag.contains("Lore")) {
                        ListTag<StringTag> loreTag = displayTag.getList("Lore", StringTag.class);
                        for (StringTag loreStringTag : loreTag.getAll()) {
                            lore.add(loreStringTag.data);
                        }
                    }
                }

                inventoryItem = Item.get(id, meta, count);
                inventoryItem.setCustomName(customName.isEmpty() ? inventoryItem.getName() : customName);

                for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                    inventoryItem.addEnchantment(entry.getKey().setLevel(entry.getValue(), false));
                }
                inventoryItem.setLore(lore.toArray(new String[0]));

                CompoundTag newTagCompound = inventoryItem.getNamedTag();
                if (tagCompound.contains("customColor")) {
                    newTagCompound.putInt("customColor", tagCompound.getInt("customColor"));
                }
                inventoryItem.setNamedTag(newTagCompound);
            } else {
                inventoryItem = Item.get(id, meta, count);
            }

            contents.put(slot, inventoryItem);
        }

        return contents;
    }

    public void setContents(Map<Integer, Item> contents) {
        CompoundTag tag = shulker.hasCompoundTag() ? shulker.getNamedTag() : new CompoundTag();
        ListTag<CompoundTag> itemsTag = new ListTag<>();

        for (Map.Entry<Integer, Item> entry : contents.entrySet()) {
            int slot = entry.getKey();
            Item item = entry.getValue();

            CompoundTag itemTag = new CompoundTag()
                    .putString("Name", item.getId())
                    .putShort("Damage", (short) item.getDamage())
                    .putByte("Count", (byte) item.getCount())
                    .putByte("Slot", (byte) slot);

            CompoundTag tagCompound = new CompoundTag();

            if (item.hasEnchantments()) {
                ListTag<CompoundTag> enchantmentsTag = new ListTag<>();
                for (Enchantment enchantment : item.getEnchantments()) {
                    enchantmentsTag.add(new CompoundTag()
                            .putShort("id", (short) enchantment.getId())
                            .putShort("lvl", (short) enchantment.getLevel()));
                }
                tagCompound.putList("ench", enchantmentsTag);
            }

            if (item.hasCustomName() || item.getLore().length > 0) {
                CompoundTag displayTag = new CompoundTag();
                if (item.hasCustomName()) {
                    displayTag.putString("Name", item.getCustomName());
                }
                if (item.getLore().length > 0) {
                    ListTag<StringTag> loreTag = new ListTag<>();
                    for (String loreLine : item.getLore()) {
                        loreTag.add(new StringTag(loreLine));
                    }
                    displayTag.putList("Lore", loreTag);
                }
                tagCompound.putCompound("display", displayTag);
            }

            CompoundTag itemNamedTag = item.getNamedTag();
            if (itemNamedTag != null && itemNamedTag.contains("customColor")) {
                tagCompound.putInt("customColor", itemNamedTag.getInt("customColor"));
            }

            if (!tagCompound.isEmpty()) {
                itemTag.putCompound("tag", tagCompound);
            }

            itemsTag.add(itemTag);
        }

        tag.putList("Items", itemsTag);
        shulker.setNamedTag(tag);
    }

    public Item onOpen(Player player) {
        CompoundTag tag = shulker.hasCompoundTag() ? shulker.getNamedTag() : new CompoundTag();
        CompoundTag shulkerInfo = new CompoundTag()
                .putString("ID", UUID.randomUUID().toString())
                .putBoolean("isOpen", true)
                .putString("UUID", player.getUniqueId().toString());
        tag.putCompound("ShulkerInfo", shulkerInfo);
        shulker.setNamedTag(tag);
        return shulker;
    }

    public void onClose(Player player, FakeInventory inventory) {
        CompoundTag tag = shulker.getNamedTag();
        if (tag != null && tag.contains("ShulkerInfo")) {
            for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
                Item item = player.getInventory().getItem(slot);
                if (item.equalsExact(shulker)) {
                    tag.remove("ShulkerInfo");
                    shulker.setNamedTag(tag);
                    this.setContents(inventory.getContents());
                    player.getInventory().setItem(slot, shulker);
                    break;
                }
            }
        }
    }
}
