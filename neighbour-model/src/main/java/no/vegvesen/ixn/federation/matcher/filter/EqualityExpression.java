package no.vegvesen.ixn.federation.matcher.filter;

import no.vegvesen.ixn.federation.matcher.Trilean;
import org.apache.qpid.server.filter.Expression;

import java.util.Objects;

public class EqualityExpression<E> extends ComparisonExpression<E> {
    public EqualityExpression(Expression<E> left, Expression<E> right) {
        super(left, right);
    }

    @Override
    public Object evaluate(E message) {
        Object lv = getLeft().evaluate(message);
        Object rv = getRight().evaluate(message);
        if (Objects.equals(lv, rv)) {
            return Trilean.TRUE;
        }
        if (lv == Trilean.UNKNOWN || rv == Trilean.UNKNOWN) {
            return Trilean.UNKNOWN;
        }
        if ((lv instanceof Comparable) && (rv instanceof Comparable)) {
            return compare((Comparable) lv, (Comparable) rv);
        }
        return Trilean.FALSE;
    }

    @Override
    protected boolean convertComparatorValueToBoolean(int answer) {
        return answer == 0;
    }

    @Override
    public String getExpressionSymbol() {
        return "=";
    }
}
