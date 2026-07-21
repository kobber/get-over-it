package kobber.getoverit.api;

import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.WeakHashMap;

public class ClimbingState {

    private static final Map<Player, Boolean> CLIMBING_PLAYERS = new WeakHashMap<>();

    public static void setClimbing(Player player, boolean climbing) {
        if (climbing) {
            CLIMBING_PLAYERS.put(player, true);
        } else {
            CLIMBING_PLAYERS.remove(player);
        }
    }

    public static boolean isClimbing(Player player) {
        return CLIMBING_PLAYERS.getOrDefault(player, false);
    }
}