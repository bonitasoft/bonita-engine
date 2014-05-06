package org.bonitasoft.engine.persistence;

public class Book implements PersistentObject {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    String title;
    String author;
    long id;

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(final String author) {
        this.author = author;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getDiscriminator() {
        return null;
    }

    @Override
    public void setId(final long id) {
        this.id = id;
    }

    @Override
    public void setTenantId(final long id) {

    }

}
