package no.vegvesen.ixn.federation.api.v1_0;
import no.vegvesen.ixn.federation.model.SubscriptionStatus;

public class SubscriptionApi{

	private String selector;
	private String path;
	private SubscriptionStatus status;

	public SubscriptionApi(){
	}

	public SubscriptionApi(String selector, String path, SubscriptionStatus status) {
		this.selector = selector;
		this.path = path;
		this.status = status;
	}

	public String getSelector() {
		return selector;
	}

	public void setSelector(String selector) {
		this.selector = selector;
	}

	@SuppressWarnings("WeakerAccess")
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public SubscriptionStatus getStatus() {
		return status;
	}

	public void setStatus(SubscriptionStatus status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "SubscriptionApi{" +
				"selector='" + selector + '\'' +
				", path='" + path + '\'' +
				", status='" + status + '\'' +
				'}';
	}
}
