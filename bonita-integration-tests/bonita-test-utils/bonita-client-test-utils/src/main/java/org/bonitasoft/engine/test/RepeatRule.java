package org.bonitasoft.engine.test;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * @author Laurent Leseigneur
 *         <p>use this rule to play x times the same junit test and add rule {@link Repeat}.</p>
 *         Example:<br>
 * <pre>
 * {@code
 * @Rule public RepeatRule repeatRule = new RepeatRule();
 * @Repeat(times = 100)
 * public void testName() throws Exception {
 *   ...
 * }
 * }
 * </pre>
 */
public class RepeatRule implements TestRule {

    private static class RepeatStatement extends Statement {

        private final int times;
        private final Statement statement;

        private RepeatStatement(final int times, final Statement statement) {
            this.times = times;
            this.statement = statement;
        }

        @Override
        public void evaluate() throws Throwable {
            for (int i = 0; i < times; i++) {
                statement.evaluate();
            }
        }
    }

    @Override
    public Statement apply(final Statement statement, final Description description) {
        Statement result = statement;
        final Repeat repeat = description.getAnnotation(Repeat.class);
        if (repeat != null) {
            final int times = repeat.times();
            result = new RepeatStatement(times, statement);
        }
        return result;
    }
}
