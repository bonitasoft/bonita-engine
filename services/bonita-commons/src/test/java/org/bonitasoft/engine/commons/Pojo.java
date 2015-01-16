package org.bonitasoft.engine.commons;

import java.util.Date;
import java.util.List;

public class Pojo {

    private boolean choice;

    private Boolean bigChoice;

    private List<Boolean> bigChoices;

    private Date date;

    private List<Long> longs;

    public Pojo() {
        //
    }

    public boolean isChoice() {
        return choice;
    }

    public void setChoice(final boolean choice) {
        this.choice = choice;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(final Date date) {
        this.date = date;
    }

    public List<Long> getLongs() {
        return longs;
    }

    public void setLongs(final List<Long> longs) {
        this.longs = longs;
    }

    public Boolean getBigChoice() {
        return bigChoice;
    }

    public void setBigChoice(final Boolean bigChoice) {
        this.bigChoice = bigChoice;
    }

    public List<Boolean> getBigChoices() {
        return bigChoices;
    }

    public void setBigChoices(final List<Boolean> bigChoices) {
        this.bigChoices = bigChoices;
    }

    public String notAGetter() {
        return null;
    }

    public String twoParamMethod(final String a, final Integer i) {
        return String.format("%s*%d", a, i);
    }
}
