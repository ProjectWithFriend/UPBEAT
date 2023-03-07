package AST;

import Game.Game;

import java.util.List;

import AST.Node.ExecNode;


public class BlockNode extends ExecNode {
    private final List<ExecNode> nodes;

    public BlockNode(List<ExecNode> nodes) {
        this.nodes = nodes;
    }

    @Override
    public boolean execute(Game game) {
        for (ExecNode node : nodes) {
            if (!node.execute(game))
                return false;
        }
        return true;
    }
}
