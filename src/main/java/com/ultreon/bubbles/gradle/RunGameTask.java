package com.ultreon.bubbles.gradle;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.process.ExecResult;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RunGameTask extends BaseTask {
    private File runDir;
    private final List<String> args = new ArrayList<>();
    private final Map<String, Object> env = new HashMap<>();
    private String maxHeapSize;

    public RunGameTask() {
        super("bubbles");
        runDir = getProject().file("run/main/");
    }

    @Override
    protected final void execute(Task task) {
        Project project = task.getProject();
        if (!runDir.exists()) {
            project.mkdir(runDir);
        }

        List<String> strings = new ArrayList<>(args);
        strings.add(0, "gameDir=" + runDir);
        List<String> devClassPath = buildDevClassPath(project);

        Map<String, Object> env = new HashMap<>(this.env);
        env.put("DEV_CLASS_PATH", String.join(System.getProperty("path.seperator"), devClassPath));
        ExecResult result = project.javaexec(exec -> {
            exec.setMaxHeapSize(maxHeapSize);
            exec.getMainClass().set("com.ultreon.dev.GameDevMain");
            exec.setArgs(strings);
            exec.setEnvironment(env);
            exec.setWorkingDir(runDir);
        });
        result.assertNormalExitValue();
    }

    private List<String> buildDevClassPath(Project project) {
        List<String> list = new ArrayList<>();
        project.getConfigurations().getByName("api").getFiles().forEach(it -> list.add(it.getPath()));
        return list;
    }

    @Internal
    public void args(String... args) {
        this.args.addAll(List.of(args));
    }

    @Internal
    public void env(Map<String, Object> env) {
        this.env.putAll(env);
    }

    @InputFile
    public File getRunDir() {
        return runDir;
    }

    @InputFile
    public void setRunDir(File runDir) {
        this.runDir = runDir;
    }

    @Internal
    public String getMaxHeapSize() {
        return maxHeapSize;
    }

    @Internal
    public void setMaxHeapSize(String maxHeapSize) {
        this.maxHeapSize = maxHeapSize;
    }
}
