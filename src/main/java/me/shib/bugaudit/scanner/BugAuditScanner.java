package me.shib.bugaudit.scanner;

import me.shib.bugaudit.commons.BugAuditException;
import org.reflections.Reflections;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class BugAuditScanner {

    private static final String scannerParserOnlyEnv = "BUGAUDIT_SCANNER_PARSERONLY";
    private static final String scannerToolEnv = "BUGAUDIT_SCANNER_TOOL";
    private static final String scannerDirPathEnv = "BUGAUDIT_SCAN_DIR";
    private static final String bugAuditBuildScriptEnv = "BUGAUDIT_BUILD_SCRIPT";
    private static final String cveBaseURL = "https://nvd.nist.gov/vuln/detail/";
    private static final Reflections reflections = new Reflections(BugAuditScanner.class.getPackage().getName());

    private static transient File scanDir;

    static {
        scanDir = calculateScanDir();
    }

    private transient boolean parserOnly;
    private transient BugAuditScanResult bugAuditScanResult;

    public BugAuditScanner() throws BugAuditException {
        this.bugAuditScanResult = new BugAuditScanResult(getTool(), getLang(), GitRepo.getRepo(), getScannerDirLabel());
        this.parserOnly = System.getenv(scannerParserOnlyEnv) != null && System.getenv(scannerParserOnlyEnv).equalsIgnoreCase("TRUE");
    }

    public static synchronized List<BugAuditScanner> getScanners(Lang lang) {
        String specifiedToolName = System.getenv(scannerToolEnv);
        List<BugAuditScanner> bugAuditScanners = new ArrayList<>();
        if (lang != null) {
            Set<Class<? extends BugAuditScanner>> scannerClasses = reflections.getSubTypesOf(BugAuditScanner.class);
            for (Class<? extends BugAuditScanner> scannerClass : scannerClasses) {
                try {
                    Class<?> clazz = Class.forName(scannerClass.getName());
                    Constructor<?> constructor = clazz.getConstructor();
                    BugAuditScanner bugAuditScanner = (BugAuditScanner) constructor.newInstance();
                    Lang scannerLang = bugAuditScanner.getLang();
                    if (scannerLang == null) {
                        scannerLang = Lang.Unknown;
                    }
                    if (scannerLang == lang) {
                        if (specifiedToolName == null || specifiedToolName.isEmpty() ||
                                specifiedToolName.equalsIgnoreCase(bugAuditScanner.getTool())) {
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

    public static String buildProject() throws IOException, InterruptedException, BugAuditException {
        String bugAuditBuildCommand = System.getenv(bugAuditBuildScriptEnv);
        if (bugAuditBuildCommand != null && !bugAuditBuildCommand.isEmpty()) {
            System.out.println("Running: " + bugAuditBuildCommand);
            CommandRunner commandRunner = new CommandRunner(bugAuditBuildCommand, scanDir, "Building Project");
            if (commandRunner.execute() == 0) {
                return commandRunner.getResult();
            }
            throw new BugAuditException("Build Failed!");
        }
        return null;
    }

    private static File calculateScanDir() {
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

    protected String runCommand(String command) throws IOException, InterruptedException {
        CommandRunner commandRunner;
        if (scanDir != null && scanDir.exists() && scanDir.isDirectory()) {
            commandRunner = new CommandRunner(command, scanDir, getTool());
        } else {
            commandRunner = new CommandRunner(command, getTool());
        }
        commandRunner.execute();
        return commandRunner.getResult();
    }

    protected boolean isParserOnly() {
        return parserOnly;
    }

    private String getScannerDirLabel() {
        if (scanDir != null) {
            return "bugaudit-scan-dir-" + scanDir.getPath();
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

    public BugAuditScanResult getBugAuditScanResult() {
        return bugAuditScanResult;
    }

    protected abstract Lang getLang();

    public abstract String getTool();

    public abstract void scan() throws Exception;

}
