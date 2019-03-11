package me.shib.bugaudit.scanner;

import java.util.HashMap;
import java.util.Map;

public abstract class BugAuditScannerConfig {

    private Map<String, Integer> classificationPriorityMap;

    public BugAuditScannerConfig() {
        this.classificationPriorityMap = new HashMap<>();
    }

    protected abstract Map<String, Integer> getDefaultClassificationPriorityMap();

    public Map<String, Integer> getClassificationPriorityMap() {
        if (classificationPriorityMap == null) {
            classificationPriorityMap = getDefaultClassificationPriorityMap();
            if (classificationPriorityMap == null) {
                classificationPriorityMap = new HashMap<>();
            }
        }
        return classificationPriorityMap;
    }

    public void addPriorityForClassification(String type, int priority) {
        getClassificationPriorityMap().put(type, priority);
    }

}
