package me.shib.bugaudit.probe;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public abstract class ProbeConfig {

    private static final String probeDirPathEnv = "BUGAUDIT_PROBE_DIR";

    private Map<String, Integer> priorityMap;
    private transient File probeDir;

    public ProbeConfig() {
        this.priorityMap = new HashMap<>();
        probeDir = makeProbeDir();

    }

    private File makeProbeDir() {
        String probeDirPath = System.getenv(probeDirPathEnv);
        if (probeDirPath != null && !probeDirPath.isEmpty()) {
            File dir = new File(probeDirPath);
            if (dir.exists() && dir.isDirectory()) {
                return dir;
            }
        }
        return new File(System.getenv("user.dir"));
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

    public File getProbeDir() {
        return probeDir;
    }

    public void addPriorityForType(String type, int priority) {
        getPriorityMap().put(type, priority);
    }

}
