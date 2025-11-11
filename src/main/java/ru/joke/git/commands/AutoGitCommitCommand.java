package ru.joke.git.commands;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.CommitConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import ru.joke.classpath.ClassPathIndexed;
import ru.joke.git.shared.GitStorage;

@ClassPathIndexed("commit")
public final class AutoGitCommitCommand implements AutoGitCommand<RevCommit> {

    private boolean all;
    private boolean amend;
    private Person author;
    private Person committer;
    private CommitConfig.CleanupMode cleanupMode;
    private boolean defaultClean;
    private boolean insertChangeId;
    private String message;
    private boolean noVerify;
    private boolean sign;
    private String signingKey;

    @Override
    public RevCommit call() {
        try {
            final var commitCommand = GitStorage.getGit().commit();
            return commitCommand
                    .setAll(this.all)
                    .setAmend(this.amend)
                    .setAuthor(this.author.name, this.author.email)
                    .setCommitter(this.committer.name, this.committer.email)
                    .setCleanupMode(this.cleanupMode)
                    .setDefaultClean(this.defaultClean)
                    .setHookErrorStream(System.err)
                    .setHookOutputStream(System.out)
                    .setInsertChangeId(this.insertChangeId)
                    .setMessage(this.message)
                    .setNoVerify(this.noVerify)
                    .setSign(this.sign)
                    .setSigningKey(this.signingKey)
                    .call();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean all() {
        return all;
    }

    public void setAll(boolean all) {
        this.all = all;
    }

    public boolean amend() {
        return amend;
    }

    public void setAmend(boolean amend) {
        this.amend = amend;
    }

    public Person author() {
        return author;
    }

    public void setAuthor(Person author) {
        this.author = author;
    }

    public Person committer() {
        return committer;
    }

    public void setCommitter(Person committer) {
        this.committer = committer;
    }

    public CommitConfig.CleanupMode cleanupMode() {
        return cleanupMode;
    }

    public void setCleanupMode(CommitConfig.CleanupMode cleanupMode) {
        this.cleanupMode = cleanupMode;
    }

    public boolean defaultClean() {
        return defaultClean;
    }

    public void setDefaultClean(boolean defaultClean) {
        this.defaultClean = defaultClean;
    }

    public boolean insertChangeId() {
        return insertChangeId;
    }

    public void setInsertChangeId(boolean insertChangeId) {
        this.insertChangeId = insertChangeId;
    }

    public String message() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean noVerify() {
        return noVerify;
    }

    public void setNoVerify(boolean noVerify) {
        this.noVerify = noVerify;
    }

    public boolean sign() {
        return sign;
    }

    public void setSign(boolean sign) {
        this.sign = sign;
    }

    public String signingKey() {
        return signingKey;
    }

    public void setSigningKey(String signingKey) {
        this.signingKey = signingKey;
    }

    public static class Person {

        private final String name;
        private final String email;

        private Person(final String name, final String email) {
            this.name = name;
            this.email = email;
        }
    }
}
