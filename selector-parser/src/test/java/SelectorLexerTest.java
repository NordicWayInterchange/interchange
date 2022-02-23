import no.vegvesen.ixn.selector.SelectorLexer;
import org.antlr.v4.runtime.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SelectorLexerTest {

    @Test
    public void testLexingSelector() {
        //TODO negative and other numeric literals
        String expression = "( TRUE  trUe anD FalSe 123";
        CommonTokenStream tokenStream = tokenizeExpression(expression);
        assertThat(tokenStream).isNotNull();
        for (Token token :tokenStream.getTokens()) {
            System.out.println(token);
        }

    }

    private CommonTokenStream tokenizeExpression(String expression) {
        SelectorLexer lexer = new SelectorLexer(CharStreams.fromString(expression));
        lexer.addErrorListener(new BaseErrorListener() {

            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                throw new RuntimeException(msg,e);
            }
        });
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        tokenStream.fill();
        return tokenStream;
    }
}
