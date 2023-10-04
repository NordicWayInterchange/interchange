package no.vegvesen.ixn.federation.matcher.filter;

import no.vegvesen.ixn.federation.matcher.Trilean;
import org.apache.qpid.server.filter.Expression;

/**
 * Three-valued version of org.apache.qpid.server.filter.BooleanExpression.
 */
public interface TrileanExpression<E> extends Expression<E> {
    Trilean matches(E object);
}
