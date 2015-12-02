package org.bonitasoft.engine.test.annotation;

import java.lang.reflect.Field;

import org.bonitasoft.engine.test.TestEngine;

/**
 * @author Baptiste Mesta
 */
public class EngineInjector<T> {


    private TestEngine testEngine;
    private Field engineField;
    private Class<T> klass;

    public EngineInjector(Class<T> klass) {
        this.klass = klass;
    }

/*
    public void inject(Class<?> klass) {
        Field[] fields = klass.getDeclaredFields();
        for (Field field : fields) {
            System.out.println("========");
            System.out.println(field.getName());
            System.out.println(field.getType());
            for (Annotation annotation : field.getDeclaredAnnotations()) {
                System.out.println(annotation);
                System.out.println(annotation.annotationType());
                if (annotation.annotationType().equals(BusinessArchive.class)) {
                    //deploy
                    field.setAccessible(true);
                    businessArchives.put(field, ((BusinessArchive) annotation).resource());
                }
                if (annotation.annotationType().equals(Engine.class)) {
                    //deploy
                    field.setAccessible(true);
                    engineField = field;
                    Engine engineAnnotation = (Engine) annotation;
                    String type = engineAnnotation.type();
                    String url = engineAnnotation.url();
                    String name = engineAnnotation.name();

                    if (type == null || type.isEmpty() || type.equals("LOCAL")) {
                        engine = BonitaTestEngine.defaultLocalEngine();
                    } else if (type.equals("HTTP")) {
                        engine = BonitaTestEngine.remoteHttp(url, name);
                    }
                }
            }
        }
    }
*/
}
