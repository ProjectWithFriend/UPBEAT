package Game;

import Player.*;
import Region.EuclidianPoint;
import Region.Point;
import Region.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public final class GamePropsTest {
    private TestPlayer player1, player2;
    private List<TestRegion> territory;
    private GameProps game;

    private static class TestRegion extends RegionProps {
        public TestRegion(Point location, long maxDeposit) {
            super(location, maxDeposit);
        }
    }

    private static class TestPlayer extends PlayerProps {
        public long budget = 1;

        public TestPlayer(TestRegion cityCenter) {
            super(0, "", 1000);
            cityCenter.setCityCenter(this);
            cityCenter.updateDeposit(100);
        }
    }

    private static Configuration createConfiguration() {
        return GameUtils.defaultConfiguration();
    }

    @BeforeEach
    public void before() {
        Configuration configuration = createConfiguration();
        createTeritory();
        createPlayers();
        List<Region> territory = new ArrayList<>(4 * 4);
        territory.addAll(this.territory);
        game = new GameProps(configuration, territory, player1, player2);
        game.beginTurn();
    }

    private void createPlayers() {
        player1 = new TestPlayer(territory.get(4));
        player2 = new TestPlayer(territory.get(7));
    }

    private void createTeritory() {
        territory = new ArrayList<>(4 * 4);
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                Point location = new EuclidianPoint(j, i);
                territory.add(new TestRegion(location, 1000000));
            }
        }
    }

    @AfterEach
    public void after() {
        game.endTurn();
    }

    @Test
    public void testCollect() {
        TestPlayer currentPlayer = player1;
        assertTrue(game.collect(0));
        assertEquals(999, currentPlayer.getBudget());

        assertFalse(game.collect(-1));
        assertEquals(999, currentPlayer.getBudget());

        assertTrue(game.collect(0));
        assertEquals(998, currentPlayer.getBudget());
        assertEquals(100, game.cityCrewRegion().getDeposit());

        assertTrue(game.collect(1));
        assertEquals(998, currentPlayer.getBudget());
        assertEquals(99, game.cityCrewRegion().getDeposit());

        assertTrue(game.collect(2));
        assertEquals(999, currentPlayer.getBudget());
        assertEquals(97, game.cityCrewRegion().getDeposit());

        assertTrue(game.collect(98));
        assertEquals(998, currentPlayer.getBudget());
        assertEquals(97, game.cityCrewRegion().getDeposit());

        assertTrue(game.collect(97));
        assertEquals(997+97, currentPlayer.getBudget());
        assertEquals(0, game.cityCrewRegion().getDeposit());
    }

    @Test
    public void nearby() {
        player1.updateBudget(1000);
        game.moveCityCrew(Point.of(0, 0));
        assertEquals(0, game.nearby(Direction.Up));
        assertEquals(0, game.nearby(Direction.UpLeft));
        assertEquals(0, game.nearby(Direction.UpRight));
        assertEquals(0, game.nearby(Direction.Down));
        assertEquals(0, game.nearby(Direction.DownLeft));
        assertEquals(0, game.nearby(Direction.DownRight));
        territory.get(11).updateOwner(player2);
        assertEquals(301, game.nearby(Direction.DownRight));
        territory.get(11).updateDeposit(100);
        assertEquals(303, game.nearby(Direction.DownRight));
    }

    @Test
    public void relocate() {
        long initialBudget = 100, distance;
        game.moveCityCrew(Point.of(3, 3));
        territory.get(15).updateOwner(player1); // x: 3, y: 3

        distance = 3;
        game.relocate();
        assertEquals(5 * distance + 10 + game.actionCost, 1000 - game.budget());

        game.moveCityCrew(Point.of(0, 0));
        territory.get(0).updateOwner(player1); // x: 0, y: 0

        distance = 1;
        game.relocate();
        assertEquals(5 * distance + 10 + game.actionCost, 974 - game.budget());
    }

    @Test
    public void attack() {
        game.moveCityCrew(Point.of(0, 0));
        territory.get(6).updateOwner(player2);
        territory.get(6).updateDeposit(100);
        game.moveCityCrew(Point.of(1, 1));
        game.attack(Direction.DownRight, 100);
        assertEquals(899, game.budget());
        assertNull(territory.get(6).getOwner());

        //update budget to 0
        player1.updateBudget(-899);
        territory.get(6).updateOwner(player2);
        game.attack(Direction.DownRight, 100);
        assertEquals(0, game.budget());
        assertEquals(player2, territory.get(6).getOwner());

//        //update budget to 1000
        player1.updateBudget(1000);
        territory.get(1).updateOwner(player2);
        territory.get(1).updateDeposit(10000);

        game.attack(Direction.Up, 10000);
        assertEquals(999, game.budget());
        assertEquals(player2, territory.get(1).getOwner());

        game.attack(Direction.Up, 997);
        assertEquals(1, game.budget());
        assertEquals(player2, territory.get(1).getOwner());
        assertEquals(9003, territory.get(1).getDeposit());
    }

    @Test
    public void testInvest() {
        TestPlayer currentPlayer = player1;
        TestRegion crewRegion = (TestRegion) game.cityCrewRegion();

        // invest always cost a unit
        game.invest(0);
        assertEquals(999, currentPlayer.getBudget());
        assertEquals(100, crewRegion.getDeposit());

        // invest cost x+1 where x amount of invest
        game.invest(11);
        assertEquals(999-12, currentPlayer.getBudget());
        assertEquals(111, crewRegion.getDeposit());

        // invest only allowed when target region have adjacent owned player region
        game.moveCityCrew(Point.of(3, 3)); // no owned adjacent with 2 players
        crewRegion = territory.get(15);
        game.invest(10);
        assertEquals(999-12-1, currentPlayer.getBudget());
        assertEquals(0, crewRegion.getDeposit());
    }

    @Test
    public void testOpponent() {
        assertEquals(0, game.opponent());
        game.moveCityCrew(Point.of(3, 2));
        assertEquals(11, game.opponent());
        game.moveCityCrew(Point.of(3, 0));
        assertEquals(14, game.opponent());
        game.moveCityCrew(Point.of(2, 0));
        assertEquals(13, game.opponent());
        game.moveCityCrew(Point.of(1, 0));
        assertEquals(23, game.opponent());
        game.moveCityCrew(Point.of(2, 1));
        assertEquals(12, game.opponent());
        game.moveCityCrew(Point.of(1, 2));
        assertEquals(22, game.opponent());
    }

    @Test
    public void testMove() {
        assertEquals(Point.of(0, 1), game.cityCrewRegion().getLocation());

        assertTrue(game.move(Direction.Up)); // move one up
        assertEquals(Point.of(0, 0), game.cityCrewRegion().getLocation());
        assertEquals(999, player1.getBudget());

        assertTrue(game.move(Direction.Up)); // act like no-op
        assertEquals(Point.of(0, 0), game.cityCrewRegion().getLocation());
        assertEquals(998, player1.getBudget());

        assertTrue(game.move(Direction.DownRight));
        assertEquals(Point.of(1, 1), game.cityCrewRegion().getLocation());

        // move all directions
        player1.budget = 6;
        assertTrue(game.move(Direction.UpLeft));
        assertEquals(Point.of(0, 0), game.cityCrewRegion().getLocation());
        assertTrue(game.move(Direction.DownRight));
        assertEquals(Point.of(1, 1), game.cityCrewRegion().getLocation());

        assertTrue(game.move(Direction.Up));
        assertEquals(Point.of(1, 0), game.cityCrewRegion().getLocation());
        assertTrue(game.move(Direction.Down));
        assertEquals(Point.of(1, 1), game.cityCrewRegion().getLocation());

        assertTrue(game.move(Direction.UpRight));
        assertEquals(Point.of(2, 0), game.cityCrewRegion().getLocation());
        assertTrue(game.move(Direction.DownLeft));
        assertEquals(Point.of(1, 1), game.cityCrewRegion().getLocation());

        // move 2 steps
        player1.budget = 4;
        assertTrue(game.move(Direction.DownRight));
        assertTrue(game.move(Direction.DownRight));
        assertEquals(Point.of(3, 2), game.cityCrewRegion().getLocation());
        assertTrue(game.move(Direction.UpLeft));
        assertTrue(game.move(Direction.UpLeft));
        assertEquals(Point.of(1, 1), game.cityCrewRegion().getLocation());

        player1.budget = 4;
        assertTrue(game.move(Direction.Down));
        assertTrue(game.move(Direction.Down));
        assertEquals(Point.of(1, 3), game.cityCrewRegion().getLocation());
        assertTrue(game.move(Direction.Up));
        assertTrue(game.move(Direction.Up));
        assertEquals(Point.of(1, 1), game.cityCrewRegion().getLocation());

        // move into opponent region
        player1.budget = 2;
        assertTrue(game.move(Direction.DownRight));
        assertEquals(Point.of(2, 1), game.cityCrewRegion().getLocation());
        assertTrue(game.move(Direction.UpRight));
        assertEquals(Point.of(2, 1), game.cityCrewRegion().getLocation());
        game.endTurn();
    }

    @Test
    public void testInterestPercentage() {
        Region playerRegion = territory.get(4);
        Configuration configuration = createConfiguration();
        long playerDeposit = 100;
        for (int i = 1; i <= 100; i++) {
            game.submitPlan("done");
            game.submitPlan("done");
            playerDeposit *= 1.0 + configuration.interestPercentage(i, playerDeposit) / 100.0;
            assertEquals(Math.min(configuration.maxDeposit(), playerDeposit), // must not exceed limit
                    playerRegion.getDeposit(), String.format("not equals at turn %d", i));
        }
    }

    @Test
    public void defeatedByOutOfBudget() {
        game.submitPlan("done");
        game.submitPlan("done");
        assertNull(game.getWinner());

        player1.updateBudget(-1000);
        game.submitPlan("done");
        assertThrows(GameException.GameEnded.class, () -> game.submitPlan("done"));
        assertEquals(player2, game.getWinner());
    }

    @Test
    public void defeatedByNoCityCenter() {
        game.submitPlan("done");
        game.submitPlan("done");
        assertNull(game.getWinner());

        territory.get(7).updateOwner(null);
        game.submitPlan("done");
        assertThrows(GameException.GameEnded.class, () -> game.submitPlan("done"));
        assertEquals(player1, game.getWinner());
    }
}