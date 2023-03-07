package Parser;

import AST.Node;

import java.util.List;

public interface Parser {
    /**
     * parse given data to AST structure
     * @return first executable node of AST
     */
    List<Node.ExecNode> parse();
}
