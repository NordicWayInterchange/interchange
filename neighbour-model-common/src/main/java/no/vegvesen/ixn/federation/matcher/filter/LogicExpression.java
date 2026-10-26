/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.vegvesen.ixn.federation.matcher.filter;

import no.vegvesen.ixn.federation.matcher.Trilean;
import org.apache.qpid.server.filter.BinaryExpression;

/**
 * A filter performing a comparison of two objects
 * Three-valued version of org.apache.qpid.server.filter.LogicExpression. Comments from original.
 */
public abstract class LogicExpression<T> extends BinaryExpression<T> implements TrileanExpression<T> {

    public LogicExpression(TrileanExpression<T> left, TrileanExpression<T> right) {
        super(left, right);
    }

    public static <E> TrileanExpression<E> createOR(TrileanExpression<E> lvalue, TrileanExpression<E> rvalue) {
        return new OrExpression<>(lvalue, rvalue);
    }

    public static <E> TrileanExpression<E> createAND(TrileanExpression<E> lvalue, TrileanExpression<E> rvalue) {
        return new AndExpression<>(lvalue, rvalue);
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

