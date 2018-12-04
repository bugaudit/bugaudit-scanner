package me.shib.bugaudit.probe;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Bug {

    private static final String sourceCureLabel = "SourceCure";

    private String title;
    private int priority;
    private Set<Content> descriptions;
    private Set<Content> references;
    private Set<String> types;
    private Set<String> keys;
    private Set<String> tags;

    public Bug(String title, int priority) {
        this.title = title;
        this.priority = priority;
        this.descriptions = new HashSet<>();
        this.references = new HashSet<>();
        this.types = new HashSet<>();
        this.keys = new HashSet<>();
        this.tags = new HashSet<>();
        addTag(sourceCureLabel);
    }

    public void addDescription(Content description) {
        this.descriptions.add(description);
    }

    public void addReference(Content reference) {
        this.references.add(reference);
    }

    public void addType(String type) {
        this.types.add(type);
        this.addTag(type);
    }

    public void addKey(String key) {
        this.keys.add(key);
        this.addTag(key);
    }

    public void addTag(String tag) {
        this.tags.add(tag);
    }

    public String getTitle() {
        return this.title;
    }

    public int getPriority() {
        return priority;
    }

    void setPriority(int priority) {
        this.priority = priority;
    }

    public List<Content> getDescriptions() {
        return new ArrayList<>(descriptions);
    }

    public List<Content> getReferences() {
        return new ArrayList<>(references);
    }

    public Set<String> getTypes() {
        return this.types;
    }

    public Set<String> getKeys() {
        return this.keys;
    }

    public Set<String> getTags() {
        return this.tags;
    }
}
