package Game;

import Game.GameException.NotImplemented;
import Player.Player;
import Region.EuclidianPoint;
import Region.Point;
import Region.Region;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public final class GameTest {
    private TestPlayer player1, player2;
    private List<TestRegion> territory;
    private GameProps game;

    private static TestRegion mockRegion(Point location, long maxDeposit) {
        return new TestRegion() {
            private boolean isCityCenter;

            @Override
            public boolean getIsCityCenter() {
                return isCityCenter;
            }

            @Override
            public Player getOwner() {
                return owner;
            }

            @Override
            public void removeCityCenter() {
                isCityCenter = false;
            }

            @Override
            public long getDeposit() {
                return deposit;
            }

            @Override
            public void updateDeposit(long amount) {
                deposit = Math.max(0, deposit + amount);
                deposit = Math.min(maxDeposit, deposit);
            }

            @Override
            public void updateOwner(Player owner) {
                this.owner = owner;
            }

            @Override
            public void setCityCenter(Player owner) {
                isCityCenter = true;
                updateOwner(owner);
            }

            @Override
            public Point getLocation() {
                return location;
            }
        };
    }

    private static List<TestRegion> mockTerritory(int rows, int cols, long maxDeposit) {
        List<TestRegion> regions = new ArrayList<>(rows * cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Point location = new EuclidianPoint(j, i);
                regions.add(mockRegion(location, maxDeposit));
            }
        }
        return regions;
    }

    private static TestPlayer mockPlayer(TestRegion initCenterLocation) {
        TestPlayer player = new TestPlayer(initCenterLocation) {

            @Override
            public long getBudget() {
                return budget;
            }

            @Override
            public boolean updateBudget(long amount) {
                boolean result = budget + amount >= 0;
                budget = Math.max(0, budget + amount);
                return result;
            }

            @Override
            public String getName() {
                throw new NotImplemented();
            }


            @Override
            public long getID() {
                throw new NotImplemented();
            }

            @Override
            public Map<String, Long> identifiers() {
                return identifiers;
            }
        };
        initCenterLocation.setCityCenter(player);
        return player;
    }

    private static Configuration mockConfiguration() {
        return GameUtils.defaultConfiguration();
    }

    @BeforeEach
    public void before() {
        Configuration configuration = mockConfiguration();
        territory = mockTerritory(4, 4, configuration.maxDeposit());
        player1 = mockPlayer(territory.get(4));
        player2 = mockPlayer(territory.get(7));
        List<Region> territoryRegion = new ArrayList<>(territory.size());
        territoryRegion.addAll(territory);
        game = new GameProps(configuration, territoryRegion, player1, player2);
    }

    @Test
    public void testCollect() {
        for (int i = 0; i < 2; i++) {
            game.beginTurn();
            TestPlayer currentPlayer = i % 2 == 1 ? player2 : player1;
            assertFalse(game.collect(0));

            currentPlayer.budget = (1);
            assertTrue(game.collect(0));
            assertEquals(0, currentPlayer.getBudget());

            currentPlayer.budget = 1;
            assertFalse(game.collect(-1)); // TODO: clarification
            assertEquals(1, currentPlayer.getBudget());

            TestRegion region = (TestRegion) game.cityCrewRegion();
            region.updateDeposit(100);

            currentPlayer.budget = 2;
            assertTrue(game.collect(0));
            assertEquals(1, currentPlayer.budget);
            assertEquals(100, region.deposit);

            assertTrue(game.collect(1));
            assertEquals(1, currentPlayer.budget);
            assertEquals(99, region.deposit);

            assertTrue(game.collect(2));
            assertEquals(2, currentPlayer.budget);
            assertEquals(97, region.deposit);

            assertTrue(game.collect(98));
            assertEquals(1, currentPlayer.budget);
            assertEquals(97, region.deposit);

            assertTrue(game.collect(97));
            assertEquals(97, currentPlayer.budget);
            assertEquals(0, region.deposit);

            game.endTurn();
        }
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
        game.beginTurn();
        player1.updateBudget(1000);
        game.moveCityCrew(Point.of(0, 0));
        game.moveCityCrew(Point.of(3, 2));
        game.relocate();
        assertEquals(974, game.budget());
        game.moveCityCrew(Point.of(0, 0));
        game.relocate();
        assertEquals(948, game.budget());

        player1.updateBudget(-948);
        game.moveCityCrew(Point.of(3, 2));
        game.relocate();
        assertEquals(0, game.budget());
    }

    @Test
    public void attack() {
        game.beginTurn();
        player1.updateBudget(1000);
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
        game.attack(Direction.Up, 999);
        assertEquals(998, game.budget());
        game.attack(Direction.Up, 997);
        assertEquals(0, game.budget());
        assertEquals(player2, territory.get(1).getOwner());
        assertEquals(9003, territory.get(1).getDeposit());
    }

    @Test
    public void testInvest() {
        for (int i = 0; i < 2; i++) {
            game.beginTurn();

            TestPlayer currentPlayer = i == 0 ? player1 : player2;
            TestRegion crewRegion = (TestRegion) game.cityCrewRegion();

            // invest always cost a unit
            currentPlayer.budget = 1;
            game.invest(0);
            assertEquals(0, currentPlayer.budget);
            assertEquals(0, crewRegion.deposit);

            // invest cost x+1 where x amount of invest
            currentPlayer.budget = 12;
            game.invest(11);
            assertEquals(0, currentPlayer.budget);
            assertEquals(11, crewRegion.deposit);

            // invest only allowed when target region have adjacent owned player region
            game.moveCityCrew(Point.of(3, 3)); // no owned adjacent with 2 players
            crewRegion = territory.get(15);
            currentPlayer.budget = 1;
            game.invest(0);
            assertEquals(0, currentPlayer.budget);
            assertEquals(0, crewRegion.deposit);

            if (currentPlayer == player1) {
                game.moveCityCrew(Point.of(0, 0));
                crewRegion = territory.get(0);
            } else {
                game.moveCityCrew(Point.of(3, 0));
                crewRegion = territory.get(3);
            }
            currentPlayer.budget = 14;
            game.invest(12);
            assertEquals(1, currentPlayer.budget);
            assertEquals(12, crewRegion.deposit);
            game.endTurn();
        }
    }

    @Test
    public void testOpponent() {
        game.beginTurn();
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
        game.beginTurn();
        TestPlayer currentPlayer = player1;
        assertFalse(game.move(Direction.Up));
        assertFalse(game.move(Direction.UpLeft));
        assertFalse(game.move(Direction.UpRight));
        assertFalse(game.move(Direction.Down));
        assertFalse(game.move(Direction.DownLeft));
        assertFalse(game.move(Direction.DownRight));

        currentPlayer.budget = 2;
        assertEquals(Point.of(0, 1), game.cityCrewRegion().getLocation());

        assertTrue(game.move(Direction.Up)); // move one up
        assertEquals(Point.of(0, 0), game.cityCrewRegion().getLocation());
        assertEquals(1, currentPlayer.budget);

        assertTrue(game.move(Direction.Up)); // act like no-op
        assertEquals(Point.of(0, 0), game.cityCrewRegion().getLocation());
        assertEquals(0, currentPlayer.budget);

        assertFalse(game.move(Direction.Up));
        assertEquals(Point.of(0, 0), game.cityCrewRegion().getLocation());
        assertEquals(0, currentPlayer.budget);

        currentPlayer.budget = 1;
        assertTrue(game.move(Direction.DownRight));
        assertEquals(Point.of(1, 1), game.cityCrewRegion().getLocation());

        // move all directions
        currentPlayer.budget = 6;
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
        currentPlayer.budget = 4;
        assertTrue(game.move(Direction.DownRight));
        assertTrue(game.move(Direction.DownRight));
        assertEquals(Point.of(3, 2), game.cityCrewRegion().getLocation());
        assertTrue(game.move(Direction.UpLeft));
        assertTrue(game.move(Direction.UpLeft));
        assertEquals(Point.of(1, 1), game.cityCrewRegion().getLocation());

        currentPlayer.budget = 4;
        assertTrue(game.move(Direction.Down));
        assertTrue(game.move(Direction.Down));
        assertEquals(Point.of(1, 3), game.cityCrewRegion().getLocation());
        assertTrue(game.move(Direction.Up));
        assertTrue(game.move(Direction.Up));
        assertEquals(Point.of(1, 1), game.cityCrewRegion().getLocation());

        // move into opponent region
        currentPlayer.budget = 2;
        assertTrue(game.move(Direction.DownRight));
        assertEquals(Point.of(2, 1), game.cityCrewRegion().getLocation());
        assertTrue(game.move(Direction.UpRight));
        assertEquals(Point.of(2, 1), game.cityCrewRegion().getLocation());
        assertFalse(game.move(Direction.UpRight));
        assertEquals(Point.of(2, 1), game.cityCrewRegion().getLocation());
        game.endTurn();
    }

    @Test
    public void testInterestPercentage() {
        Region playerRegion = territory.get(4);
        playerRegion.updateDeposit(100);
        Configuration configuration = mockConfiguration();
        long playerDeposit = 100;
        for (int i = 0; i <= 100; i++) {
            game.beginTurn();
            playerDeposit *= 1.0 + configuration.interestPercentage(i, playerDeposit) / 100.0;
            assertEquals(Math.min(configuration.maxDeposit(), playerDeposit), // must not exceed limit
                    playerRegion.getDeposit(), String.format("not equals at turn %d", i));
            game.endTurn();
            game.beginTurn();
            game.endTurn();
        }
    }

    private static abstract class TestRegion implements Region {
        public long deposit = 0;
        public Player owner = null;
    }

    private static abstract class TestPlayer implements Player {
        public final Map<String, Long> identifiers = new HashMap<>();
        public TestRegion cityCenter;
        public long budget = 0;

        public TestPlayer(TestRegion cityCenter) {
            cityCenter.updateOwner(this);
            this.cityCenter = cityCenter;
        }
    }
}