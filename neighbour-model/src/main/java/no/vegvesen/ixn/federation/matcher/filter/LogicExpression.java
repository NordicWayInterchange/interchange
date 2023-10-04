package no.vegvesen.ixn.federation.matcher.filter;


import no.vegvesen.ixn.federation.matcher.Trilean;
import org.apache.qpid.server.filter.BinaryExpression;

/**
 * A filter performing a comparison of two objects
 * Three-valued version of org.apache.qpid.server.filter.LogicExpression. Comments from original.
 */
public abstract class LogicExpression<T> extends BinaryExpression<T> implements TrileanExpression<T> {
    public static <E> TrileanExpression<E> createOR(TrileanExpression<E> lvalue, TrileanExpression<E> rvalue) {
        return new OrExpression<>(lvalue, rvalue);
    }

    public static <E> TrileanExpression<E> createAND(TrileanExpression<E> lvalue, TrileanExpression<E> rvalue) {
        return new AndExpression<>(lvalue, rvalue);
    }

    public LogicExpression(TrileanExpression<T> left, TrileanExpression<T> right) {
        super(left, right);
    }

    @Override
    public abstract Object evaluate(T message);

    @Override
    public Trilean matches(T message) {
        Object object = evaluate(message);
        if (object instanceof Trilean) {
            return ((Trilean) object);
        } else {
            return Trilean.FALSE;
        }
    }

    private static class OrExpression<E> extends LogicExpression<E> {
        public OrExpression(final TrileanExpression<E> lvalue, final TrileanExpression<E> rvalue) {
            super(lvalue, rvalue);
        }

        @Override
        public Object evaluate(E message) {
            Trilean lv = (Trilean) getLeft().evaluate(message);
            if (lv == null || lv == Trilean.TRUE) {
                return lv;
            } else {
                Trilean rv = (Trilean) getRight().evaluate(message);
                if (rv == null || rv == Trilean.TRUE) {
                    return rv;
                } else {
                    if (lv == Trilean.UNKNOWN || rv == Trilean.UNKNOWN) {
                        return Trilean.UNKNOWN;
                    } else {
                        return Trilean.FALSE;
                    }
                }
            }
        }

        @Override
        public String getExpressionSymbol() {
            return "OR";
        }
    }

    private static class AndExpression<E> extends LogicExpression<E> {
        public AndExpression(final TrileanExpression<E> lvalue, final TrileanExpression<E> rvalue) {
            super(lvalue, rvalue);
        }

        @Override
        public Object evaluate(E message) {
            Trilean lv = (Trilean) getLeft().evaluate(message);
            if (lv == null || lv == Trilean.FALSE) {
                return lv;
            } else {
                Trilean rv = (Trilean) getRight().evaluate(message);
                if (rv == null || rv == Trilean.FALSE) {
                    return rv;
                } else {
                    if (lv == Trilean.UNKNOWN || rv == Trilean.UNKNOWN) {
                        return Trilean.UNKNOWN;
                    } else {
                        return Trilean.TRUE;
                    }
                }
            }
        }

        @Override
        public String getExpressionSymbol() {
            return "AND";
        }
    }
}

