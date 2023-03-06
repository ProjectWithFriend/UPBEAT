package Parser;

import static AST.Node.ExecNode;

import Tokenizer.IterateTokenizer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static Parser.ParserException.*;

public class GrammarParserTest {
    public GrammarParser parser;
    public ExecNode node;

    @Test
    public void testEmptyStatement() {
        assertThrows(StatementRequired.class, () -> new GrammarParser(new IterateTokenizer(null)));
        assertThrows(StatementRequired.class, () -> new GrammarParser(new IterateTokenizer("")));
    }

    @Test
    public void testMultipleStatement() {
        parser = new GrammarParser(new IterateTokenizer(
                "t = 0 + 1  # keeping track of the turn number\n" + // t ??
                    "m = 0  # number of random moves\n" +
                    "while (deposit) { # still our region\n" +
                    "  if (deposit - 100)\n" +
                    "  then collect (deposit / 4)  # collect 1/4 of available deposit\n" +
                    "  else if (budget - 25) then invest 25\n" +
                    "  else {}\n" +
                    "  if (budget - 100) then {} else done  # too poor to do anything else\n" +
                    "  opponentLoc = opponent\n" +
                    "  if (opponentLoc / 10 - 1)\n" +
                    "  then  # opponent afar\n" +
                    "    if (opponentLoc % 10 - 5) then move downleft\n" +
                    "    else if (opponentLoc % 10 - 4) then move down\n" +
                    "    else if (opponentLoc % 10 - 3) then move downright\n" +
                    "    else if (opponentLoc % 10 - 2) then move downright\n" + // try to correct
                    "    else if (opponentLoc % 10 - 1) then move upright\n" +
                    "    else move up\n" +
                    "  else if (opponentLoc)\n" +
                    "  then  # opponent adjacent to city crew\n" +
                    "    if (opponentLoc % 10 - 5) then {\n" +
                    "      cost = 10 ^ (nearby upleft % 100 + 1)\n" +
                    "      if (budget - cost) then shoot upleft cost else {}\n" +
                    "    }\n" +
                    "    else if (opponentLoc % 10 - 4) then {\n" +
                    "      cost = 10 ^ (nearby downleft % 100 + 1)\n" +
                    "      if (budget - cost) then shoot downleft cost else {}\n" +
                    "    }\n" +
                    "    else if (opponentLoc % 10 - 3) then {\n" +
                    "      cost = 10 ^ (nearby down % 100 + 1)\n" +
                    "      if (budget - cost) then shoot down cost else {}\n" +
                    "    }\n" +
                    "    else if (opponentLoc % 10 - 2) then {\n" +
                    "      cost = 10 ^ (nearby downright % 100 + 1)\n" +
                    "      if (budget - cost) then shoot downright cost else {}\n" +
                    "    }\n" +
                    "    else if (opponentLoc % 10 - 1) then {\n" +
                    "      cost = 10 ^ (nearby upright % 100 + 1)\n" +
                    "      if (budget - cost) then shoot upright cost else {}\n" +
                    "    }\n" +
                    "    else {\n" +
                    "      cost = 10 ^ (nearby up % 100 + 1)\n" +
                    "      if (budget - cost) then shoot up cost else {}\n" +
                    "    }\n" +
                    "  else {  # no visible opponent; move in a random direction\n" +
                    "    dir = random % 6\n" +
                    "    if (dir - 4) then move upleft\n" +
                    "    else if (dir - 3) then move downleft\n" +
                    "    else if (dir - 2) then move down\n" +
                    "    else if (dir - 1) then move downright\n" +
                    "    else if (dir) then move upright\n" +
                    "    else move up\n" +
                    "    m = m + 1\n" +
                    "  }\n" +
                    "}  # end while\n" +
                    "# city crew on a region belonging to nobody, so claim it\n" +
                    "if (budget - 1) then invest 1 else {}\n"
        ));
        assertDoesNotThrow(parser::parse);
    }

    @Test
    public void testExpression() {
        parser = new GrammarParser(new IterateTokenizer("1+1"));
        assertThrows(CommandNotFound.class, parser::parse);
    }

    @Test
    public void testUnknown() {
        parser = new GrammarParser(new IterateTokenizer("wind"));
        assertThrows(CommandNotFound.class, parser::parse);
    }
    @Test
    public void testReservedWord() {
        parser = new GrammarParser(new IterateTokenizer("nearby=123"));
        assertThrows(ReservedIdentifier.class, parser::parse);
    }
}
