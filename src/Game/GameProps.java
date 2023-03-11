package Game;

import AST.Node;
import Parser.GrammarParser;
import Parser.Parser;
import Player.Player;
import Region.Point;
import Region.*;
import Tokenizer.IterateTokenizer;

import java.util.*;

public class GameProps implements Game {
    protected final Player player1;
    protected final Player player2;
    protected final List<Region> territory;
    protected final int actionCost = 1;
    protected Player currentPlayer;
    protected Region cityCrew;
    protected final Map<Player, Region> cityCenters;
    protected final Configuration config;
    protected long turn;
    protected Player winner;

    public GameProps(Configuration config, List<Region> territory, Player player1, Player player2) {
        this.turn = 1;
        this.config = config;
        this.territory = territory;
        this.player1 = player1;
        this.player2 = player2;
        this.currentPlayer = this.player1;
        this.cityCenters = new HashMap<>();
    }

    private void getCityCenters() {
        for (Region region : territory) {
            if (region.getIsCityCenter())
                cityCenters.put(region.getOwner(), region);
        }
    }

    @Override
    public boolean collect(long value) {
        if (currentPlayer.getBudget() < 1 || value < 0)
            return false;
        currentPlayer.updateBudget(-1);
        Region targetRegion = cityCrew;
        if (value > targetRegion.getDeposit())
            return true;
        targetRegion.updateDeposit(-value);
        currentPlayer.updateBudget(value);
        if (targetRegion.getDeposit() == 0)
            targetRegion.updateOwner(null);
        return true;
    }

    private List<Region> getAdjacentRegions(Region region) {
        List<Region> adjacentRegions = new ArrayList<>(6);
        Point currentLocation = region.getLocation();
        for (Direction direction : Direction.values()) {
            Point newLocation = currentLocation.direction(direction);
            if (!newLocation.isValidPoint(config.rows(), config.cols()))
                continue;
            adjacentRegions.add(regionAt(newLocation));
        }
        return adjacentRegions;
    }

    @Override
    public boolean invest(long value) {
        currentPlayer.updateBudget(-1);
        boolean atLeastOneAdjacent = cityCrew.getOwner() == currentPlayer;
        for (Region adjacent : getAdjacentRegions(cityCrew)) {
            if (atLeastOneAdjacent) break;
            atLeastOneAdjacent = adjacent.getOwner() == currentPlayer;
        }
        if (!atLeastOneAdjacent) // adjacency requirement
            return true;
        if (currentPlayer.getBudget() < value) // budget requirement
            return true;
        currentPlayer.updateBudget(-value);
        cityCrew.updateOwner(currentPlayer);
        cityCrew.updateDeposit(value);
        return true;
    }

    private record GoaledPoint(Point position, Point goal) implements Comparable<GoaledPoint> {
        @Override
        public int compareTo(GoaledPoint o) {
            return (int) (distancePythagoras(position, goal) - distancePythagoras(o.position, goal));
        }
    }

    /**
     * find the shortest distance according to <a href="https://en.wikipedia.org/wiki/A*_search_algorithm">wikipedia<a/>
     *
     * @param start starting point
     * @param goal  target point
     * @return `-1` when no path possible else distance
     */
    private long distanceAStar(Point start, Point goal) {
        PriorityQueue<GoaledPoint> openSet = new PriorityQueue<>();
        HashMap<Point, Point> cameFrom = new HashMap<>();
        HashMap<Point, Double> gScore = new HashMap<>();
        openSet.add(new GoaledPoint(start, goal));
        gScore.put(start, 0.0);
        while (!openSet.isEmpty()) {
            Point current = openSet.remove().position;
            if (current.equals(goal))
                return findDistanceAStar(cameFrom, current);
            for (Direction direction : Direction.values()) {
                Point neighbor = current.direction(direction);
                if (!neighbor.isValidPoint(config.rows(), config.cols()) || neighbor.equals(start))
                    continue;
                double tentativeGScore = gScore.get(current) + distancePythagoras(current, neighbor);
                gScore.putIfAbsent(neighbor, Double.POSITIVE_INFINITY);
                if (tentativeGScore >= gScore.get(neighbor))
                    continue;
                cameFrom.put(neighbor, current);
                gScore.put(neighbor, tentativeGScore);
                GoaledPoint point = new GoaledPoint(neighbor, goal);
                if (!openSet.contains(point))
                    openSet.add(point);
            }
        }
        return -1;
    }

