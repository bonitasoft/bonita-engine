package com.bonitasoft.engine.api.impl.transaction.expression.fix;

import java.lang.reflect.Constructor;

import com.bonitasoft.engine.bdm.Entity;


public class EntityCloner {

    @SuppressWarnings("unchecked")
    public static <T extends Entity> T copy(final T entity) {
        try {
            final Constructor<? extends Entity> constructor = entity.getClass().getConstructor(entity.getClass());
            return (T) constructor.newInstance(entity);
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
