/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package no.vegvesen.ixn.federation.matcher.filter;

import no.vegvesen.ixn.federation.matcher.Trilean;
import org.apache.qpid.server.filter.Expression;

import java.math.BigDecimal;

import static java.lang.Double.parseDouble;

/**
 * Represents a constant expression
 * Three-valued version of org.apache.qpid.server.filter.ConstantExpression. Comments from original.
 */
public class ConstantExpression<T> implements Expression<T> {

    public static final TrileanConstantExpression NULL = new TrileanConstantExpression(null);
    public static final TrileanConstantExpression TRUE = new TrileanConstantExpression(Trilean.TRUE);
    public static final TrileanConstantExpression FALSE = new TrileanConstantExpression(Trilean.FALSE);
    private final Object _value;

    public ConstantExpression(Object value) {
        this._value = value;
    }

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
        Number value = parseDouble(text);
        return new ConstantExpression<>(value);
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
     * TODO: more efficient hashCode()
     *
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    /**
     * TODO: more efficient hashCode()
     *
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object o) {
        if ((o == null) || !this.getClass().equals(o.getClass())) {
            return false;
        }
        return toString().equals(o.toString());
    }

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
}
