package net.pl3x.map.towny.task;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyObject;
import net.pl3x.map.api.Key;
import net.pl3x.map.api.MapWorld;
import net.pl3x.map.api.SimpleLayerProvider;
import net.pl3x.map.api.marker.MarkerOptions;
import net.pl3x.map.api.marker.Polygon;
import net.pl3x.map.towny.configuration.Config;
import net.pl3x.map.towny.data.Claim;
import net.pl3x.map.towny.data.Group;
import net.pl3x.map.towny.hook.TownyHook;
import net.pl3x.map.towny.util.RectangleMerge;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class Pl3xMapTask extends BukkitRunnable {
    private final MapWorld world;
    private final SimpleLayerProvider provider;

    private boolean stop;

    public Pl3xMapTask(MapWorld world, SimpleLayerProvider provider) {
        this.world = world;
        this.provider = provider;
    }

    @Override
    public void run() {
        if (stop) {
            cancel();
        }

        provider.clearMarkers(); // TODO track markers instead of clearing them

        List<Claim> townClaims = new ArrayList<>();
        for (Town town : TownyHook.getTowns()) {
            town.getTownBlocks().forEach(block ->
                    townClaims.add(new Claim(block.getX(), block.getZ(), town.getUUID())));
        }
        groupClaims(townClaims).forEach(this::drawGroup);
    }

    private List<Group> groupClaims(List<Claim> claims) {
        // break groups down by uuid
        Map<UUID, List<Claim>> byUUID = new HashMap<>();
        for (Claim claim : claims) {
            List<Claim> list = byUUID.getOrDefault(claim.getOwner(), new ArrayList<>());
            list.add(claim);
            byUUID.put(claim.getOwner(), list);
        }

        // combine touching claims
        Map<UUID, List<Group>> groups = new HashMap<>();
        for (Map.Entry<UUID, List<Claim>> entry : byUUID.entrySet()) {
            UUID uuid = entry.getKey();
            List<Claim> list = entry.getValue();
            next1:
            for (Claim claim : list) {
                List<Group> groupList = groups.getOrDefault(uuid, new ArrayList<>());
                for (Group group : groupList) {
                    if (group.isTouching(claim)) {
                        group.add(claim);
                        continue next1;
                    }
                }
                groupList.add(new Group(claim, uuid));
                groups.put(uuid, groupList);
            }
        }

        // combined touching groups
        List<Group> combined = new ArrayList<>();
        for (List<Group> list : groups.values()) {
            next:
            for (Group group : list) {
                for (Group toChk : combined) {
                    if (toChk.isTouching(group)) {
                        toChk.add(group);
                        continue next;
                    }
                }
                combined.add(group);
            }
        }

        return combined;
    }

    private void drawGroup(Group group) {
        Town town = TownyUniverse.getInstance().getTown(group.town());
        if (town == null) {
            return;
        }

        Polygon polygon = RectangleMerge.getPoly(group.claims());
        MarkerOptions.Builder options = options(town);
        polygon.markerOptions(options);

        String markerid = "towny_" + world.name() + "_town_" + group.id();
        this.provider.addMarker(Key.of(markerid), polygon);
    }

    private MarkerOptions.Builder options(Town town) {
        String nation;
        try {
            nation = town.getNation().getName();
        } catch (NotRegisteredException e) {
            nation = "_none_";
        }
        List<String> assistants = town.getRank("assistant").stream()
                .map(TownyObject::getName)
                .collect(Collectors.toList());
        List<String> residents = town.getResidents().stream()
                .map(TownyObject::getName)
                .collect(Collectors.toList());
        String flags = "Has Upkeep: " + town.hasUpkeep()
                + "<br/>pvp: " + town.isPVP()
                + "<br/>mobs: " + town.hasMobs()
                + "<br/>public: " + town.isPublic()
                + "<br/>explosion: " + town.isBANG()
                + "<br/>fire: " + town.isFire()
                + "<br/>nation: " + nation;
        return MarkerOptions.builder()
                .strokeColor(Config.STROKE_COLOR)
                .strokeWeight(Config.STROKE_WEIGHT)
                .strokeOpacity(Config.STROKE_OPACITY)
                .fillColor(Config.FILL_COLOR)
                .fillOpacity(Config.FILL_OPACITY)
                .clickTooltip(Config.CLAIM_TOOLTIP
                        .replace("{name}", town.getName())
                        .replace("{nation}", nation)
                        .replace("{mayor}", town.hasMayor() ? town.getMayor().getName() : "")
                        .replace("{assistants}", String.join(", ", assistants))
                        .replace("{residents}", String.join(", ", residents))
                        .replace("{flags}", flags)
                );
    }

    public void disable() {
        cancel();
        this.stop = true;
        this.provider.clearMarkers();
    }
}

