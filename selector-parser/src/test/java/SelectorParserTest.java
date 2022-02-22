import no.vegvesen.ixn.selector.SelectorLexer;
import no.vegvesen.ixn.selector.SelectorParser;
import org.antlr.v4.runtime.*;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;

public class SelectorParserTest {

    @Test
    public void testBooleanLiteral() {
        SelectorParser.ExprContext expr = parseExpressionString("TRUE");
        assertThat(expr).isNotNull();
        expr = parseExpressionString("FALSE");
        //this is where I can start checking the children, either directly, or using visitors
    }

    @Test
    public void testBooleanLiteralInBrace() {
        SelectorParser.ExprContext expr = parseExpressionString("(true )");
    }

    private SelectorParser.ExprContext parseExpressionString(String expression) {
        SelectorLexer lexer = new SelectorLexer(CharStreams.fromString(expression));
        lexer.addErrorListener(new BaseErrorListener() {

            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                throw new RuntimeException(msg,e);
            }
        });
        SelectorParser parser = new SelectorParser(new CommonTokenStream(lexer));
        parser.addErrorListener(new BaseErrorListener() {

            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                throw new RuntimeException("Syntax error, " + offendingSymbol + ", at " + line + ", " + charPositionInLine);
            }
        });
        SelectorParser.ExprContext expr = parser.expr();
        return expr;
    }

}
