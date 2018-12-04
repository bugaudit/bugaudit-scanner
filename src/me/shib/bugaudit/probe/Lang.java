package me.shib.bugaudit.probe;

public enum Lang {

    GoLan, Java, NodeJS, Python, Ruby;

    private static final String langEnv = "LANG";
    private static Lang lang;

    static synchronized Lang getCurrentLang() {
        if (lang == null) {
            try {
                lang = Lang.valueOf(System.getenv(langEnv));
            } catch (Exception e) {
                lang = detectLang();
            }
        }
        return lang;
    }

    private static Lang detectLang() {
        //TODO laguage predictor
        return Lang.Ruby;
    }

}
