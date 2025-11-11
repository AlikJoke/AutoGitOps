package ru.joke.git.commands;

import org.eclipse.jgit.api.errors.GitAPIException;
import ru.joke.classpath.ClassPathIndexed;
import ru.joke.git.shared.GitStorage;

import java.util.List;

@ClassPathIndexed("add")
public final class AutoGitAddCommand implements AutoGitCommand<Boolean> {

    private boolean all;
    private boolean update;
    private List<String> files;

    @Override
    public Boolean call() {
        try {
            final var addCommand = GitStorage.getGit().add();
            if (this.files != null) {
                this.files.forEach(addCommand::addFilepattern);
            }

            addCommand
                    .setAll(this.all)
                    .setUpdate(this.update)
                    .call();
            return true;
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

    public boolean update() {
        return update;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    public List<String> files() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }
}
