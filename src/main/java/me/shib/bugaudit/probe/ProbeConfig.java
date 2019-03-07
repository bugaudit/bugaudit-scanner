package me.shib.bugaudit.probe;

import java.util.HashMap;
import java.util.Map;

public abstract class ProbeConfig {

    private static final String probeDirPathEnv = "BUGAUDIT_PROBE_DIR";

    private Map<String, Integer> priorityMap;
    private transient String probeDirPath;

    public ProbeConfig() {
        this.priorityMap = new HashMap<>();
        this.probeDirPath = makeProbeDir();

    }

    private String makeProbeDir() {
        String path = System.getenv(probeDirPathEnv);
        String currentPath = System.getenv("user.dir");
        if (path == null || path.isEmpty() || !currentPath.endsWith(path)) {
            return null;
        }
        return path;
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

    public String getProbeDirPath() {
        return probeDirPath;
    }

    public void addPriorityForType(String type, int priority) {
        getPriorityMap().put(type, priority);
    }

}
