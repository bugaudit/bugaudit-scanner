package me.shib.bugaudit.commons;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public enum Lang {

    GoLang, Java, NodeJS, Python, Ruby;

    private static final String langEnv = "BUGAUDIT_LANG";
    private static Lang lang;

    public static synchronized Lang getCurrentLang() {
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
        File currentDir = Paths.get("").toAbsolutePath().toFile();
        String[] fileArr = currentDir.list();
        if (fileArr != null) {
            List<String> files = Arrays.asList(fileArr);
            if (files.contains("pom.xml")) {
                return Java;
            } else if (files.contains("Gemfile.lock") || files.contains("Gemfile")) {
                return Ruby;
            } else if (files.contains("package.json")) {
                return NodeJS;
            }
        }
        return null;
    }

}
