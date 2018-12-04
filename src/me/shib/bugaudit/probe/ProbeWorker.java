package me.shib.bugaudit.probe;

import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ProbeWorker {

    private static final Reflections reflections = new Reflections("");

    private Lang lang;
    private Set<String> tools;
    private GitRepo repo;
    private List<PriorityFilter> priorityFilters;
    private List<SourceScan> sourceScans;

    public ProbeWorker(GitRepo repo, List<PriorityFilter> priorityFilters) {
        this.lang = Lang.getCurrentLang();
        this.tools = new HashSet<>();
        this.repo = repo;
        this.priorityFilters = priorityFilters;
        this.sourceScans = getScanners(this.lang);
    }

    public void audit() {
        List<PriorityFilter> shortlistedFilters = shortlistFilters(priorityFilters);
        for (SourceScan sourceScan : sourceScans) {
            sourceScan.scan();
            applyPriorityFilter(sourceScan, shortlistedFilters);
        }
    }

    public List<SourceScan> getSourceScans() {
        return sourceScans;
    }

    private List<PriorityFilter> shortlistFilters(List<PriorityFilter> priorityFilters) {
        List<PriorityFilter> filters = new ArrayList<>();
        for (PriorityFilter priorityFilter : priorityFilters) {
            if (priorityFilter.getLang() == lang || tools.contains(priorityFilter.getTool())) {
                filters.add(priorityFilter);
            }
        }
        return filters;
    }

    private void applyPriorityFilter(SourceScan sourceScan, List<PriorityFilter> filters) {
        for (Bug bug : sourceScan.getVulnerabilities()) {
            for (PriorityFilter filter : filters) {
                boolean langMatch = filter.getLang() == null || filter.getLang() == sourceScan.getLang();
                boolean toolMatch = filter.getTool() == null || filter.getTool().equalsIgnoreCase(sourceScan.getTool());
                boolean typeMatch = filter.getType() == null || bug.getTypes().contains(filter.getType());
                if (langMatch && toolMatch && typeMatch && (bug.getPriority() > filter.getPriority())) {
                    bug.setPriority(filter.getPriority());
                }
            }
        }
    }

    private List<SourceScan> getScanners(Lang lang) {
        List<SourceScan> sourceScans = new ArrayList<>();
        if (lang != null) {
            Set<Class<? extends SourceScan>> scannerClasses = reflections.getSubTypesOf(SourceScan.class);
            for (Class<? extends SourceScan> scannerClass : scannerClasses) {
                try {
                    Class<?> clazz = Class.forName(scannerClass.getName());
                    Constructor<?> ctor = clazz.getConstructor();
                    SourceScan sourceScan = (SourceScan) ctor.newInstance();
                    if (sourceScan.getLang() == lang) {
                        tools.add(sourceScan.getTool());
                        sourceScan.setRepo(repo);
                        sourceScans.add(sourceScan);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return sourceScans;
    }

}
