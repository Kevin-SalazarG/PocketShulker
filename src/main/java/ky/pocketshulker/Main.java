package ky.pocketshulker;

import cn.nukkit.Player;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.TextFormat;

import java.util.HashMap;
import java.util.Map;

public class Main extends PluginBase {
    private static Main instance;
    private Map<Player, ShulkerInventory> playerShulkerInventories = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        this.getLogger().info(TextFormat.DARK_GREEN + "Plugin activated.");
        this.getServer().getPluginManager().registerEvents(new ShulkerListener(), this);
    }

    @Override
    public void onDisable() {
        this.getLogger().info(TextFormat.DARK_RED + "Plugin deactivated.");
        playerShulkerInventories.clear();
        instance = null;
    }

    public static Main getInstance() {
        return instance;
    }

    public ShulkerInventory getShulkerInventory(Player player) {
        return playerShulkerInventories.get(player);
    }

    public void setShulkerInventory(Player player, ShulkerInventory shulkerInventory) {
        playerShulkerInventories.put(player, shulkerInventory);
    }

    public void removeShulkerInventory(Player player) {
        playerShulkerInventories.remove(player);
    }
}
