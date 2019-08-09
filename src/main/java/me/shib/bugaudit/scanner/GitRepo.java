package me.shib.bugaudit.scanner;

import me.shib.bugaudit.commons.BugAuditException;

import java.io.File;
import java.io.IOException;

public final class GitRepo {

    private static final String gitUrlEnv = "BUGAUDIT_GIT_REPO";
    private static final String gitBranchEnv = "BUGAUDIT_GIT_BRANCH";
    private static final String gitCommitEnv = "BUGAUDIT_GIT_COMMIT";

    private static GitRepo gitRepo;

    private String host;
    private String owner;
    private String repoName;
    private String branch;
    private String commit;
    private Lang lang;

    private GitRepo(String url, String branch, String commit) {
        url = cleanRepoUrl(url);
        String[] urlSplit = url.split("/");
        this.host = urlSplit[0];
        this.repoName = urlSplit[urlSplit.length - 1];
        this.owner = url.replaceFirst(host + "/", "");
        this.owner = removeEndingSequence(owner, "/" + repoName);
        this.branch = branch;
        this.commit = commit;
        this.lang = Lang.getCurrentLang();
    }

    public static GitRepo getRepo() throws BugAuditException {
        if (gitRepo == null) {
            gitRepo = new GitRepo(getGitUrlFromRepoAndEnv(), getGitBranchFromRepoAndEnv(),
                    getGitCommitFromRepoAndEnv());
        }
        return gitRepo;
    }

    private static boolean isGitRepo(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().equalsIgnoreCase(".git") && file.isDirectory()) {
                        System.out.println(file.getAbsolutePath());
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static String runGitCommand(String gitCommand) throws BugAuditException {
        CommandRunner runner = new CommandRunner(gitCommand, "Git");
        runner.suppressConsoleLog();
        try {
            runner.execute();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
        String response = runner.getStreamContent();
        if (response.contains("command not found") || response.contains("is currently not installed")) {
            throw new BugAuditException("Git was not found in local environment before proceeding");
        }
        return response;
    }

    private static String getGitUrlFromLocalRepo() throws BugAuditException {
        String response = runGitCommand("git config --get remote.origin.url");
        if (response != null) {
            return response.trim();
        }
        return null;
    }

    private static String getGitBranchFromLocalRepo() throws BugAuditException {
        String response = runGitCommand("git branch");
        try {
            return response.split("\\s+")[1];
        } catch (Exception e) {
            return null;
        }
    }

    private static String getGitCommitFromLocalRepo() throws BugAuditException {
        String commit = runGitCommand("git show --format=%H --no-patch");
        if (commit == null || commit.isEmpty()) {
            return null;
        }
        return commit.trim();
    }

    private static String getGitUrlFromRepoAndEnv() throws BugAuditException {
        String gitUrl = System.getenv(gitUrlEnv);
        if (gitUrl == null || gitUrl.isEmpty()) {
            try {
                gitUrl = getGitUrlFromLocalRepo();
            } catch (BugAuditException e) {
                gitUrl = null;
            }
            if (gitUrl == null) {
                throw new BugAuditException("Run inside a Git repo or provide " +
                        gitUrlEnv + " environmental variable.");
            }
        }
        return gitUrl;
    }

    private static String getGitBranchFromRepoAndEnv() throws BugAuditException {
        String gitBranch = System.getenv(gitBranchEnv);
        if (gitBranch == null || gitBranch.isEmpty()) {
            try {
                gitBranch = getGitBranchFromLocalRepo();
            } catch (BugAuditException e) {
                gitBranch = null;
            }
            if (gitBranch == null) {
                throw new BugAuditException("Run inside a Git repo or provide " +
                        gitBranchEnv + " environmental variable.");
            }
        }
        return gitBranch;
    }

    private static String getGitCommitFromRepoAndEnv() throws BugAuditException {
        String gitCommit = System.getenv(gitCommitEnv);
        if (gitCommit == null || gitCommit.isEmpty()) {
            try {
                gitCommit = getGitCommitFromLocalRepo();
            } catch (BugAuditException e) {
                gitCommit = null;
            }
            if (gitCommit == null) {
                throw new BugAuditException("Run inside a Git repo or provide " +
                        gitBranchEnv + " environmental variable.");
            }
        }
        return gitCommit;
    }

    private static String removeEndingSequence(String source, String seq) {
        if (source.endsWith(seq)) {
            int start = source.lastIndexOf(seq);
            return source.substring(0, start) +
                    source.substring(start + seq.length());
        }
        return source;
    }

    private static String cleanRepoUrl(String url) {
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

    public Lang getLang() {
        return lang;
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

    public String getWebUrl() {
        return "https://" + host + "/" + owner + "/" + repoName;
    }

    public String getRepoUrl() {
        return "https://" + host + "/" + owner + "/" + repoName + ".git";
    }

    public String getSSHPath() {
        return "git@" + host + ":" + owner + "/" + repoName + ".git";
    }

    public String getBranch() {
        return branch;
    }

    public String getCommit() {
        return commit;
    }

    @Override
    public String toString() {
        return owner + "/" + repoName;
    }
}
