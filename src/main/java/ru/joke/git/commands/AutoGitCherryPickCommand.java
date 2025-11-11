package ru.joke.git.commands;

import org.eclipse.jgit.api.CherryPickResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.ContentMergeStrategy;
import org.eclipse.jgit.merge.StrategyRecursive;
import org.eclipse.jgit.merge.StrategyResolve;
import org.eclipse.jgit.revwalk.RevCommit;
import ru.joke.classpath.ClassPathIndexed;
import ru.joke.git.shared.GitStorage;
import ru.joke.git.shared.ProgressMonitorStorage;

import java.io.IOException;
import java.util.List;

@ClassPathIndexed("cherry-pick")
public final class AutoGitCherryPickCommand implements AutoGitCommand<CherryPickResult> {

    private List<String> commitHashes;
    private boolean noCommit = true;
    private ContentMergeStrategy contentMergeStrategy = ContentMergeStrategy.CONFLICT;
    private MergeStrategy mergeStrategy = MergeStrategy.RECURSIVE;

    @Override
    public CherryPickResult call() {
        final Repository repo = GitStorage.getGit().getRepository();
        final var cherryPickCommand = GitStorage.getGit().cherryPick();

        try {
            for (final var hash : this.commitHashes) {
                final var commitRef = repo.resolve(hash);
                cherryPickCommand.include(commitRef);
            }

            return cherryPickCommand
                    .setNoCommit(this.noCommit)
                    .setStrategy(this.mergeStrategy.strategy)
                    .setCherryPickCommitMessageProvider(RevCommit::getFullMessage)
                    .setProgressMonitor(ProgressMonitorStorage.getProgressMonitor())
                    .setContentMergeStrategy(this.contentMergeStrategy)
                    .call();
        } catch (GitAPIException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> commitHashes() {
        return commitHashes;
    }

    public void setCommitHashes(List<String> commitHashes) {
        this.commitHashes = commitHashes;
    }

    public boolean noCommit() {
        return noCommit;
    }

    public void setNoCommit(boolean noCommit) {
        this.noCommit = noCommit;
    }

    public ContentMergeStrategy contentMergeStrategy() {
        return contentMergeStrategy;
    }

    public void setContentMergeStrategy(ContentMergeStrategy contentMergeStrategy) {
        this.contentMergeStrategy = contentMergeStrategy;
    }

    public MergeStrategy mergeStrategy() {
        return mergeStrategy;
    }

    public void setMergeStrategy(MergeStrategy mergeStrategy) {
        this.mergeStrategy = mergeStrategy;
    }

    public enum MergeStrategy {
        RESOLVE(new StrategyResolve()),
        RECURSIVE(new StrategyRecursive());

        private final org.eclipse.jgit.merge.MergeStrategy strategy;

        MergeStrategy(final org.eclipse.jgit.merge.MergeStrategy strategy) {
            this.strategy = strategy;
        }
    }
}
