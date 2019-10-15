package me.shib.bugaudit.scanner;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public enum Lang {

    Go, Java, JavaScript, Python, Ruby, Unknown;

    private static final String langEnv = "BUGAUDIT_LANG";
    private static Lang lang;

    static synchronized Lang getCurrentLang() {
        if (lang == null) {
            try {
                lang = Lang.valueOf(System.getenv(langEnv));
            } catch (Exception e) {
                lang = findLangFromCode();
                if (lang != null) {
                    System.out.println("Detected language from source code: " + lang);
                } else {
                    System.out.println("Detected language from source code: Unknown");
                }
            }
        }
        return lang;
    }

    private static Lang findLangFromCode() {
        File dir;
        if (BugAuditScanner.getScanDirectory() != null) {
            dir = BugAuditScanner.getScanDirectory();
        } else {
            dir = Paths.get("").toAbsolutePath().toFile();
        }
        String[] fileArr = dir.list();
        if (fileArr != null) {
            List<String> files = Arrays.asList(fileArr);
            if (files.contains("pom.xml") || files.contains("build.gradle")) {
                return Java;
            } else if (files.contains("Gemfile.lock") || files.contains("Gemfile")) {
                return Ruby;
            } else if (files.contains("package.json")) {
                return JavaScript;
            }
        }
        return null;
    }

}
