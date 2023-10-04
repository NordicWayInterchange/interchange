package no.vegvesen.ixn.federation.matcher;

import java.util.*;

class CombinationExpandingIterator<T> implements Iterator<Map<String, T>> {
    private final Collection<T> values;
    private final String key;
    private final Iterator<Map<String, T>> nextInChain;
    private Iterator<T> localIterator;
    private Map<String, T> lastValueFromNextInChain;

    CombinationExpandingIterator(Map<String, ? extends Collection<T>> sourceMap) {
        Map<String, Collection<T>> sourceMapCopy = new HashMap<>(sourceMap);
        if (sourceMapCopy.isEmpty()) {
            localIterator = Collections.emptyIterator();
            nextInChain = Collections.emptyIterator();
            key = null;
            values = Collections.emptyList();
        } else {
            String firstKey = sourceMapCopy.keySet().iterator().next();
            values = sourceMapCopy.remove(firstKey);
            localIterator = values.iterator();
            key = firstKey;
            if (sourceMapCopy.isEmpty()) {
                nextInChain = Collections.emptyIterator();
                lastValueFromNextInChain = Collections.emptyMap();
            } else {
                nextInChain = new CombinationExpandingIterator<>(sourceMapCopy);
                lastValueFromNextInChain = nextInChain.next();
            }
        }
    }

    @Override
    public boolean hasNext() {
        return localIterator.hasNext() || nextInChain.hasNext();
    }

    @Override
    public Map<String, T> next() {
        if (!localIterator.hasNext()) {
            if (nextInChain.hasNext()) {
                localIterator = values.iterator();
                lastValueFromNextInChain = nextInChain.next();
            } else {
                throw new NoSuchElementException();
            }
        }
        Map<String, T> stringObjectHashMap = new HashMap<>();
        stringObjectHashMap.put(key, localIterator.next());
        stringObjectHashMap.putAll(lastValueFromNextInChain);
        return stringObjectHashMap;
    }
}
