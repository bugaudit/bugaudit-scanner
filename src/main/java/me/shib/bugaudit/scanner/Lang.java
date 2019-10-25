package me.shib.bugaudit.scanner;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

public enum Lang {

    Go("go"), Java("java"), JavaScript("js"),
    Python("py"), Ruby("rb");

    private static final String langEnv = "BUGAUDIT_LANG";
    private static Lang lang;
    private String extension;

    Lang(String extension) {
        this.extension = extension;
    }

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


    private static int indexOfLastSeparator(final String filename) {
        if (filename == null) {
            return -1;
        }
        final int lastUnixPos = filename.lastIndexOf('/');
        final int lastWindowsPos = filename.lastIndexOf('\\');
        return Math.max(lastUnixPos, lastWindowsPos);
    }

    private static int indexOfExtension(final String filename) {
        if (filename == null) {
            return -1;
        }
        final int extensionPos = filename.lastIndexOf('.');
        final int lastSeparator = indexOfLastSeparator(filename);
        return lastSeparator > extensionPos ? -1 : extensionPos;
    }

    private static String getExtension(final String filename) {
        if (filename == null) {
            return null;
        }
        final int index = indexOfExtension(filename);
        if (index == -1) {
            return "";
        } else {
            return filename.substring(index + 1);
        }
    }

    private static void traverseAndUpdateExtensionCount(Map<String, Integer> extensionCountMap, File file) {
        if (!file.isHidden()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null) {
                    for (File f : files) {
                        traverseAndUpdateExtensionCount(extensionCountMap, f);
                    }
                }
            } else {
                String extension = getExtension(file.getAbsolutePath());
                Integer count = extensionCountMap.get(extension);
                if (count != null) {
                    count++;
                } else {
                    count = 1;
                }
                extensionCountMap.put(extension, count);
            }
        }
    }

    private static Map<Lang, Integer> getLangFilesCount(File dir) {
        Map<Lang, Integer> langFilesCountMap = new HashMap<>();
        Map<String, Integer> extensionCountMap = new HashMap<>();
        if (dir != null) {
            traverseAndUpdateExtensionCount(extensionCountMap, dir);
        }
        for (Lang lang : Lang.values()) {
            Integer count = extensionCountMap.get(lang.extension);
            if (count != null) {
                langFilesCountMap.put(lang, extensionCountMap.get(lang.extension));
            }
        }
        return langFilesCountMap;
    }

    private static List<Lang> getLangListByUsage(File dir) {
        Map<Lang, Integer> langFilesCountMap = getLangFilesCount(dir);
        Map<Integer, List<Lang>> countToLang = new HashMap<>();
        for (Map.Entry<Lang, Integer> entry : langFilesCountMap.entrySet()) {
            List<Lang> langList = countToLang.get(entry.getValue());
            if (langList == null) {
                langList = new ArrayList<>();
            }
            langList.add(entry.getKey());
            countToLang.put(entry.getValue(), langList);
        }
        List<Integer> counts = new ArrayList<>(countToLang.keySet());
        Collections.sort(counts);
        Collections.reverse(counts);
        List<Lang> langListByUsage = new ArrayList<>();
        for (Integer count : counts) {
            langListByUsage.addAll(countToLang.get(count));
        }
        return langListByUsage;
    }

    public static Lang getLangFromDir(File dir) {
        List<Lang> langList = getLangListByUsage(dir);
        if (langList.size() > 0) {
            return langList.get(0);
        } else {
            return null;
        }
    }

    private static Lang findLangFromCode() {
        File dir;
        if (BugAuditScanner.getScanDirectory() != null) {
            dir = BugAuditScanner.getScanDirectory();
        } else {
            dir = Paths.get("").toAbsolutePath().toFile();
        }
        return getLangFromDir(dir);
    }

}
