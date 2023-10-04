package no.vegvesen.ixn.federation.matcher.filter;

import no.vegvesen.ixn.federation.matcher.Trilean;
import org.apache.qpid.server.filter.Expression;

import java.math.BigDecimal;

/**
 * Represents a constant expression
 * Three-valued version of org.apache.qpid.server.filter.ConstantExpression. Comments from original.
 */
public class ConstantExpression<T> implements Expression<T> {
    static class TrileanConstantExpression<E> extends ConstantExpression<E> implements TrileanExpression<E> {
        public TrileanConstantExpression(Object value) {
            super(value);
        }

        @Override
        public Trilean matches(E message) {
            Object object = evaluate(message);
            if (object instanceof Trilean) {
                return ((Trilean) object);
            } else {
                return Trilean.FALSE;
            }
        }
    }

    public static final TrileanConstantExpression NULL = new TrileanConstantExpression(null);
    public static final TrileanConstantExpression TRUE = new TrileanConstantExpression(Trilean.TRUE);
    public static final TrileanConstantExpression FALSE = new TrileanConstantExpression(Trilean.FALSE);
    private final Object _value;

    public static <E> ConstantExpression<E> NULL() {
        return NULL;
    }

    public static <E> ConstantExpression<E> TRUE() {
        return TRUE;
    }

    public static <E> ConstantExpression<E> FALSE() {
        return FALSE;
    }

    public static <E> ConstantExpression<E> createFromDecimal(String text) {
        // Strip off the 'l' or 'L' if needed.
        if (text.endsWith("l") || text.endsWith("L")) {
            text = text.substring(0, text.length() - 1);
        }
        Number value;
        try {
            value = Long.valueOf(text);
        } catch (NumberFormatException e) {
            // The number may be too big to fit in a long.
            value = new BigDecimal(text);
        }
        long l = value.longValue();
        if ((Integer.MIN_VALUE <= l) && (l <= Integer.MAX_VALUE)) {
            value = value.intValue();
        }
        return new ConstantExpression<>(value);
    }

    public static <E> ConstantExpression<E> createFromHex(String text) {
        Number value = Long.parseLong(text.substring(2), 16);
        long l = value.longValue();
        if ((Integer.MIN_VALUE <= l) && (l <= Integer.MAX_VALUE)) {
            value = value.intValue();
        }
        return new ConstantExpression<>(value);
    }

    public static <E> ConstantExpression<E> createFromOctal(String text) {
        Number value = Long.parseLong(text, 8);
        long l = value.longValue();
        if ((Integer.MIN_VALUE <= l) && (l <= Integer.MAX_VALUE)) {
            value = value.intValue();
        }
        return new ConstantExpression<>(value);
    }

    public static <E> ConstantExpression<E> createFloat(String text) {
        Number value = new Double(text);
        return new ConstantExpression<>(value);
    }

    public ConstantExpression(Object value) {
        this._value = value;
    }

    @Override
    public Object evaluate(T message) {
        return _value;
    }

    public Object getValue() {
        return _value;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        if (_value == null) {
            return "NULL";
        }
        if (_value instanceof Trilean) {
            return _value.toString();
        }
        if (_value instanceof String) {
            return encodeString((String) _value);
        }
        return _value.toString();
    }

    /**

     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object o) {
        if ((o == null) || !this.getClass().equals(o.getClass())) {
            return false;
        }
        return toString().equals(o.toString());
    }

    /**
     * Encodes the value of string so that it looks like it would look like
     * when it was provided in a selector.
     *
     * @param s string to encode
     * @return encoded string
     */
    public static String encodeString(String s) {
        StringBuilder b = new StringBuilder();
        b.append('\'');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\'') {
                b.append(c);
            }
            b.append(c);
        }
        b.append('\'');
        return b.toString();
    }
}
