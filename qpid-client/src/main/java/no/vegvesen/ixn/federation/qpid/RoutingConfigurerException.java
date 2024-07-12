package no.vegvesen.ixn.federation.qpid;

@SuppressWarnings("WeakerAccess")
public class RoutingConfigurerException extends RuntimeException {
	public RoutingConfigurerException(Throwable e) {
		super (e);
	}

	public RoutingConfigurerException(String message) {
		super(message);
	}

	public RoutingConfigurerException(String errorMessage, Throwable e) {
		super(errorMessage, e);
	}
}
