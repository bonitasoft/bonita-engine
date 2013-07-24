package org.bonitasoft.engine.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a Bonita API method does not need to have a valid session to be called
 * 
 * @author Emmanuel Duchastenier
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NoSessionRequired {

}
