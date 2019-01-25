package me.shib.bugaudit.probe;

import me.shib.bugaudit.BugAuditException;
import me.shib.bugaudit.probe.helper.CommandExecutor;

public final class GitRepo {

    private static final String gitUrlEnv = "GIT_URL";
    private static final String gitBranchEnv = "GIT_BRANCH";

    private static GitRepo gitRepo;

    private String host;
    private String owner;
    private String repoName;
    private String branch;

    private GitRepo(String url, String branch) {
        url = cleanRepoUrl(url);
        String[] urlSplit = url.split("/");
        this.host = urlSplit[0];
        this.repoName = urlSplit[urlSplit.length - 1];
        this.owner = url.replaceFirst(host + "/", "");
        this.owner = removeEndingSequence(owner, "/" + repoName);
        this.branch = branch;
    }

    static GitRepo getRepo() {
        if (gitRepo == null) {
            gitRepo = new GitRepo(getGitUrlFromEnv(), getGitBranchFromEnv());
        }
        return gitRepo;
    }

    private static String runGitCommang(String gitCommand) throws BugAuditException {
        CommandExecutor commandExecutor = new CommandExecutor();
        commandExecutor.runCommand(gitCommand);
        String response = commandExecutor.getConsoleOutput();
        if (response.contains("command not found") || response.contains("is currently not installed")) {
            throw new BugAuditException("Git was not found in local environment before proceeding");
        }
        return response;
    }

    private static String getGitUrlFromLocalRepo() throws BugAuditException {
        String response = runGitCommang("git remote show origin");
        String[] lines = response.split("\n");
        for (String line : lines) {
            if (line.contains("Fetch URL")) {
                return line.split("Fetch URL: ")[1];
            }
        }
        return null;
    }

    private static String getGitBranchFromLocalRepo() throws BugAuditException {
        String response = runGitCommang("git branch");
        try {
            return response.split("\\s+")[1];
        } catch (Exception e) {
            return null;
        }
    }

    private static String getGitUrlFromEnv() {
        String gitUrl = System.getenv(gitUrlEnv);
        if (gitUrl == null) {
            try {
                gitUrl = getGitUrlFromLocalRepo();
            } catch (BugAuditException e) {
                gitUrl = null;
            }
            if (gitUrl == null) {
                throw new UnsupportedOperationException("Expected " + gitUrlEnv + " environmental variable.");
            }
        }
        return gitUrl;
    }

    private static String getGitBranchFromEnv() {
        String gitBranch = System.getenv(gitBranchEnv);
        if (gitBranch == null) {
            try {
                gitBranch = getGitBranchFromLocalRepo();
            } catch (BugAuditException e) {
                gitBranch = null;
            }
            if (gitBranch == null) {
                throw new UnsupportedOperationException("Expected " + gitBranchEnv + " environmental variable.");
            }
        }
        return gitBranch;
    }

    private String removeEndingSequence(String source, String seq) {
        if (source.endsWith(seq)) {
            int start = source.lastIndexOf(seq);
            StringBuilder cleaner = new StringBuilder();
            cleaner.append(source, 0, start);
            cleaner.append(source.substring(start + seq.length()));
            return cleaner.toString();
        }
        return source;
    }

    private String cleanRepoUrl(String url) {
        if (url.contains("//")) {
            String[] split = url.split("//");
            url = split[split.length - 1];
        }
        if (url.contains("@")) {
            String[] split = url.split("@");
            url = split[split.length - 1];
        }
        url = removeEndingSequence(url, ".git");
        url = removeEndingSequence(url, "/");
        return url.replaceFirst(":", "/");
    }

    private String cleanHost(String hostName) {
        hostName = removeEndingSequence(hostName, "/");
        String[] split = hostName.split("/");
        return split[split.length - 1];
    }

    public String getHost() {
        return host;
    }

    public String getOwner() {
        return owner;
    }

    public String getRepoName() {
        return repoName;
    }

    public String getUrl() {
        return "https://" + host + "/" + owner + "/" + repoName;
    }

    public String getBranch() {
        return branch;
    }

    @Override
    public String toString() {
        return owner + "/" + repoName;
    }
}
