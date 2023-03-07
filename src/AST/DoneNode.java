package AST;

import Game.Game;

import static AST.Node.*;

public class DoneNode extends ExecNode {
    @Override
    public boolean execute(Game game) {
        return false;
    }
}
