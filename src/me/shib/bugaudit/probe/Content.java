package me.shib.bugaudit.probe;

public final class Content {

    String name;
    String value;

    public Content(ContentType contentType, String value) {
        this.name = contentType.toString();
        this.value = value;
    }

    public Content(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public Content(String content) {
        this.name = null;
        this.value = content;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public enum ContentType {
        CodePath, URL, Code, Title
    }
}
