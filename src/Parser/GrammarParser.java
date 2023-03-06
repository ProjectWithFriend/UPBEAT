package Parser;

import AST.*;
import AST.Node.*;
import Game.Direction;
import Tokenizer.Tokenizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static Parser.ParserException.*;

public class GrammarParser implements Parser {
    /* Grammar specification
    Plan → Statement+
    Statement → Command | BlockStatement | IfStatement | WhileStatement
    Command → AssignmentStatement | ActionCommand
    AssignmentStatement → <identifier> = Expression
    ActionCommand → done | relocate | MoveCommand | RegionCommand | AttackCommand
    MoveCommand → move Direction
    RegionCommand → invest Expression | collect Expression
    AttackCommand → shoot Direction Expression
    Direction → up | down | upleft | upright | downleft | downright
    BlockStatement → { Statement* }
    IfStatement → if ( Expression ) then Statement else Statement
    WhileStatement → while ( Expression ) Statement
    Expression → Expression + Term | Expression - Term | Term
    Term → Term * Factor | Term / Factor | Term % Factor | Factor
    Factor → Power ^ Factor | Power
    Power → <number> | <identifier> | ( Expression ) | InfoExpression
    InfoExpression → opponent | nearby Direction
     */

    private final Tokenizer tkz;
    private final List<String> commands = Arrays.stream(
            new String[]{"done", "relocate", "move", "invest", "collect", "shoot"}
    ).toList();
    private final List<String> reserved = Arrays.stream(
            new String[]{"collect", "done", "down", "downleft", "downright", "else", "if", "invest", "move", "nearby", "opponent", "relocate", "shoot", "then", "up", "upleft", "upright", "while"}
    ).toList();

    public GrammarParser(Tokenizer tkz) {
        if (!tkz.hasNext())
            throw new StatementRequired(tkz.getLine());
        this.tkz = tkz;
    }

    @Override
    public List<ExecNode> parse() {
        List<ExecNode> actions = parsePlan();
        if (tkz.hasNext())
            throw new ASTException.LeftoverTokenException(tkz.peek());
        return actions;
    }

    private List<ExecNode> parsePlan() {
        List<ExecNode> plan = new ArrayList<>();
        plan.add(parseStatement());
        parseStatements(plan);
        return plan;
    }

    private void parseStatements(List<ExecNode> list) {
        while (!tkz.peek("}") && tkz.hasNext()) {
            list.add(parseStatement());
        }
    }

    private ExecNode parseStatement() {
        if (tkz.peek("if")) {
            return parseIfStatement();
        } else if (tkz.peek("while")) {
            return parseWhileStatement();
        } else if (tkz.peek("{")) {
            return parseBlockStatement();
        } else {
            return parseCommand();
        }
    }

    private ExecNode parseBlockStatement() {
        List<ExecNode> nodes = new ArrayList<>();
        tkz.consume("{");
        parseStatements(nodes);
        tkz.consume("}");
        return new BlockNode(nodes);
    }

    private ExecNode parseWhileStatement() {
        tkz.consume("while");
        tkz.consume("(");
        ExprNode expression = parseExpression();
        tkz.consume(")");
        ExecNode statements = parseStatement();
        return new WhileNode(expression, statements);
    }

    private ExecNode parseIfStatement() {
        tkz.consume("if");
        tkz.consume("(");
        ExprNode expression = parseExpression();
        tkz.consume(")");
        tkz.consume("then");
        ExecNode trueStatement = parseStatement();
        tkz.consume("else");
        ExecNode falseStatement = parseStatement();
        return new IfElseNode(expression, trueStatement, falseStatement);
    }

    private ExecNode parseCommand() {
        if (commands.contains(tkz.peek()))
            return parseActionCommand();
        else
            return parseAssignmentStatement();
    }

    private ExecNode parseAssignmentStatement() {
        String identifier = parseIdentifier();
        if (tkz.peek("="))
            tkz.consume();
        else
            throw new CommandNotFound(identifier, tkz.getLine());
        ExprNode expression = parseExpression();
        return new AssignmentNode(identifier, expression);
    }

    private String parseIdentifier() {
        String identifier = tkz.consume();
        if (reserved.contains(identifier))
            throw new ReservedIdentifier(identifier, tkz.getLine());
        return identifier;
    }

    private ExecNode parseActionCommand() {
        String command = tkz.consume();
        return switch (command) {
            case "done" -> new DoneNode();
            case "relocate" -> new RelocateNode();
            case "move" -> parseMoveCommand();
            case "invest" -> parseInvestCommand();
            case "collect" -> parseCollectCommand();
            case "shoot" -> parseShootCommand();
            default -> throw new CommandNotImplemented(command, tkz.getLine());
        };
    }

    private ExecNode parseShootCommand() {
        Direction direction = parseDirection();
        ExprNode expression = parseExpression();
        return new AttackNode(expression, direction);
    }

    private ExecNode parseCollectCommand() {
        ExprNode expression = parseExpression();
        return new CollectNode(expression);
    }

    private ExecNode parseInvestCommand() {
        ExprNode expression = parseExpression();
        return new InvestNode(expression);
    }

    private ExprNode parseExpression() {
        ExprNode left = parseTerm();
        while (tkz.peek("+") || tkz.peek("-")) {
            String operator = tkz.consume();
            ExprNode right = parseTerm();
            left = new BinaryOperationNode(left, operator, right);
        }
        return left;
    }

    private ExprNode parseTerm() {
        ExprNode left = parseFactor();
        while (tkz.peek("*") || tkz.peek("/") || tkz.peek("%")) {
            String operator = tkz.consume();
            ExprNode right = parseFactor();
            left = new BinaryOperationNode(left, operator, right);
        }
        return left;
    }

    private ExprNode parseFactor() {
        ExprNode left = parsePower();
        if (tkz.peek("^")) {
            String operator = tkz.consume();
            ExprNode right = parseFactor();
            left = new BinaryOperationNode(left, operator, right);
        }
        return left;
    }

    private ExprNode parsePower() {
        if (Character.isDigit(tkz.peek().charAt(0))) {
            return new AtomicNode(Integer.parseInt(tkz.consume()));
        } else if (tkz.peek("opponent") || tkz.peek("nearby")) {
            return parseInfoExpression();
        } else if (tkz.peek("(")) {
            tkz.consume("(");
            ExprNode expr = parseExpression();
            tkz.consume(")");
            return expr;
        }
        return new AtomicNode(tkz.consume());
    }

    private ExprNode parseInfoExpression() {
        if (tkz.peek("opponent")) {
            tkz.consume();
            return new OpponentNode();
        } else if (tkz.peek("nearby")) {
            tkz.consume();
            Direction direction = parseDirection();
            return new NearbyNode(direction);
        } else {
            throw new InvalidInfoExpression(tkz.peek(), tkz.getLine());
        }
    }

    private ExecNode parseMoveCommand() {
        Direction direction = parseDirection();
        return new MoveNode(direction);
    }

    private Direction parseDirection() {
        String direction = tkz.consume();
        return switch (direction) {
            case "up" -> Direction.Up;
            case "down" -> Direction.Down;
            case "upleft" -> Direction.UpLeft;
            case "upright" -> Direction.UpRight;
            case "downleft" -> Direction.DownLeft;
            case "downright" -> Direction.DownRight;
            default -> throw new InvalidDirection(direction, tkz.getLine());
        };
    }
}
