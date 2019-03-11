package me.shib.bugaudit.scanner;

import com.google.gson.reflect.TypeToken;
import me.shib.bugaudit.commons.BugAuditException;
import me.shib.java.lib.jsonconfig.JsonConfig;
import org.reflections.Reflections;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class BugAuditScanner {

    private static final String scannerParserOnlyEnv = "BUGAUDIT_SCANNER_PARSERONLY";
    private static final String scannerToolEnv = "BUGAUDIT_SCANNER_TOOL";
    private static final String scannerDirPathEnv = "BUGAUDIT_SCANNER_DIR";
    private static final String scannerConfigFilePath = System.getenv("BUGAUDIT_SCANNER_CONFIG");
    private static final String cveBaseURL = "https://nvd.nist.gov/vuln/detail/";
    private static final Reflections reflections = new Reflections(BugAuditScanner.class.getPackage().getName());
    private transient boolean parserOnly;
    private BugAuditScanResult bugAuditScanResult;

    public BugAuditScanner() {
        ScannerConfig scannerConfig = getConfigFromFile();
        if (scannerConfig == null) {
            scannerConfig = getDefaultScannerConfig();
        }
        this.bugAuditScanResult = new BugAuditScanResult(getTool(), getLang(), GitRepo.getRepo(), scannerConfig.getClassificationPriorityMap(), makeScannerDir());
        this.parserOnly = System.getenv(scannerParserOnlyEnv) != null && System.getenv(scannerParserOnlyEnv).equalsIgnoreCase("TRUE");
    }

    public static synchronized List<BugAuditScanner> getScanners(GitRepo repo) {
        String scannerToolName = System.getenv(scannerToolEnv);
        List<BugAuditScanner> bugAuditScanners = new ArrayList<>();
        if (repo.getLang() != null) {
            Set<Class<? extends BugAuditScanner>> scannerClasses = reflections.getSubTypesOf(BugAuditScanner.class);
            for (Class<? extends BugAuditScanner> scannerClass : scannerClasses) {
                try {
                    Class<?> clazz = Class.forName(scannerClass.getName());
                    Constructor<?> constructor = clazz.getConstructor();
                    BugAuditScanner bugAuditScanner = (BugAuditScanner) constructor.newInstance();
                    if (bugAuditScanner.getLang() == repo.getLang()) {
                        if (scannerToolName == null || scannerToolName.isEmpty() ||
                                scannerToolName.equalsIgnoreCase(bugAuditScanner.getTool())) {
                            bugAuditScanners.add(bugAuditScanner);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return bugAuditScanners;
    }

    protected boolean isParserOnly() {
        return parserOnly;
    }

    private String makeScannerDir() {
        String path = System.getenv(scannerDirPathEnv);
        String currentPath = System.getProperty("user.dir");
        if (path == null || path.isEmpty() || !currentPath.endsWith(path)) {
            return null;
        }
        path = "scanpath-" + path;
        return path;
    }

    protected String getUrlForCVE(String cve) throws BugAuditException {
        if (cve != null && cve.toUpperCase().startsWith("CVE")) {
            return cveBaseURL + cve;
        }
        throw new BugAuditException("CVE provided is not valid");
    }

    private ScannerConfig getConfigFromFile() {
        try {
            if (scannerConfigFilePath != null && !scannerConfigFilePath.isEmpty()) {
                File scannerConfigFile = new File(scannerConfigFilePath);
                if (scannerConfigFile.exists()) {
                    JsonConfig jsonConfig = JsonConfig.getJsonConfig(scannerConfigFile);
                    Type type = new TypeToken<Map<String, ScannerConfig>>() {
                    }.getType();
                    Map<String, ScannerConfig> configMap = jsonConfig.get(type);
                    return configMap.get(getTool());
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    public BugAuditScanResult getBugAuditScanResult() {
        return bugAuditScanResult;
    }

    protected abstract ScannerConfig getDefaultScannerConfig();

    protected abstract Lang getLang();

    public abstract String getTool();

    public abstract void scan() throws Exception;

}
