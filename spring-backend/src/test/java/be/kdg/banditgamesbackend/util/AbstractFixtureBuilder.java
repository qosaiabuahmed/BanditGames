package be.kdg.banditgamesbackend.util;

import org.instancio.*;

import java.util.HashMap;
import java.util.Map;

import static org.instancio.Select.field;

public abstract class AbstractFixtureBuilder<T, S extends AbstractFixtureBuilder<T, S>> {

    private final Map<TargetSelector, Object> fieldValues = new HashMap<>();

    protected S setField(GetMethodSelector<T, ?> methodReference, Object value) {
        return set(field(methodReference), value);
    }

    protected S ignoreField(GetMethodSelector<T, ?> methodReference) {
        return ignore(field(methodReference));
    }

    protected S set(TargetSelector selector, Object value) {
        if (fieldValues.containsKey(selector)) {
            fieldValues.replace(selector, value);
        } else {
            fieldValues.put(selector, value);
        }
        return self();
    }

    protected S ignore(Selector selector) {
        fieldValues.put(selector, null);
        return self();
    }

    protected final T buildInternal(Model<T> model) {
        InstancioApi<T> instancioApi = Instancio.of(model);
        fieldValues.forEach(instancioApi::set);
        return instancioApi.create();
    }

    public abstract T build();
    public abstract S self();
}