    private long findDistanceAStar(HashMap<Point, Point> cameFrom, Point current) {
        long distance = 0;
        current = cameFrom.get(current);
        while (current != null) {
            distance++;
            current = cameFrom.get(current);
        }
        return distance;
    }

    private static double distancePythagoras(Point from, Point to) {
        return Math.sqrt(Math.pow(from.getX() - to.getX(), 2) + Math.pow(from.getY() - to.getY(), 2)) * 10;
    }

    @Override
    public boolean relocate() {
        //check if the player has enough budget
        if (!currentPlayer.updateBudget(-actionCost))
            return false;

        Point currentCityCrewLocation = cityCrew.getLocation();
        Point currentCityCenter = cityCenters.get(currentPlayer).getLocation();
        long distance = distanceAStar(currentCityCrewLocation, currentCityCenter);
        long cost = 5 * distance + 10;

        //validate if the player has enough budget
        if (currentPlayer.getBudget() >= cost && cityCrew.getOwner() == currentPlayer) {
            currentPlayer.updateBudget(-cost);
            //update the city center location of current player
            cityCrew.setCityCenter(currentPlayer);
            cityCenters.get(currentPlayer).removeCityCenter();
        }
        return false;
    }

    @Override
    public long nearby(Direction direction) {
        Point currentLocation = cityCrew.getLocation();
        int distance = 0;
        Point newLocation = currentLocation.direction(direction);
        while (newLocation.isValidPoint(config.rows(), config.cols())) {
            Region region = regionAt(newLocation);
            if (region.getOwner() != null && region.getOwner() != currentPlayer)
                return ((distance + 1L) * 100 + (long) (Math.log10(region.getDeposit() + 1)) + 1);
            distance++;
            newLocation = newLocation.direction(direction);
        }
        return 0L;
    }

    @Override
    public long opponent() {
        Point[] spreads = new Point[6];
        int distance = 0;
        boolean stop;
        for (int i = 0; i < 6; i++)
            spreads[i] = cityCrew.getLocation();
        do {
            for (int i = 0; i < 6; i++) {
                if (spreads[i] == null)
                    continue;
                long index = spreads[i].getY() * config.cols() + spreads[i].getX();
                Player owner = territory.get((int) index).getOwner();
                if (owner != null && owner != currentPlayer)
                    return i + 1L + distance * 10L;
                spreads[i] = spreads[i].direction(Direction.values()[i]);
            }
            for (int i = 0; i < 6; i++) {
                if (spreads[i] == null)
                    continue;
                spreads[i] = spreads[i].isValidPoint(config.rows(), config.cols()) ? spreads[i] : null;
            }
            stop = true;
            for (Point point : spreads)
                stop &= point == null;
            distance++;
        } while (!stop);
        return 0;
    }

    private void executePlan(String plan) {
        Parser parser = new GrammarParser(new IterateTokenizer(plan));
        List<Node.ExecNode> nodes = parser.parse();
        for (Node.ExecNode node : nodes) {
            node.execute(this);
        }
    }

    @Override
    public void submitPlan(String constructionPlan) {
        if (winner != null)
            throw new GameException.GameEnded();
        beginTurn();
        executePlan(constructionPlan);
        winner = findWinner();
        endTurn();
    }

