package AST;

import Game.Game;

public class WhileNode extends ConditionalNode {
    private int executionCount = 0;

    public WhileNode(ExprNode expression, ExecNode statements) {
        super(expression, statements, null);
        if (trueNode == null)
            trueNode = this;
    }

    @Override
    public boolean execute(Game game) {
        if (super.condition.eval(game) > 0 && executionCount < 10000) {
            executionCount++;
            if (!trueNode.execute(game))
                return false;
            return execute(game);
        }
        return true;
    }
}
