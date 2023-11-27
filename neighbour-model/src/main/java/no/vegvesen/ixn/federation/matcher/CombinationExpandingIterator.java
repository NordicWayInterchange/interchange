package no.vegvesen.ixn.federation.matcher;

import lombok.Value;
import java.util.*;

class CombinationExpandingIterator<T> implements Iterator<Map<String, T>> {
    private final Collection<T> ownValues;
    private final String ownKey;
    private final Iterator<Map<String, T>> nextIteratorInStack;
    private Iterator<T> ownValuesIterator;
    private Map<String, T> lastRetrievedValueFromNextIterator;

    CombinationExpandingIterator(Map<String, ? extends Collection<T>> sourceMap) {
        Map<String, Collection<T>> sourceMapCopy = new LinkedHashMap<>(sourceMap);
        KeyValuePair<T> keyValuePair = takeNextKeyValuePairFromHeadOfMap(sourceMapCopy);
        nextIteratorInStack = prepareAnIteratorForTheRemainingEntriesInTheMap(sourceMapCopy);
        lastRetrievedValueFromNextIterator = getInitialValueFromIterator(nextIteratorInStack);
        ownKey = keyValuePair.getKey();
        ownValues = keyValuePair.getValue();
        ownValuesIterator = ownValues.iterator();
    }

    private static <T> KeyValuePair<T> takeNextKeyValuePairFromHeadOfMap(Map<String, Collection<T>> sourceMapCopy) {
        String firstKey = null;
        Collection<T> value = Collections.emptyList();
        Iterator<Map.Entry<String, Collection<T>>> iterator = sourceMapCopy.entrySet().iterator();
        if (iterator.hasNext()) {
            Map.Entry<String, Collection<T>> next = iterator.next();
            firstKey = next.getKey();
            value = next.getValue();
            if (value.isEmpty()) {
                value = Collections.singleton(null);
            }
            iterator.remove();
        }
        return new KeyValuePair<>(firstKey, value);
    }

    private static <T> Iterator<Map<String, T>> prepareAnIteratorForTheRemainingEntriesInTheMap(Map<String, Collection<T>> sourceMapCopy) {
        if (sourceMapCopy.isEmpty()) {
            return Collections.emptyIterator();
        } else {
            return new CombinationExpandingIterator<>(sourceMapCopy);
        }
    }

    private static <T> Map<String, T> getInitialValueFromIterator(Iterator<Map<String, T>> mapIterator) {
        if (mapIterator.hasNext()) {
            return mapIterator.next();
        } else {
            return Collections.emptyMap();
        }
    }

    @Override
    public boolean hasNext() {
        return ownValuesIterator.hasNext() || nextIteratorInStack.hasNext();
    }

    @Override
    public Map<String, T> next() {
        if (!ownValuesIterator.hasNext()) {
            if (nextIteratorInStack.hasNext()) {
                ownValuesIterator = ownValues.iterator();
                lastRetrievedValueFromNextIterator = nextIteratorInStack.next();
            } else {
                throw new NoSuchElementException("No (more) elements at key " + ownKey);
            }
        }
        Map<String, T> stringObjectHashMap = new HashMap<>();
        stringObjectHashMap.put(ownKey, ownValuesIterator.next());
        stringObjectHashMap.putAll(lastRetrievedValueFromNextIterator);
        return stringObjectHashMap;
    }

    @Value
    private static class KeyValuePair<T> {
        String key;
        Collection<T> value;
    }
}
