package org.bonitasoft.engine.continuation;

import java.util.List;

import org.bonitasoft.engine.work.NonTxBonitaWork;

public class ListAdder extends NonTxBonitaWork {

    private final List<String> list;

    private final String toAdd;

    private final long delay;

    public ListAdder(final List<String> arrayList, final String toAdd, final long delay) {
        list = arrayList;
        this.toAdd = toAdd;
        this.delay = delay;
    }

    @Override
    protected void work() throws Exception {
        try {
            Thread.sleep(delay);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        list.add(toAdd);
    }

    @Override
    protected String getDescription() {
        return getClass().getSimpleName() + ": Adding " + toAdd + " to " + list.toString() + " after (ms) " + delay;
    }
}
