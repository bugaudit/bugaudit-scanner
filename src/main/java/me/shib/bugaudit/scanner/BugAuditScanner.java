package me.shib.bugaudit.scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.shib.bugaudit.commons.BugAuditException;
import org.reflections.Reflections;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
    private transient Gson gson;
    private transient File scanDir;
    private BugAuditScanResult bugAuditScanResult;

    public BugAuditScanner() throws BugAuditException {
        BugAuditScannerConfig scannerConfig = getConfigFromFile();
        if (scannerConfig == null) {
            scannerConfig = getDefaultScannerConfig();
        }
        this.scanDir = calculateScanDir();
        this.bugAuditScanResult = new BugAuditScanResult(getTool(), getLang(), GitRepo.getRepo(), scannerConfig.getClassificationPriorityMap(), getScannerDirLabel());
        this.parserOnly = System.getenv(scannerParserOnlyEnv) != null && System.getenv(scannerParserOnlyEnv).equalsIgnoreCase("TRUE");
        this.gson = new GsonBuilder().create();
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

    public String runCommand(String command) throws IOException, InterruptedException {
        CommandRunner commandRunner;
        if (scanDir != null) {
            commandRunner = new CommandRunner(command, scanDir, getTool());
        } else {
            commandRunner = new CommandRunner(command, getTool());
        }
        commandRunner = new CommandRunner(command, getTool());
        return commandRunner.execute();
    }

    protected boolean isParserOnly() {
        return parserOnly;
    }

    private File calculateScanDir() {
        String scanDirPath = System.getenv(scannerDirPathEnv);
        String currentPath = System.getProperty("user.dir");
        if (scanDirPath != null && !scanDirPath.isEmpty() &&
                !scanDirPath.startsWith("/") && currentPath.endsWith(scanDirPath)) {
            File scanDir = new File(scanDirPath);
            if (scanDir.isDirectory()) {
                return scanDir;
            }
        }
        return null;
    }

    private String getScannerDirLabel() {
        if (scanDir != null) {
            return "bugaudit-scanpath-" + scanDir.getPath();
        }
        return null;
    }

    protected String getUrlForCVE(String cve) throws BugAuditException {
        if (cve != null && cve.toUpperCase().startsWith("CVE")) {
            return cveBaseURL + cve;
        }
        throw new BugAuditException("CVE provided is not valid");
    }

    private String readFromFile(File file) throws IOException {
        if (!file.exists() || file.isDirectory()) {
            return "";
        }
        StringBuilder contentBuilder = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
            contentBuilder.append(line).append("\n");
        }
        br.close();
        return contentBuilder.toString();
    }

    private BugAuditScannerConfig getConfigFromFile() {
        try {
            if (scannerConfigFilePath != null && !scannerConfigFilePath.isEmpty()) {
                File scannerConfigFile = new File(scannerConfigFilePath);
                if (scannerConfigFile.exists()) {
                    String json = readFromFile(scannerConfigFile);
                    Type type = new TypeToken<Map<String, BugAuditScannerConfig>>() {
                    }.getType();
                    Map<String, BugAuditScannerConfig> configMap = gson.fromJson(json, type);
                    return configMap.get(getTool());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public BugAuditScanResult getBugAuditScanResult() {
        return bugAuditScanResult;
    }

    protected abstract BugAuditScannerConfig getDefaultScannerConfig();

    protected abstract Lang getLang();

    public abstract String getTool();

    public abstract void scan() throws Exception;

}