    private Player findWinner() {
        if (player1.getBudget() == 0)
            return player2;
        else if (player2.getBudget() == 0)
            return player1;
        if (territory.stream().noneMatch(region -> region.getIsCityCenter() && region.getOwner() == player1))
            return player2;
        else if (territory.stream().noneMatch(region -> region.getIsCityCenter() && region.getOwner() == player2))
            return player1;
        return null;
    }

    @Override
    public List<Region> getTerritory() {
        return territory;
    }

    @Override
    public Player getPlayer1() {
        return player1;
    }

    @Override
    public Player getPlayer2() {
        return player2;
    }

    @Override
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    @Override
    public Player winner() {
        return winner;
    }

    @Override
    public long getTurn() {
        return turn;
    }

    @Override
    public Region regionAt(Point point) {
        long index = point.getY() * config.cols() + point.getX();
        return territory.get((int) index);
    }

    @Override
    public long budget() {
        return currentPlayer.getBudget();
    }

    public void beginTurn() {
        getCityCenters();
        cityCrew = cityCenters.get(currentPlayer);
    }

    public void endTurn() {
        if (currentPlayer == player1) {
            currentPlayer = player2;
        } else {
            currentPlayer = player1;
            interestProcess();
            turn++;
        }
    }

    private void interestProcess() {
        for (Region region : territory) {
            if (region.getOwner() != null) {
                long deposit = region.getDeposit();
                deposit *= config.interestPercentage(turn, deposit) / 100.0;
                region.updateDeposit(deposit);
            }
        }
    }

    @Override
    public Region cityCrewRegion() {
        return cityCrew;
    }

    public void moveCityCrew(Point point) {
        if (!point.isValidPoint(config.rows(), config.cols()))
            return;
        cityCrew = regionAt(point);
    }

    @Override
    public boolean move(Direction direction) {
        if (currentPlayer.getBudget() < actionCost)
            return false;
        currentPlayer.updateBudget(-actionCost);
        Point newLocation = cityCrew.getLocation().direction(direction);
        if (newLocation.isValidPoint(config.rows(), config.cols())) {
            Region newRegion = regionAt(newLocation);
            if (newRegion.getOwner() == null || newRegion.getOwner() == currentPlayer)
                cityCrew = newRegion;
        }
        return true;
    }

    @Override
    public Map<String, Long> identifiers() {
        return currentPlayer.identifiers();
    }

    @Override
    public Map<String, Long> specialIdentifiers() {
        Map<String, Long> map = new HashMap<>();
        map.put("rows", config.cols());
        map.put("cols", config.cols());
        map.put("currow", cityCrew.getLocation().getX());
        map.put("curcol", cityCrew.getLocation().getY());
        map.put("budget", currentPlayer.getBudget());
        map.put("deposit", cityCrew.getDeposit());
        map.put("int", (long) config.interestPercentage(turn, cityCrew.getDeposit()));
        map.put("maxdeposit", config.maxDeposit());
        map.put("random", new Random().nextLong(1000));
        return map;
    }

    @Override
    public boolean attack(Direction direction, long value) {
        //validate if the player has enough budget
        if (value + actionCost > currentPlayer.getBudget() || value < 0) {
            currentPlayer.updateBudget(-actionCost);
            return false;
        }

        //get vital information
        Point cityCrewLocation = cityCrew.getLocation();
        Point targetLocation = cityCrewLocation.direction(direction);

        //validate if the target location is valid
        if (targetLocation.isValidPoint(config.rows(), config.cols())) {
            Region targetRegion = regionAt(targetLocation);
            if (value < targetRegion.getDeposit()) {
                //update the budget of current player
                currentPlayer.updateBudget(-actionCost - value);
                //update the deposit of the target region
                targetRegion.updateDeposit(-value);
            } else if (value >= targetRegion.getDeposit()) {
                targetRegion.updateDeposit(-value);
                targetRegion.updateOwner(null);
                currentPlayer.updateBudget(-actionCost - value);
            }
        }
        return true;
    }
}
