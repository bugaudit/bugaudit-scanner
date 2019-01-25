/*
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
    private List<ProbeScaner> probeScaners;

    public ProbeWorker(GitRepo repo) {
        this.lang = Lang.getCurrentLang();
        this.tools = new HashSet<>();
        this.repo = repo;
        this.probeScaners = getScanners(this.lang);
    }

    public void audit() {
        for (ProbeScaner probeScaner : probeScaners) {
            probeScaner.scan();
        }
    }

    public List<ProbeScaner> getProbeScaners() {
        return probeScaners;
    }

}
*/
