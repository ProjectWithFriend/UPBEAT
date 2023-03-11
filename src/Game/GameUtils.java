package Game;

import AST.AssignmentNode;
import Parser.*;
import Player.*;
import Region.*;
import Tokenizer.*;
import AST.Node.*;
import Game.GameException.*;

import java.util.*;

public final class GameUtils {

    private static Map<String, Long> nodesEvaluation(List<ExecNode> nodes) {
        Map<String, Long> map = new HashMap<>();
        for (ExecNode node : nodes) {
            if (!(node instanceof AssignmentNode))
                throw new InvalidConfiguration();
            ((AssignmentNode) node).execute(map);
        }
        return map;
    }

    public static Configuration loadConfig(String config) {
        Parser parser = new GrammarParser(new IterateTokenizer(config));
        List<ExecNode> nodes = parser.parse();
        Map<String, Long> map = nodesEvaluation(nodes);
        Configuration configuration = new Configuration() {
            @Override
            public long rows() {
                return map.getOrDefault("m", 20L);
            }

            @Override
            public long cols() {
                return map.getOrDefault("n", 15L);
            }

            @Override
            public long initialPlanMinutes() {
                return map.getOrDefault("init_plan_min", 5L);
            }

            @Override
            public long initialPlanSeconds() {
                return map.getOrDefault("init_plan_sec", 0L);
            }

            @Override
            public long initialBudget() {
                return map.getOrDefault("init_budget", 10000L);
            }

            @Override
            public long initialDeposit() {
                return map.getOrDefault("init_center_dep", 100L);
            }

            @Override
            public long revisionPlanMinutes() {
                return map.getOrDefault("plan_rev_min", 30L);
            }

            @Override
            public long revisionPlanSeconds() {
                return map.getOrDefault("plan_rev_sec", 0L);
            }

            @Override
            public long revisionCost() {
                return map.getOrDefault("rev_cost", 100L);
            }

            @Override
            public long maxDeposit() {
                return map.getOrDefault("max_dep", 1000000L);
            }

            @Override
            public double interestPercentage(long turn, long deposit) {
                return map.getOrDefault("interest_pct", 0L) * Math.log10(deposit) * Math.log(turn);
            }
        };
        if (configuration.initialPlanSeconds() >= 60) throw new InvalidConfiguration();
        if (configuration.revisionPlanSeconds() >= 60) throw new InvalidConfiguration();
        return configuration;
    }

    /**
     * create new territory from given configuration
     *
     * @return null if not territory else new player
     */
    public static List<Region> createTerritory(Configuration configuration) {
        List<Region> territory = new ArrayList<>((int) (configuration.rows() * configuration.cols()));
        for (int i = 0; i < configuration.rows(); i++) {
            for (int j = 0; j < configuration.cols(); j++) {
                territory.add(new RegionProps(Point.of(j, i), configuration.maxDeposit()));
            }
        }
        return territory;
    }

    private static Region pickUnoccupiedRegion(List<Region> territory) {
        Region region;
        Random random = new Random();
        do {
            int regionIndex = random.nextInt(territory.size());
            region = territory.get(regionIndex);
        } while (region.getOwner() != null);
        return region;
    }

    private static int id = 1;

    /**
     * create new a player
     *
     * @return null if no territory else a new player
     */
    public static Player createPlayer(Configuration configuration, List<Region> territory, String name) {
        Region region = pickUnoccupiedRegion(territory);
        Player player = new PlayerProps(id++, name, configuration.initialBudget());
        region.setCityCenter(player);
        region.updateDeposit(configuration.initialDeposit());
        return player;
    }

    public static Configuration defaultConfiguration() {
        return loadConfig("""
                m=4
                n=4
                init_plan_min=5
                init_plan_sec=0
                init_budget=10000
                init_center_dep=100
                plan_rev_min=30
                plan_rev_sec=0
                rev_cost=100
                max_dep=1000000
                interest_pct=5
                """);
    }

    /**
     * create new game instance
     *
     * @param namePlayer1 name of player 1
     * @param namePlayer2 name of player 2
     * @return instance of the game
     */
    public static Game createGame(String namePlayer1, String namePlayer2) {
        Configuration configuration = defaultConfiguration();
        List<Region> territory = createTerritory(configuration);
        Player player1 = createPlayer(configuration, territory, namePlayer1);
        Player player2 = createPlayer(configuration, territory, namePlayer2);
        return new GameProps(configuration, territory, player1, player2);
    }
}
