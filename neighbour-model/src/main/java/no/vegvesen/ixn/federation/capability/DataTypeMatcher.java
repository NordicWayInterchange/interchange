package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.model.DataType;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
@Component
public class DataTypeMatcher {
	public static Set<DataType> calculateCommonInterest(Set<DataType> s1, Set<DataType> s2) {
		return s1.stream()
				.filter(dt1 -> s2.stream().anyMatch(dt1::matches))
				.collect(Collectors.toSet());
	}
}
