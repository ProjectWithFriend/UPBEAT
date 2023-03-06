package AST;

import Game.Game;

public class CollectNode extends Node.ExecNode {
    private final ExprNode expression;

    public CollectNode(ExprNode expression) {
        this.expression = expression;
    }

    @Override
    public boolean execute(Game game) {
        return game.collect(expression.eval(game));
    }
}
