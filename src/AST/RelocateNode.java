package AST;

import Game.Game;

import static AST.Node.*;

public class RelocateNode extends ExecNode {
    @Override
    public boolean execute(Game game) {
        game.relocate();
        return true;
    }
}
