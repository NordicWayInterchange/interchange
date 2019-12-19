package no.vegvesen.ixn.federation.api.v1_0;

import java.util.Set;

public class SubscriptionApi{

	private String selector;
	private String path;
	private Set<String> quadTreeTiles;
	private SubscriptionStatus status;

	public SubscriptionApi(){
	}

	public SubscriptionApi(String selector, String path, Set<String> quadTreeTiles, SubscriptionStatus status) {
		this.selector = selector;
		this.path = path;
		this.quadTreeTiles = quadTreeTiles;
		this.status = status;
	}

	public String getSelector() {
		return selector;
	}

	public void setSelector(String selector) {
		this.selector = selector;
	}

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

	public Set<String> getQuadTreeTiles() {
		return quadTreeTiles;
	}

	public void setQuadTreeTiles(Set<String> quadTreeTiles) {
		this.quadTreeTiles = quadTreeTiles;
	}
}
