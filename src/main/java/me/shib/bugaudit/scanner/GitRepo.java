package me.shib.bugaudit.scanner;

import me.shib.bugaudit.commons.BugAuditException;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;

public final class GitRepo {

    private static GitRepo gitRepo;
    private static Repository repository;

    private String host;
    private String owner;
    private String repoName;
    private String branch;
    private Lang lang;

    private GitRepo(String url, String branch) {
        url = cleanRepoUrl(url);
        String[] urlSplit = url.split("/");
        this.host = urlSplit[0];
        this.repoName = urlSplit[urlSplit.length - 1];
        this.owner = url.replaceFirst(host + "/", "");
        this.owner = removeEndingSequence(owner, "/" + repoName);
        this.branch = branch;
        this.lang = Lang.getCurrentLang();
    }

    private static synchronized Repository getJGitRepository() throws IOException {
        if (repository == null) {
            FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
            repositoryBuilder.setMustExist(true);
            repositoryBuilder.setWorkTree(new File(System.getProperty("user.dir")));
            repository = repositoryBuilder.build();
        }
        return repository;
    }

    public static GitRepo getRepo() throws IOException {
        if (gitRepo == null) {
            gitRepo = new GitRepo(getGitUrlFromLocalRepo(), getGitBranchFromLocalRepo());
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

    public static boolean cloneRepo(String gitUrl, String branch, String username, String password, File cloneDir) throws BugAuditException {
        if (cloneDir == null) {
            cloneDir = new File(System.getProperty("user.dir"));
        }
        if (isGitRepo(cloneDir)) {
            throw new BugAuditException("A Git Repository already exists in: " + cloneDir.getAbsolutePath());
        }
        if (gitUrl == null || gitUrl.isEmpty()) {
            throw new BugAuditException("Invalid Git Repository URL: " + gitUrl);
        }
        System.out.println("Cloning...");
        System.out.println("Repository: " + gitUrl);
        try {
            String cleanedGitUrl = cleanRepoUrl(gitUrl);
            CloneCommand cloneCommand = Git.cloneRepository()
                    .setURI("https://" + cleanedGitUrl)
                    .setDirectory(cloneDir);
            if (branch != null && !branch.isEmpty()) {
                System.out.println("Branch: " + branch);
                cloneCommand.setBranch(branch);
            }
            if (password != null && !password.isEmpty()) {
                if (username == null || username.isEmpty()) {
                    username = "git";
                }
                cloneCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password));
            }
            Git git = cloneCommand.call();
            git.close();
            return isGitRepo(cloneDir);
        } catch (GitAPIException e) {
            throw new BugAuditException(e.getMessage());
        }
    }

    public static boolean cloneRepo(String gitUrl, String branch, String authToken, File dirToCloneInto) throws BugAuditException {
        return cloneRepo(gitUrl, branch, null, authToken, dirToCloneInto);
    }

    public static boolean cloneRepo(String gitUrl, String branch, String username, String password) throws BugAuditException {
        return cloneRepo(gitUrl, branch, username, password, new File(System.getProperty("user.dir")));
    }

    public static boolean cloneRepo(String gitUrl, String branch, String gitApiToken) throws BugAuditException {
        return cloneRepo(gitUrl, branch, gitApiToken, new File(System.getProperty("user.dir")));
    }

    private static String getGitUrlFromLocalRepo() throws IOException {
        return getJGitRepository().getConfig().getString("remote", "origin", "url");
    }

    private static String getGitBranchFromLocalRepo() throws IOException {
        return getJGitRepository().getBranch();
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
