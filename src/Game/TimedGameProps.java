package Game;

import Player.Player;
import Region.Region;

import java.util.List;

public class TimedGameProps extends GameProps {
    public TimedGameProps(Configuration config, List<Region> territory, Player player1, Player player2) {
        super(config, territory, player1, player2);
    }
}
