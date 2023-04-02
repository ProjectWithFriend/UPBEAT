package Game;

import AST.Node;
import Parser.GrammarParser;
import Parser.Parser;
import Player.Player;
import Region.Point;
import Region.*;
import Tokenizer.IterateTokenizer;

import java.util.*;
import java.util.stream.Collectors;

public class GameProps implements Game {
    protected List<Player> players;
    protected final List<Region> territory;
    protected final int actionCost = 1;
    protected Player currentPlayer;
    protected Region cityCrew;
    protected Map<Player, Region> cityCenters;
    protected final Configuration config;
    protected long turn;
    protected Player winner;

    public GameProps(Configuration config, List<Region> territory, Player player1, Player player2) {
        this.turn = 1;
        this.config = config;
        this.territory = territory;
        this.players = List.of(player1, player2);
        this.currentPlayer = players.get(0);
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
    public void invest(long value) {
        currentPlayer.updateBudget(-1);
        if (currentPlayer.getBudget() < value || value <= 0)
            return;
        boolean atLeastOneAdjacent = cityCrew.getOwner() == currentPlayer;
        for (Region adjacent : getAdjacentRegions(cityCrew)) {
            if (atLeastOneAdjacent) break;
            atLeastOneAdjacent = adjacent.getOwner() == currentPlayer;
        }
        if (!atLeastOneAdjacent)
            return;
        currentPlayer.updateBudget(-value);
        cityCrew.updateOwner(currentPlayer);
        cityCrew.updateDeposit(value);
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
            cityCenters.get(currentPlayer).removeCityCenter();
            cityCrew.setCityCenter(currentPlayer);
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
        endTurn();
    }

    @Override
    public List<Region> getTerritory() {
        return territory;
    }


    @Override
    public long getTurnCount() {
        return this.turn;
    }

    @Override
    public Player getWinner() {
        return winner;
    }

    @Override
    public Player getPlayer1() {
        return players.get(0);
    }

    @Override
    public Player getPlayer2() {
        return players.get(1);
    }

    @Override
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public Region regionAt(Point point) {
        long index = point.getY() * config.cols() + point.getX();
        return territory.get((int) index);
    }

    public long budget() {
        return currentPlayer.getBudget();
    }

    protected void beginTurn() {
        getCityCenters();
        cityCrew = cityCenters.get(currentPlayer);
    }

    protected void endTurn() {
        if (winner != null)
            return;

        // round-robin player
        if (currentPlayer.equals(getPlayer1()))
            currentPlayer = getPlayer2();
        else {
            currentPlayer = getPlayer1();
            interestProcess();
            turn++;
        }

        players = players.stream()
                .filter(player -> player.getBudget() > 0)
                .collect(Collectors.toList());
        if (players.size() == 1)
            winner = players.get(0);
        cityCenters = cityCenters.entrySet().stream()
                .filter(e -> e.getValue().getOwner() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        if (cityCenters.size() == 1)
            winner = cityCenters.keySet().iterator().next();
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
    public boolean attack(Direction direction, long expenditure) {
        currentPlayer.updateBudget(-actionCost);
        //validate if the player has enough budget
        if (expenditure > currentPlayer.getBudget())
            return false;
        currentPlayer.updateBudget(-expenditure);

        //get vital information
        Point cityCrewLocation = cityCrew.getLocation();
        Point targetLocation = cityCrewLocation.direction(direction);

        //validate if the target location is valid
        if (!targetLocation.isValidPoint(config.rows(), config.cols()))
            return true;
        Region targetRegion = regionAt(targetLocation);
        Player oldOwner = targetRegion.updateDeposit(-expenditure);
        if (oldOwner != null && cityCenters.containsValue(targetRegion))
            winner = currentPlayer;
        return true;
    }
}
