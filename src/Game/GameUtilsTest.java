package Game;

import Game.GameException.*;
import org.junit.jupiter.api.Test;
import Region.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public final class GameUtilsTest {
    @Test
    public void testLoadConfig() {
        assertDoesNotThrow(() -> GameUtils.loadConfig("""
                m=1
                n=1
                init_plan_min=1
                init_plan_sec=1
                init_budget=1
                init_center_dep=1
                plan_rev_min=1
                plan_rev_sec=1
                rev_cost=1
                max_dep=1
                interest_pct=1
                """));
        assertDoesNotThrow(() -> GameUtils.loadConfig("""
                m=1
                n=1
                """));
        assertThrows(InvalidConfiguration.class, () -> GameUtils.loadConfig("""
                plan_rev_sec=60
                """));
    }

    @Test
    public void testCityCenter() {
        Game game = GameUtils.createGame("a", "b");
        List<Region> territory = game.getTerritory();
        List<Region> cityCenters = new ArrayList<>(2);
        for (Region region : territory) {
            if (region.getIsCityCenter()) cityCenters.add(region);
        }
        assertEquals(2, cityCenters.size(), "more than two city centers created");
        assertNotEquals(cityCenters.get(0), cityCenters.get(1), "city center collapse");
    }

    @Test
    public void testRegionLocation() {
        Game game = GameUtils.createGame("a", "b");
        List<Region> territory = game.getTerritory();
        Set<Point> regions = new HashSet<>();
        for (Region region : territory) {
            regions.add(region.getLocation());
        }
        assertEquals(regions.size(), territory.size(), "duplicated region found");
    }
}
