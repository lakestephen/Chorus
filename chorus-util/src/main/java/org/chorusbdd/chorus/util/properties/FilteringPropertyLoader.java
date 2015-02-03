package org.chorusbdd.chorus.util.properties;

import org.chorusbdd.chorus.util.function.BiPredicate;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * Created by GA2EBBU on 09/01/2015.
 *
 * Apply a function to convert the keys from a source Properties object into a Properties instance
 * with modified keys
 */
class FilteringPropertyLoader implements PropertyLoader {

    private PropertyLoader wrappedLoader;
    private BiPredicate<String,String> predicate;

    public FilteringPropertyLoader(PropertyLoader wrappedLoader, BiPredicate<String, String> predicate) {
        this.wrappedLoader = wrappedLoader;
        this.predicate = predicate;
    }

    public Properties loadProperties() {
        Properties p = wrappedLoader.loadProperties();
        Properties dest = new Properties();
        for ( Map.Entry<Object,Object> mapEntry : p.entrySet()) {
            if(predicate.test(mapEntry.getKey().toString(), mapEntry.getValue().toString())) {
                dest.put(mapEntry.getKey(), mapEntry.getValue());
            }
        }
        return dest;
    }
}
