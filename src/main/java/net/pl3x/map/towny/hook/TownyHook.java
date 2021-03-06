package net.pl3x.map.towny.hook;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;

import java.util.Collection;

public class TownyHook {
    public static Collection<Town> getTowns() {
        return TownyAPI.getInstance().getDataSource().getTowns();
    }
}
