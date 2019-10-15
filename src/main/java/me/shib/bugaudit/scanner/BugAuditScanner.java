package me.shib.bugaudit.scanner;

import com.google.gson.Gson;
import me.shib.bugaudit.commons.BugAuditException;
import org.apache.commons.codec.digest.DigestUtils;
import org.reflections.Reflections;

import java.io.*;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class BugAuditScanner {

    private static final transient Gson gson = new Gson();
    private static final transient File scanDir;
    private static final transient String scannerParserOnlyEnv = "BUGAUDIT_SCANNER_PARSERONLY";
    private static final transient String scannerToolEnv = "BUGAUDIT_SCANNER_TOOL";
    private static final transient String scannerDirPathEnv = "BUGAUDIT_SCAN_DIR";
    private static final transient String bugAuditBuildScriptEnv = "BUGAUDIT_BUILD_SCRIPT";
    private static final transient String cveBaseURL = "https://nvd.nist.gov/vuln/detail/";
    private static final transient Reflections reflections = new Reflections(BugAuditScanner.class.getPackage().getName());

    static {
        scanDir = calculateScanDir();
    }

    private transient boolean parserOnly;
    private transient BugAuditScanResult bugAuditScanResult;

    public BugAuditScanner() throws BugAuditException {
        this.bugAuditScanResult = new BugAuditScanResult(getTool(), GitRepo.getRepo(), getScannerDirLabel());
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
                    if (bugAuditScanner.isLangSupported(lang)) {
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

    public static File getScanDirectory() {
        return scanDir;
    }

    protected static String getBuildScript() {
        String bugAuditBuildScript = System.getenv(bugAuditBuildScriptEnv);
        if (bugAuditBuildScript != null && !bugAuditBuildScript.isEmpty()) {
            return bugAuditBuildScript;
        }
        return null;
    }

    public static String buildProject() throws IOException, InterruptedException, BugAuditException {
        String bugAuditBuildScript = getBuildScript();
        if (bugAuditBuildScript != null) {
            System.out.println("Running: " + bugAuditBuildScript);
            CommandRunner commandRunner = new CommandRunner(bugAuditBuildScript, scanDir, "Building Project");
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
        if (scanDirPath != null && !scanDirPath.isEmpty() && !scanDirPath.startsWith("/")) {
            File scanDir = new File(scanDirPath);
            if (scanDir.isDirectory() && scanDir.getAbsolutePath().startsWith(currentPath)) {
                return scanDir;
            }
        }
        return new File(System.getProperty("user.dir"));
    }

    protected String getHash(File file, int lineNo, String type, String[] args) throws IOException {
        return getHash(file, lineNo, lineNo, type, args);
    }

    protected String getHash(File file, int lineNo, String type) throws IOException {
        return getHash(file, lineNo, lineNo, type, null);
    }

    protected String getHash(File file, int startLineNo, int endLineNo, String type) throws IOException {
        return getHash(file, startLineNo, endLineNo, type, null);
    }

    protected String getHash(File file, int startLineNo, int endLineNo, String type, String[] args) throws IOException {
        class HashableContent {
            private String filePath;
            private String snippet;
            private String type;
            private String[] args;
        }
        List<String> lines = readLinesFromFile(file);
        if (startLineNo <= endLineNo && endLineNo <= lines.size() && startLineNo > 0) {
            StringBuilder snippet = new StringBuilder();
            snippet.append(lines.get(startLineNo - 1));
            for (int i = startLineNo; i < endLineNo; i++) {
                snippet.append("\n").append(lines.get(i));
            }
            HashableContent hashableContent = new HashableContent();
            hashableContent.type = type;
            hashableContent.filePath = file.getAbsolutePath().replaceFirst(scanDir.getAbsolutePath(), "");
            hashableContent.snippet = snippet.toString();
            hashableContent.args = args;
            return DigestUtils.sha1Hex(gson.toJson(hashableContent));
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
            File currentDir = new File(System.getProperty("user.dir"));
            if (!scanDir.equals(currentDir)) {
                return "bugaudit-scan-dir-" + scanDir.getPath();
            }
        }
        return null;
    }

    protected String getUrlForCVE(String cve) throws BugAuditException {
        if (cve != null && cve.toUpperCase().startsWith("CVE")) {
            return cveBaseURL + cve;
        }
        throw new BugAuditException("CVE provided is not valid");
    }

    private List<String> readLinesFromFile(File file) throws IOException {
        List<String> lines = new ArrayList<>();
        if (file.exists() && !file.isDirectory()) {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
            br.close();
        }
        return lines;
    }

    protected String readFromFile(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        for (String line : readLinesFromFile(file)) {
            content.append(line).append("\n");
        }
        return content.toString();
    }

    protected void writeToFile(String content, File file) throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(file);
        pw.append(content);
        pw.close();
    }

    public BugAuditScanResult getBugAuditScanResult() {
        return bugAuditScanResult;
    }

    protected abstract boolean isLangSupported(Lang lang);

    public abstract String getTool();

    public abstract void scan() throws Exception;

}
