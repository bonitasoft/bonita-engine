package org.bonitasoft.engine.dependency.model;

public class DependencyContent {

    private String fileName;
    private byte[] content;

    public DependencyContent(String fileName, byte[] content) {
        this.fileName = fileName;
        this.content = content;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
