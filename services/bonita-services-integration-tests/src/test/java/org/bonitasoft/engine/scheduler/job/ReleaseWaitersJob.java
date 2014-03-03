package org.bonitasoft.engine.scheduler.job;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Baptiste Mesta
 */
public class ReleaseWaitersJob extends GroupJob {

    private static final long serialVersionUID = 3707724945060118636L;

    private static JobSemaphore semaphore;

    private static String jobKey;

    @Override
    public void execute() {
        System.out.println("EXECUTE on " + new Date().getSeconds());
        if (semaphore != null) {
            semaphore.key = jobKey;
            semaphore.release();
        }
    }

    @Override
    public String getDescription() {
        return "release a semaphore";
    }

    @Override
    public void setAttributes(final Map<String, Serializable> attributes) {
        super.setAttributes(attributes);
        jobKey = (String) attributes.get("jobKey");
    }

    /*
     * create a new semphore and wait for the release to be called
     */
    public static void waitForJobToExecuteOnce() throws Exception {
        semaphore = new JobSemaphore(1);
        semaphore.acquire();
        boolean acquired = semaphore.tryAcquire(30, TimeUnit.SECONDS);
        if (!acquired) {
            throw new Exception("job was not triggered");
        }
    }

    public static void checkNotExecutedDuring(final int milliseconds) throws Exception {
        semaphore = new JobSemaphore(1);
        semaphore.acquire();
        boolean acquired = semaphore.tryAcquire(milliseconds, TimeUnit.MILLISECONDS);
        if (acquired) {
            throw new Exception("job " + jobKey + " was triggered");
        }
    }

}
