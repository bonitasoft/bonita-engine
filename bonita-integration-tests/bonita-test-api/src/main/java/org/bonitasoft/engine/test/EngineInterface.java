package org.bonitasoft.engine.test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author mazourd
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface EngineInterface {

    String user() default "defaultUser";
    String password() default "defaultPassword";
}
