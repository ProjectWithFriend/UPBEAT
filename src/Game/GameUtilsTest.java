package Game;

import Game.GameException.*;
import org.junit.jupiter.api.Test;

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
}
