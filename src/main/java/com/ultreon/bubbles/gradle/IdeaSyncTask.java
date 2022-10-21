package com.ultreon.bubbles.gradle;

import kotlin.Unit;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.plugins.ide.idea.model.IdeaModel;
import org.gradle.plugins.ide.idea.model.IdeaProject;
import org.jetbrains.gradle.ext.Application;
import org.jetbrains.gradle.ext.IdeaModelUtilsKt;

import java.util.stream.Collectors;

public class IdeaSyncTask extends BaseTask {
    public IdeaSyncTask() {
        super("bubbles");

        for (PrepareRunGameTask prepareRunGameTask : getProject().getTasks().withType(PrepareRunGameTask.class)) {
            dependsOn(prepareRunGameTask);
        }
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected final void execute(Task task) {
        Project project = task.getProject();
        Project rootProject = project.getRootProject();
        TaskContainer tasks = project.getTasks();
        var runConfigurationsDir = rootProject.file(".idea/runConfigurations");
        runConfigurationsDir.mkdirs();

        IdeaModel idea = (IdeaModel) project.getExtensions().findByName("idea");
        IdeaProject ideaProj;
        if (idea != null) {
            ideaProj = idea.getProject();
        } else {
            getLogger().error("'idea' extension wasn't found, did you add the 'org.jetbrains.gradle.plugin.idea-ext' plugin?.");
            return;
        }

        TaskCollection<PrepareRunGameTask> prepareRunGameTasks = tasks.withType(PrepareRunGameTask.class);
        getLogger().info("Creating " + prepareRunGameTasks.size() + " run configs.");

        if (prepareRunGameTasks.isEmpty()) {
            getLogger().warn("There are no prepare run game tasks bruh.");
        }

        prepareRunGameTasks.forEach(prepareTask -> {
            var taskName = prepareTask.getName();
            var mainClass = "com.ultreon.dev.GameDevMain";
            var props = prepareTask.getProps();
            var env = prepareTask.getEnv();
            var args = prepareTask.getArgs();

            var params = args.stream().map(name -> {
                if (name.contains(" ")) {
                    return "\"" + name + "\"";
                }
                return name;
            }).collect(Collectors.joining(" "));

            getLogger().info("Create run config: " + taskName);
            idea.project(proj -> IdeaModelUtilsKt.settings(ideaProj, settings -> {
                IdeaModelUtilsKt.runConfigurations(settings, runConfigurations -> {
                    Application runConfig = runConfigurations.maybeCreate(taskName, Application.class);
                    runConfig.setMainClass(mainClass);
                    runConfig.setProgramParameters(params);
                    runConfig.setEnvs(env);
                    for (var entry : props.entrySet()) {
                        runConfig.setProperty(entry.getKey(), entry.getValue());
                    }
                    return Unit.INSTANCE;
                });
                return Unit.INSTANCE;
            }));
        });
    }
}
