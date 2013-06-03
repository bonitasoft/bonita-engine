package org.bonitasoft.engine.continuation;

import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.work.BonitaWork;

public class ListAdder extends BonitaWork {

    private final List<String> list;

    private final String toAdd;

    private final long delay;

    public ListAdder(final List<String> arrayList, final String toAdd, final long delay) {
        list = arrayList;
        this.toAdd = toAdd;
        this.delay = delay;
    }

    @Override
    protected void work() throws SBonitaException {
        try {
            Thread.sleep(delay);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        list.add(toAdd);
    }

}
