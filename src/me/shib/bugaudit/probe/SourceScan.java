package me.shib.bugaudit.probe;

import java.util.*;

public abstract class SourceScan {

    private Map<String, Bug> vulnerabilityMap;
    private GitRepo repo;

    public SourceScan() {
        this.vulnerabilityMap = new HashMap<>();
    }

    protected void addVulnerability(Bug bug) {
        bug.addTag(repo.toString());
        bug.addTag(getTool());
        bug.addTag(getLang().toString());
        StringBuilder key = new StringBuilder();
        List<String> keyList = new ArrayList<>(bug.getKeys());
        Collections.sort(keyList);
        for (String k : keyList) {
            key.append(k).append(";");
        }
        vulnerabilityMap.put(key.toString(), bug);
    }

    public GitRepo getRepo() {
        return repo;
    }

    void setRepo(GitRepo repo) {
        this.repo = repo;
    }

    public List<Bug> getVulnerabilities() {
        return new ArrayList<>(vulnerabilityMap.values());
    }

    public abstract Lang getLang();

    public abstract String getTool();

    protected abstract void scan();

}
