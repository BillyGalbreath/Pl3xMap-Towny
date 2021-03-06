package net.pl3x.map.towny.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Group {
    private final List<Claim> claims = new ArrayList<>();
    private final UUID town;

    public Group(Claim claim, UUID town) {
        add(claim);
        this.town = town;
    }

    public boolean isTouching(Claim claim) {
        for (Claim toChk : claims) {
            if (toChk.isTouching(claim)) {
                return true;
            }
        }
        return false;
    }

    public boolean isTouching(Group group) {
        for (Claim claim : group.claims()) {
            if (isTouching(claim)) {
                return true;
            }
        }
        return false;
    }

    public void add(Claim claim) {
        claims.add(claim);
    }

    public void add(Group group) {
        claims.addAll(group.claims());
    }

    public List<Claim> claims() {
        return claims;
    }

    public UUID town() {
        return town;
    }

    public String id() {
        if (claims.size() > 0) {
            Claim claim = claims.get(0);
            return claim.getX() + "_" + claim.getZ();
        } else {
            return "NaN_NaN";
        }
    }
}
