package org.bonitasoft.engine.tracking;

public class Record {

    private final long timestamp;

    private final String name;

    private final String description;

    private final long duration;

    public Record(long timestamp, String name, String description, long duration) {
        super();
        this.timestamp = timestamp;
        this.name = name;
        this.description = description;
        this.duration = duration;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public long getDuration() {
        return duration;
    }

}
