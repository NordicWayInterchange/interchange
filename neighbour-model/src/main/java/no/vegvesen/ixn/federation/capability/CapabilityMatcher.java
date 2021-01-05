package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.DatexCapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.DenmCapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.IviCapabilityApi;
import no.vegvesen.ixn.properties.MessageProperty;
import org.apache.qpid.server.filter.JMSSelectorFilter;
import org.springframework.stereotype.Component;

import java.util.*;

@SuppressWarnings("WeakerAccess")
@Component
public class CapabilityMatcher {
	public static Set<JMSSelectorFilter> calculateCommonInterest(Set<CapabilityApi> capabilityApis, Set<JMSSelectorFilter> selectorFilters) {
		Set<JMSSelectorFilter> match = new HashSet<>();
		for (CapabilityApi capabilityApi : capabilityApis) {
			List<CapabilityFilter> capabilitiesFlatAsFilters = getCapabilitiesFlatAsFilters(capabilityApi);
			for (CapabilityFilter capabilityFlatFilter : capabilitiesFlatAsFilters) {
				for (JMSSelectorFilter selectorFilter : selectorFilters) {
					if (selectorFilter.matches(capabilityFlatFilter)) {
						match.add(selectorFilter);
					}
				}
			}
		}
		return match;
	}

	private static List<CapabilityFilter> getCapabilitiesFlatAsFilters(CapabilityApi capabilityApi) {
		Map<String, String> values = capabilityApi.getValues();
		List<CapabilityFilter> capabilitiesFlat = new LinkedList<>();
		for (String quad : capabilityApi.getQuadTree()) {
			if (capabilityApi instanceof DenmCapabilityApi) {
				for (String cause : ((DenmCapabilityApi) capabilityApi).getCauseCode()) {
					Map<String, Object> singleCapability = new HashMap<>(values);
					singleCapability.put(MessageProperty.QUAD_TREE.getName(), quad);
					singleCapability.put(MessageProperty.CAUSE_CODE.getName(), cause);
					capabilitiesFlat.add(new CapabilityFilter(singleCapability));
				}
			}
			else if (capabilityApi instanceof DatexCapabilityApi) {
				for (String publicationType : ((DatexCapabilityApi) capabilityApi).getPublicationType()) {
					Map<String, Object> singleCapability = new HashMap<>(values);
					singleCapability.put(MessageProperty.QUAD_TREE.getName(), quad);
					singleCapability.put(MessageProperty.PUBLICATION_TYPE.getName(), publicationType);
					capabilitiesFlat.add(new CapabilityFilter(singleCapability));
				}

			}
			else if (capabilityApi instanceof IviCapabilityApi) {
				for (String iviType : ((IviCapabilityApi) capabilityApi).getIviType()) {
					Map<String, Object> singleCapability = new HashMap<>(values);
					singleCapability.put(MessageProperty.QUAD_TREE.getName(), quad);
					singleCapability.put(MessageProperty.IVI_TYPE.getName(), iviType);
					capabilitiesFlat.add(new CapabilityFilter(singleCapability));
				}
			}
		}
		return capabilitiesFlat;
	}

	private boolean matchesCapabilitiesFlat(List<CapabilityFilter> capabilitiesFlat, JMSSelectorFilter selector) {
		Optional<CapabilityFilter> datex2MatchesDenm = capabilitiesFlat.stream().filter(selector::matches).findAny();
		return datex2MatchesDenm.isPresent();
	}

}
