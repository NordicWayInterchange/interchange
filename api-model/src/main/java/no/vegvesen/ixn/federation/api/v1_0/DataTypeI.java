package no.vegvesen.ixn.federation.api.v1_0;

import java.util.Collection;
import java.util.Set;

public interface DataTypeI {
	String getOriginatingCountry();

	void setOriginatingCountry(String originatingCountry);

	String getMessageType();

	void setMessageType(String messageType);

	Set<String> getQuadTree();

	void setQuadTree(Collection<String> quadTree);

	String getPublisherId();

	void setPublisherId(String publisherId);

	String getPublisherName();

	void setPublisherName(String publisherName);
}
