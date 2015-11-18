package io.github.drmashu.buri.handlebars;

import com.github.jknack.handlebars.ValueResolver;
import io.github.drmashu.dikon.Container;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by drmashu on 2015/11/08.
 */
public class DikonResolver implements ValueResolver {

    private Container container;
    public DikonResolver(Container container) {
        this.container = container;
    }

    @SuppressWarnings({"rawtypes", "unchecked" })
    @Override
    public Object resolve(final Object context, final String name) {
        Object value = this.container.get(name);
        return value == null ? UNRESOLVED : value;
    }

    @Override
    public Object resolve(final Object context) {
        return context;
    }

    @SuppressWarnings({"unchecked", "rawtypes" })
    @Override
    public Set<Map.Entry<String, Object>> propertySet(final Object context) {
        if (context instanceof Map) {
            return ((Map) context).entrySet();
        }
        return Collections.emptySet();
    }
}
