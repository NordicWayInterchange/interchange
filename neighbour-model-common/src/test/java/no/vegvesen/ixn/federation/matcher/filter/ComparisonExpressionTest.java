package no.vegvesen.ixn.federation.matcher.filter;

import no.vegvesen.ixn.federation.matcher.ParseException;
import no.vegvesen.ixn.federation.matcher.SelectorParser;
import no.vegvesen.ixn.federation.matcher.Trilean;
import org.apache.qpid.server.filter.PropertyExpression;
import org.apache.qpid.server.filter.PropertyExpressionFactory;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.fasterxml.jackson.core.type.TypeReference;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class ComparisonExpressionTest {

    @Test
    public void equalExpression() throws ParseException, IOException {
        String sql = "a";
        TypeReference<Map<String, Object>> typeRef = new TypeReference<>() { };
        Map<String,Object> map = new ObjectMapper().readValue(Path.of("src/test/resources/capability.json").toFile(),typeRef);

        SelectorParser<Map<String,Object>>  selectorParser = new SelectorParser<>();
        PropertyExpressionFactory<Map<String, Object>> expressionFactory = value -> new MapPropertyExpression(map, value);
        selectorParser.setPropertyExpressionFactory(expressionFactory);

        TrileanExpression<Map<String, Object>> expression = selectorParser.parse(sql);
        System.out.println(expression);

        System.out.println(expression.matches(map));

    }

    private static class MapPropertyExpression implements PropertyExpression<Map<String, Object>> {
        private final Map<String, Object> map;
        private final String value;

        public MapPropertyExpression(Map<String, Object> map, String value) {
            this.map = map;
            this.value = value;
        }

        @Override
        public Object evaluate(Map<String, Object> object) {
            return map.getOrDefault(value, Trilean.UNKNOWN);
        }

        @Override
        public String toString() {
            return  value;
        }


    }
}
