package me.shib.bugaudit.probe;

public final class GitRepo {

    private static final String gitUrlEnv = "GIT_URL";
    private static final String gitBranchEnv = "GIT_BRANCH";

    private String host;
    private String owner;
    private String repoName;
    private String branch;

    public GitRepo() {
        this(getGitUrlFromEnv());
    }

    public GitRepo(String url) {
        this(url, null);
    }

    public GitRepo(String url, String branch) {
        url = cleanRepoUrl(url);
        String[] urlSplit = url.split("/");
        this.host = urlSplit[0];
        this.repoName = urlSplit[urlSplit.length - 1];
        this.owner = url.replaceFirst(host + "/", "");
        this.owner = removeEndingSequence(owner, "/" + repoName);
        this.branch = getGitBranchFromEnv(branch);
    }

    public GitRepo(String host, String owner, String repoName) {
        this(host, owner, repoName, null);
    }

    public GitRepo(String host, String owner, String repoName, String branch) {
        this.host = cleanHost(host);
        this.owner = owner;
        this.repoName = repoName;
        this.branch = getGitBranchFromEnv(branch);
    }

    private static String getGitUrlFromEnv() {
        String gitUrl = System.getenv(gitUrlEnv);
        if (gitUrl == null) {
            throw new UnsupportedOperationException("Expected " + gitUrlEnv + " environmental variable.");
        }
        return gitUrl;
    }

    private static String getGitBranchFromEnv(String gitBranch) {
        if (gitBranch == null) {
            gitBranch = System.getenv(gitBranchEnv);
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
