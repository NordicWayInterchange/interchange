package no.vegvesen.ixn.federation.dbhelper;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public interface DatabaseHelperInterface {

	@EventListener(ApplicationReadyEvent.class)
	void fillDatabase();
}
