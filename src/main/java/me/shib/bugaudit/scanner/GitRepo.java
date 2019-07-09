package me.shib.bugaudit.scanner;

import me.shib.bugaudit.commons.BugAuditException;
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

    public static boolean cloneRepo(String gitUrl, String gitApiToken, File dirToCloneInto) throws BugAuditException {
        try {
            String cleanedGitUrl = cleanRepoUrl(gitUrl);
            Git git = Git.cloneRepository()
                    .setURI("https://" + cleanedGitUrl)
                    .setDirectory(dirToCloneInto)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider("git", gitApiToken))
                    .call();
            git.close();
            File gitDir = new File(".git");
            return gitDir.exists();
        } catch (GitAPIException e) {
            throw new BugAuditException(e.getMessage());
        }
    }

    public static boolean cloneRepo(String gitUrl, String gitApiToken) throws BugAuditException {
        return cloneRepo(gitUrl, gitApiToken, new File(System.getProperty("user.dir")));
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
