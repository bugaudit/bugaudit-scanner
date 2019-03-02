package me.shib.bugaudit.probe;

import com.google.gson.reflect.TypeToken;
import me.shib.bugaudit.commons.Bug;
import me.shib.bugaudit.commons.BugAuditResult;
import me.shib.bugaudit.commons.GitRepo;
import me.shib.bugaudit.commons.Lang;
import me.shib.java.lib.jsonconfig.JsonConfig;
import org.reflections.Reflections;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class ProbeScanner {

    private static final String probeConfigFilePath = System.getenv("BUGAUDIT_PROBE_CONFIG");
    private static final Reflections reflections = new Reflections(ProbeScanner.class.getPackage().getName());
    protected BugAuditResult bugAuditResult;
    private ProbeConfig probeConfig;
    private GitRepo repo;

    public ProbeScanner() {
        this.probeConfig = getConfigFromFile();
        if (this.probeConfig == null) {
            this.probeConfig = getDefaultProbeConfig();
        }
        this.bugAuditResult = new BugAuditResult(getTool(), getLang(), GitRepo.getRepo(), this.probeConfig.getPriorityMap());
    }

    private static synchronized List<ProbeScanner> getScanners(GitRepo repo) {
        List<ProbeScanner> probeScanners = new ArrayList<>();
        if (repo.getLang() != null) {
            Set<Class<? extends ProbeScanner>> scannerClasses = reflections.getSubTypesOf(ProbeScanner.class);
            for (Class<? extends ProbeScanner> scannerClass : scannerClasses) {
                try {
                    Class<?> clazz = Class.forName(scannerClass.getName());
                    Constructor<?> ctor = clazz.getConstructor();
                    ProbeScanner probeScanner = (ProbeScanner) ctor.newInstance();
                    if (probeScanner.getLang() == repo.getLang()) {
                        probeScanners.add(probeScanner);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return probeScanners;
    }

    public static synchronized List<BugAuditResult> getAuditResultsFromScanners() {
        List<BugAuditResult> auditResults = new ArrayList<>();
        List<ProbeScanner> scanners = ProbeScanner.getScanners(GitRepo.getRepo());
        for (ProbeScanner scanner : scanners) {
            System.out.println("Now running scanner: " + scanner.getTool());
            scanner.scan();
            auditResults.add(scanner.bugAuditResult);
        }
        return auditResults;
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

    private void applyPriorityFilters(Bug bug) {
        Map<String, Integer> priorityMap = probeConfig.getPriorityMap();
        for (String type : priorityMap.keySet()) {
            if ((type == null || bug.getTypes().contains(type)) &&
                    (bug.getPriority() > priorityMap.get(type))) {
                bug.setPriority(priorityMap.get(type));
            }
        }
    }

    protected abstract Lang getLang();

    protected abstract String getTool();

    protected abstract void scan();

}
