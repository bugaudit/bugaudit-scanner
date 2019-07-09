package me.shib.bugaudit.scanner;

import me.shib.bugaudit.commons.BugAuditContent;
import me.shib.bugaudit.commons.BugAuditException;

import java.util.HashSet;
import java.util.Set;

public final class Bug {

    private static final String bugAuditLabel = "BugAudit";

    private String title;
    private int priority;
    private BugAuditContent description;
    private Set<String> types;
    private Set<String> keys;
    private Set<String> tags;

    Bug(String title, int priority) throws BugAuditException {
        this.title = title;
        this.priority = priority;
        this.types = new HashSet<>();
        this.keys = new HashSet<>();
        this.tags = new HashSet<>();
        this.addKey(bugAuditLabel);
    }

    public void addType(String type) {
        this.types.add(type);
        this.addTag(type);
    }

    public void addKey(String key) throws BugAuditException {
        if (key == null || key.isEmpty()) {
            throw new BugAuditException("Null or Empty key cannot be processed");
        }
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

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public BugAuditContent getDescription() {
        return description;
    }

    public void setDescription(BugAuditContent description) {
        this.description = description;
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
