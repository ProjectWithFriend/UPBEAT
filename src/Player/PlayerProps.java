package Player;

import java.util.HashMap;
import java.util.Map;

public class PlayerProps implements Player {
    private final long id;
    private final String name;
    private long budget;
    private final Map<String, Long> identifier;

    public PlayerProps(long id, String name, long budget) {
        this.id = id;
        this.name = name;
        this.identifier = new HashMap<>();
        this.budget = budget;
    }

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
        return this.name;
    }

    @Override
    public long getID() {
        return id;
    }

    @Override
    public Map<String, Long> identifiers() {
        return identifier;
    }

    @Override
    public String toString() {
        return name;
    }
}
