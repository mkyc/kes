package it.mltk.kes.domain.service;

import it.mltk.kes.domain.model.Project;
import it.mltk.kes.infrastructure.streams.client.ProjectClient;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public class ProjectService {

    private final ProjectClient projectClient;

    public ProjectService(ProjectClient projectClient) {
        this.projectClient = projectClient;
    }

    public UUID initProject() {
        Project project = new Project(UUID.randomUUID());
        log.debug("project : " + project);
        projectClient.save(project);
        return project.getId();
    }

    public void renameProject(UUID projectUuid, String name) {
        Project project = projectClient.find(projectUuid);
        project.renameProject(name);
        projectClient.save(project);
    }

    public UUID addTask(UUID projectUuid, String taskName) {
        Project project = projectClient.find(projectUuid);
        UUID taskUuid = UUID.randomUUID();
        project.addTask(taskUuid, taskName);
        projectClient.save(project);
        return taskUuid;
    }

    public void deleteTask(final UUID projectUuid, final UUID taskUuid) {
        Project project = projectClient.find(projectUuid);
        project.deleteTask(taskUuid);
        projectClient.save(project);
    }

    public void renameTask(final UUID projectUuid, final UUID taskUuid, final String name) {
        Project project = projectClient.find(projectUuid);
        project.renameTask(taskUuid, name);
        projectClient.save(project);
    }

    public Project find( final UUID projectUuid ) {
        log.debug( "find : enter" );
        Project project = projectClient.find( projectUuid );
        log.debug( "find : project=" + project );
        return project;
    }
}
