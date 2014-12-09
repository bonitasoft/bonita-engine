package org.bonitasoft.engine.test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Laurent Leseigneur
 *         <p>use this rule to play x times the same junit test and add rule {@link RepeatRule}.</p>
 *         Example:<br>
 *        <pre>
 * {@code
 * @Rule public RepeatRule repeatRule = new RepeatRule();
 * @Repeat(times = 100)
 *               public void testName() throws Exception {
 *               ...
 *               }
 *               }
 *               </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({
    java.lang.annotation.ElementType.METHOD
})
public @interface Repeat {

    public abstract int times();
}
