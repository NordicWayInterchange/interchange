package no.vegvesen.ixn.federation.matcher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.matcher.filter.TrileanExpression;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * SelectorCapabilityMatcher developed by Monotch
 */
public class SelectorCapabilityMatcher {
    private final ObjectMapper mapper = new ObjectMapper();
    private final String[] quadKeyIdentifiers;

    public SelectorCapabilityMatcher() {
        this("quadTree");
    }

    public SelectorCapabilityMatcher(String... quadKeyIdentifiers) {
        this.quadKeyIdentifiers = quadKeyIdentifiers;
    }

    public boolean match(String selector, String capabilityJson) {
        if (selector.isEmpty()) {
            return true;
        }
        try {
            Map<String, Object> map = mapper.readValue(capabilityJson, Map.class);
            SelectorParser<Map<String, Object>> selectorParser = new SelectorParser<>();
            selectorParser.setPropertyExpressionFactory(value -> objectMap -> objectMap.get(value));
            TrileanExpression<Map<String, Object>> matcher = selectorParser.parse(selector);
            Map<String, Set<Object>> capabilityMultiValueMap = convertMapValuesToCollections(map);
            for (String quadKeyIdentifier : quadKeyIdentifiers) {
                if (capabilityMultiValueMap.containsKey(quadKeyIdentifier)) {
                    Pattern quadKeyRegexPattern = Pattern.compile("(?:'[\\%\\,]*,)(?<quadKey>[0-3]+)(?:[\\%\\,]*')", Pattern.CASE_INSENSITIVE);
                    Matcher regexMatcher = quadKeyRegexPattern.matcher(selector);
                    Set<String> quadKeysUsedInSelector = new HashSet<>();
                    while (regexMatcher.find()) {
                        quadKeysUsedInSelector.add(regexMatcher.group("quadKey"));
                    }
                    capabilityMultiValueMap.put(quadKeyIdentifier, expandQuadKeyValuesToIncludeSubQuadrantKeys(capabilityMultiValueMap.get(quadKeyIdentifier), quadKeysUsedInSelector));
                }
            }
            for (Iterator<Map<String, Object>> iterator = new CombinationExpandingIterator<>(capabilityMultiValueMap); iterator.hasNext(); ) {
                Map<String, Object> capabilityValuesMap = iterator.next();
                if (canThereBeASelectorMatchForTheseParameters(capabilityValuesMap, matcher)) {
                    return true;
                }
            }
            return false;
        } catch (JsonProcessingException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private static Set<Object> expandQuadKeyValuesToIncludeSubQuadrantKeys(Set<Object> quadKeysToExpand, Set<String> quadKeysToFind) {
        Set<Object> set = new HashSet<>(quadKeysToExpand);
        for (String quadKeyToFind : quadKeysToFind) {
            for (Object quadKey : quadKeysToExpand) {
                String quadKeyString = (String) quadKey;
                if (quadKeyToFind.startsWith(quadKeyString)) {
                    set.add(quadKeyToFind);
                }
            }
        }
        return set;
    }

    private static Map<String, Set<Object>> convertMapValuesToCollections(Map<String, Object> map) {
        return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> {
            if (e.getValue() instanceof Collection) {
                return new HashSet<>(((Collection<?>) e.getValue()));
            } else {
                return Collections.singleton(e.getValue());
            }
        }));
    }

    private static boolean canThereBeASelectorMatchForTheseParameters(Map<String, Object> map, TrileanExpression<Map<String, Object>> matcher) {
        return matcher.matches(map) != Trilean.FALSE;
    }
}

