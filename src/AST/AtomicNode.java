package AST;

import Game.Game;

import static AST.Node.*;
import static AST.ASTException.*;

public class AtomicNode extends ExprNode {
    private final long value;
    private final String identifier;

    public AtomicNode(long value) {
        this.value = value;
        this.identifier = null;
    }

    public AtomicNode(String identifier) {
        this.value = 0;
        this.identifier = identifier;
    }

    @Override
    public long eval(Game game) {
        if (identifier == null) {
            return value;
        } else {
            Long specialValue = game.specialIdentifiers().get(identifier);
            if (specialValue != null)
                return specialValue;
            Long value = game.identifiers().get(identifier);
            if (value == null)
                throw new UndefinedIdentifier(identifier);
            return value;
        }
    }

    public long eval() {
        if (identifier == null) {
            return value;
        } else {
            throw new IntegerRequired(identifier);
        }
    }

    @Override
    public String toString() {
        return identifier != null ? identifier : String.valueOf(value);
    }
}
