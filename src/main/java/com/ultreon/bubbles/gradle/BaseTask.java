package com.ultreon.bubbles.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.Task;

public abstract class BaseTask extends DefaultTask {
    public BaseTask(String group) {
        this.setGroup(group);

        this.doFirst(this::execute);
    }

    protected abstract void execute(Task task);
}
