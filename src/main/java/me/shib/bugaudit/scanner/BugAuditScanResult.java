package me.shib.bugaudit.scanner;

import me.shib.bugaudit.commons.BugAuditException;

import java.util.*;

public class BugAuditScanResult {

    private static final String bugAuditLabel = "BugAudit";

    private String tool;
    private Lang lang;
    private GitRepo repo;
    private Map<String, Integer> priorityMap;
    private Set<String> keys;
    private Map<String, Bug> bugMap;
    private String scanPath;

    public BugAuditScanResult(String tool, Lang lang, GitRepo repo, Map<String, Integer> priorityMap, String scanPath) {
        this.tool = tool;
        this.lang = lang;
        this.repo = repo;
        this.priorityMap = priorityMap;
        if (this.priorityMap == null) {
            this.priorityMap = new HashMap<>();
        }
        this.scanPath = scanPath;
        this.keys = new HashSet<>();
        this.bugMap = new HashMap<>();
    }

    public String getBugAuditLabel() {
        return bugAuditLabel;
    }

    private void applyPriorityFilters(Bug bug) {
        for (String type : priorityMap.keySet()) {
            if ((type == null || bug.getTypes().contains(type)) &&
                    (bug.getPriority() > priorityMap.get(type))) {
                bug.setPriority(priorityMap.get(type));
            }
        }
    }

    public Bug newBug(String title, int priority) throws BugAuditException {
        return new Bug(title, priority);
    }

    public void addBug(Bug bug) throws BugAuditException {
        bug.addKey(repo.toString());
        bug.addKey(tool);
        bug.addKey(lang.toString());
        if (scanPath != null && !scanPath.isEmpty()) {
            bug.addKey(scanPath);
        }
        bug.addKey(lang.toString());
        StringBuilder key = new StringBuilder();
        List<String> keyList = new ArrayList<>(bug.getKeys());
        Collections.sort(keyList);
        for (String k : keyList) {
            key.append(k).append(";");
        }
        applyPriorityFilters(bug);
        bugMap.put(key.toString(), bug);
    }

    public void addKey(String key) {
        keys.add(key);
    }

    public List<String> getKeys() {
        return new ArrayList<>(keys);
    }

    public String getTool() {
        return tool;
    }

    public Lang getLang() {
        return lang;
    }

    public GitRepo getRepo() {
        return repo;
    }

    public List<Bug> getBugs() {
        return new ArrayList<>(bugMap.values());
    }
}
