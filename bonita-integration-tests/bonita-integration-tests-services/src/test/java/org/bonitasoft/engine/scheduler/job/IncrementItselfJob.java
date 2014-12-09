package org.bonitasoft.engine.scheduler.job;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Baptiste Mesta
 */
public class IncrementItselfJob extends GroupJob {

    private static final long serialVersionUID = 3707724945060118636L;

    private static int value = 0;

    private static List<Date> executionDates = new ArrayList<Date>();

    @Override
    public void execute() {
        value++;
        addToExecutionDates(new Date(System.currentTimeMillis()));
    }

    public static int getValue() {
        return value;
    }

    public static void reset() {
        value = 0;
        executionDates = new ArrayList<Date>();
    }

    public static synchronized List<Date> getExecutionDates() {
        return new ArrayList<Date>(executionDates);
    }
    synchronized void addToExecutionDates(Date date) {
        executionDates.add(date);
    }

    @Override
    public String getDescription() {
        return "Increment itself ";
    }

    @Override
    public void setAttributes(final Map<String, Serializable> attributes) {
        super.setAttributes(attributes);
    }

}
