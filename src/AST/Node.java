package AST;

import Game.Game;

public abstract class Node {
    public abstract static class ExprNode extends Node {
        /**
         * evaluate node for a result
         *
         * @param game identifiers for evaluation (variables)
         * @return result of evaluation
         */
        public abstract long eval(Game game);
        public abstract String toString();
    }

    public abstract static class ExecNode extends Node {
        public ExecNode next;

        /**
         * execute a node and maybe change state of execution
         *
         * @param game current state of game
         */
        public abstract boolean execute(Game game);
    }
}

