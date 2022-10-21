package com.ultreon.bubbles.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.tasks.TaskContainer;

import javax.annotation.Nonnull;
import java.util.Objects;

@SuppressWarnings("unused")
public class BubblesGradlePlugin implements Plugin<Project> {
    private static BubblesGradlePlugin instance;

    public BubblesGradlePlugin() {
        instance = this;
    }

    public static BubblesGradlePlugin get() {
        return instance;
    }

    @Override
    public void apply(@Nonnull Project project) {
        RepositoryHandler repositories = project.getRepositories();
        ConfigurationContainer configurations = project.getConfigurations();
        TaskContainer tasks = project.getTasks();
        repositories.maven(it -> {
            it.setName("Bubble Blaster Maven");
            it.setUrl(project.uri("https://maven.pkg.github.com/Ultreon/bubble-blaster-2"));
            it.credentials(creds -> {
                String usr = String.valueOf(project.findProperty("gpr.user"));
                String key = String.valueOf(project.findProperty("gpr.key"));
                if (usr.equals("null")) usr = System.getenv("GITHUB_USERNAME");
                if (key.equals("null")) key = System.getenv("GITHUB_TOKEN");
                creds.setUsername(usr);
                creds.setPassword(key);
            });
        });
        repositories.maven(it -> {
            it.setName("Atlassian");
            it.setUrl(project.uri("https://maven.atlassian.com/3rdparty/"));
        });
        repositories.maven(it -> {
            it.setName("ImageJ");
            it.setUrl(project.uri("https://maven.imagej.net/content/repositories/public/"));
        });
        repositories.maven(it -> {
            it.setName("Maven Central");
            it.setUrl(project.uri("https://repo1.maven.org/maven2/"));
        });
        repositories.maven(it -> {
            it.setName("RuneLite");
            it.setUrl(project.uri("https://repo.runelite.net/"));
        });
        repositories.maven(it -> {
            it.setName("JitPack");
            it.setUrl(project.uri("https://jitpack.io/"));
        });

        configurations.getByName("api").setCanBeResolved(true);

        tasks.register("prepareRunGame", PrepareRunGameTask.class);
        tasks.register("prepareRunGameDebug", PrepareRunGameTask.class, task -> task.args("--debug"));
        tasks.register("prepareRunGameDev", PrepareRunGameTask.class, task -> task.args("--dev"));

        if (Objects.equals(project.getRootProject(), project)) {
            tasks.register("ideaSync", IdeaSyncTask.class);
        }
    }
}
