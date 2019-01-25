package me.shib.bugaudit.probe;

import java.util.HashMap;
import java.util.Map;

public abstract class ProbeConfig {

    private Map<String, Integer> priorityMap;

    public ProbeConfig() {
        this.priorityMap = new HashMap<>();
    }

    protected abstract Map<String, Integer> getDefaultPriorityMap();

    public Map<String, Integer> getPriorityMap() {
        if (priorityMap == null) {
            priorityMap = getDefaultPriorityMap();
            if (priorityMap == null) {
                priorityMap = new HashMap<>();
            }
        }
        return priorityMap;
    }

    public void addPriorityForType(String type, int priority) {
        getPriorityMap().put(type, priority);
    }

}
