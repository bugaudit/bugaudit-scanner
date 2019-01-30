package me.shib.bugaudit.probe;

import com.google.gson.reflect.TypeToken;
import me.shib.bugaudit.commons.GitRepo;
import me.shib.bugaudit.commons.Lang;
import me.shib.java.lib.jsonconfig.JsonConfig;
import org.reflections.Reflections;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.*;

public abstract class ProbeScaner {

    private static final String probeConfigFilePath = System.getenv("BUGAUDIT_PROBE_CONFIG");
    private static final Reflections reflections = new Reflections("me.shib.bugaudit.probe");

    private ProbeConfig probeConfig;
    private Map<String, Bug> bugMap;
    private GitRepo repo;

    public ProbeScaner() {
        this.probeConfig = getConfigFromFile();
        if (this.probeConfig == null) {
            this.probeConfig = getDefaultProbeConfig();
        }
        this.bugMap = new HashMap<>();
    }

    private static synchronized List<ProbeScaner> getScanners(GitRepo repo) {
        Lang lang = Lang.getCurrentLang();
        List<ProbeScaner> probeScaners = new ArrayList<>();
        if (lang != null) {
            Set<Class<? extends ProbeScaner>> scannerClasses = reflections.getSubTypesOf(ProbeScaner.class);
            for (Class<? extends ProbeScaner> scannerClass : scannerClasses) {
                try {
                    Class<?> clazz = Class.forName(scannerClass.getName());
                    Constructor<?> ctor = clazz.getConstructor();
                    ProbeScaner probeScaner = (ProbeScaner) ctor.newInstance();
                    if (probeScaner.getLang() == lang) {
                        probeScaner.setRepo(repo);
                        probeScaners.add(probeScaner);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return probeScaners;
    }

    public static synchronized List<Bug> bugsFromScanners() {
        List<Bug> bugs = new ArrayList<>();
        List<ProbeScaner> scanners = ProbeScaner.getScanners(GitRepo.getRepo());
        for (ProbeScaner scanner : scanners) {
            System.out.println("Now running scanner: " + scanner.getTool());
            scanner.scan();
            bugs.addAll(scanner.getBugs());
        }
        return bugs;
    }

    private ProbeConfig getConfigFromFile() {
        try {
            if (probeConfigFilePath != null && !probeConfigFilePath.isEmpty()) {
                File probeConfigFile = new File(probeConfigFilePath);
                if (probeConfigFile.exists()) {
                    JsonConfig jsonConfig = JsonConfig.getJsonConfig(probeConfigFile);
                    Type type = new TypeToken<Map<String, ProbeConfig>>() {
                    }.getType();
                    Map<String, ProbeConfig> configMap = jsonConfig.get(type);
                    return configMap.get(getTool());
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    protected abstract ProbeConfig getDefaultProbeConfig();

    protected ProbeConfig getProbeConfig() {
        return probeConfig;
    }

    protected void addBug(Bug bug) {
        bug.addTag(repo.toString());
        bug.addTag(getTool());
        bug.addTag(getLang().toString());
        StringBuilder key = new StringBuilder();
        List<String> keyList = new ArrayList<>(bug.getKeys());
        Collections.sort(keyList);
        for (String k : keyList) {
            key.append(k).append(";");
        }
        applyPriorityFilters(bug);
        bugMap.put(key.toString(), bug);
    }

    private void applyPriorityFilters(Bug bug) {
        Map<String, Integer> priorityMap = probeConfig.getPriorityMap();
        for (String type : priorityMap.keySet()) {
            if ((type == null || bug.getTypes().contains(type)) &&
                    (bug.getPriority() > priorityMap.get(type))) {
                bug.setPriority(priorityMap.get(type));
            }
        }
    }

    protected Bug newBug(String title, int priority) {
        return new Bug(title, priority);
    }

    public GitRepo getRepo() {
        return repo;
    }

    void setRepo(GitRepo repo) {
        this.repo = repo;
    }

    public List<Bug> getBugs() {
        return new ArrayList<>(bugMap.values());
    }

    public abstract Lang getLang();

    public abstract String getTool();

    protected abstract void scan();

}
