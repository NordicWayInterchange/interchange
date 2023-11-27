package no.vegvesen.ixn.federation.matcher.filter;

import no.vegvesen.ixn.federation.matcher.Trilean;
import org.apache.qpid.server.filter.ConstantExpression;
import org.apache.qpid.server.filter.Expression;
import org.apache.qpid.server.filter.SelectorParsingException;

import java.util.Objects;

public class EqualityExpression<E> extends ComparisonExpression<E> {
    public EqualityExpression(Expression<E> left, Expression<E> right) {
        super(left, right);
    }

    public static <E> TrileanExpression<E> createEqual(Expression<E> left, Expression<E> right) {
        checkEqualOperand(left);
        checkEqualOperand(right);
        checkEqualOperandCompatability(left, right);
        return new EqualityExpression<>(left, right);
    }

    private static <E> void checkEqualOperandCompatability(Expression<E> left, Expression<E> right) {
        if ((left instanceof ConstantExpression) && (right instanceof ConstantExpression)) {
            if ((left instanceof TrileanExpression) && !(right instanceof TrileanExpression)) {
                throw new SelectorParsingException("'" + left + "' cannot be compared with '" + right + "'");
            }
        }
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
