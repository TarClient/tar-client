package dev.tarclient.launcher;

import java.util.Arrays;
import java.util.List;

/** Cosmetic badge metadata. The server separately authorizes every rank change. */
public enum BadgeRank {
    NORMAL("normal", "Normal / remove rank", 0xFFFFFF),
    PARTNER("partner", "Partner (light purple)", 0xC4A0FF),
    MOD("mod", "Mod (light blue)", 0x91C8FF),
    ADMIN("admin", "Admin (light red)", 0xFF9C9C),
    DONATOR("donator", "Donator (light green)", 0xA8E6A3),
    CO_OWNER("coowner", "Co-Owner (light orange)", 0xFFC58A),
    OWNER("owner", "Owner (orange)", 0xFF8800);

    public final String id, label;
    public final int color;
    BadgeRank(String id, String label, int color) { this.id=id; this.label=label; this.color=color; }
    public static BadgeRank find(String id) { return Arrays.stream(values()).filter(rank->rank.id.equals(id)).findFirst().orElse(null); }
    public static List<BadgeRank> assignable(boolean owner) {
        return owner ? List.of(NORMAL, PARTNER, MOD, ADMIN, DONATOR, CO_OWNER) : List.of(PARTNER, MOD);
    }
}
