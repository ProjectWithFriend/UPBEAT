package AST;

import Game.Game;

public class InvestNode extends Node.ExecNode {
    private final ExprNode expression;

    public InvestNode(ExprNode expression) {
        this.expression = expression;
    }

    @Override
    public boolean execute(Game game) {
        return game.invest(expression.eval(game));
    }
}
