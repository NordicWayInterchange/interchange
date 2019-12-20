package no.vegvesen.ixn.federation.model;


import no.vegvesen.ixn.federation.api.v1_0.SubscriptionStatus;
import no.vegvesen.ixn.properties.MessageProperty;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@Entity
@Table(name = "subscriptions")
public class Subscription {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sub_generator")
	@SequenceGenerator(name = "sub_generator", sequenceName = "sub_seq")
	@Column(name = "sub_id")
	private Integer sub_id;

	@Enumerated(EnumType.STRING)
	private SubscriptionStatus subscriptionStatus;

	private String selector;

	@ElementCollection(fetch = FetchType.EAGER)
	private Set<String> quadTreeTiles = new HashSet<>();

	private String path;

	private int numberOfPolls = 0;

	public Subscription() {
	}

	public Subscription(String selector, SubscriptionStatus subscriptionStatus) {
		this.selector = selector;
		this.subscriptionStatus = subscriptionStatus;
	}

	public SubscriptionStatus getSubscriptionStatus() {
		return subscriptionStatus;
	}

	public void setSubscriptionStatus(SubscriptionStatus status) {
		this.subscriptionStatus = status;
	}

	public String getSelector() {
		return selector;
	}

	public void setSelector(String selector) {
		this.selector = selector;
	}

	public Integer getId() {
		return sub_id;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getNumberOfPolls() {
		return numberOfPolls;
	}

	public void setNumberOfPolls(int numberOfPolls) {
		this.numberOfPolls = numberOfPolls;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (this == o) return true;
		if (!(o instanceof Subscription)) return false;

		Subscription that = (Subscription) o;

		return selector.equals(that.selector);
	}

	@Override
	public int hashCode() {
		return selector.hashCode();
	}

	@Override
	public String toString() {
		return "Subscription{" +
				"sub_id=" + sub_id +
				", subscriptionStatus=" + subscriptionStatus +
				", selector='" + selector + '\'' +
				", path='" + path + '\'' +
				'}';
	}

	public Set<String> getQuadTreeTiles() {
		return quadTreeTiles;
	}

	public void setQuadTreeTiles(Set<String> quadTreeTiles) {
		this.quadTreeTiles = quadTreeTiles;
	}

	public String getSelectorWithQuadTree() {
		String quadTreeSelector = this.quadTreeTilesSelector();
		if (quadTreeSelector != null) {
			if (selector == null) {
				return quadTreeSelector;
			}
			return String.format("(%s) and (%s)", this.getSelector(), quadTreeSelector);
		}
		return selector;
	}

	String quadTreeTilesSelector() {
		if (this.quadTreeTiles.isEmpty()) {
			return null;
		} else {
			StringBuilder quadTreeSelector = new StringBuilder();
			Iterator<String> quadIterator = quadTreeTiles.iterator();
			while (quadIterator.hasNext()) {
				String quadTreeTile = quadIterator.next();
				quadTreeSelector.append(MessageProperty.QUAD_TREE.getName());
				quadTreeSelector.append(" like '%,");
				quadTreeSelector.append(quadTreeTile);
				quadTreeSelector.append("%'");
				if (quadIterator.hasNext()) {
					quadTreeSelector.append(" or ");
				}
			}
			return quadTreeSelector.toString();
		}
	}
}
