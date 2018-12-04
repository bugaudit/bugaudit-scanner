package me.shib.bugaudit.probe;

public final class PriorityFilter {

    private Lang lang;
    private String tool;
    private String type;
    private int priority;

    public PriorityFilter(Lang lang, String tool, String type, int priority) {
        this.lang = lang;
        this.tool = tool;
        this.type = type;
        this.priority = priority;
    }

    public Lang getLang() {
        return lang;
    }

    public String getTool() {
        return tool;
    }

    public String getType() {
        return type;
    }

    public int getPriority() {
        return priority;
    }
}
