package net.pl3x.map.towny;

import net.pl3x.map.towny.configuration.Config;
import net.pl3x.map.towny.hook.Pl3xMapHook;
import org.bukkit.plugin.java.JavaPlugin;

public final class Pl3xMapTowny extends JavaPlugin {
    private Pl3xMapHook pl3xmapHook;

    @Override
    public void onEnable() {
        Config.reload(this);

        if (!getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
            getLogger().severe("WorldGuard not found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!getServer().getPluginManager().isPluginEnabled("Pl3xMap")) {
            getLogger().severe("Pl3xMap not found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        pl3xmapHook = new Pl3xMapHook(this);
    }

    @Override
    public void onDisable() {
        if (pl3xmapHook != null) {
            pl3xmapHook.disable();
        }
    }
}
