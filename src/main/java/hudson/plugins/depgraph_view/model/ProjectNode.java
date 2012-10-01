package hudson.plugins.depgraph_view.model;

import com.google.common.base.Preconditions;
import hudson.model.AbstractProject;

/**
 * @author wolfs
 */
public class ProjectNode {
    private final AbstractProject<?,?> project;

    public ProjectNode(AbstractProject<?, ?> project) {
        Preconditions.checkNotNull(project);
        this.project = project;
    }

    public String getName() {
        return project.getFullDisplayName();
    }

    public AbstractProject<?, ?> getProject() {
        return project;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProjectNode that = (ProjectNode) o;

        if (!project.equals(that.project)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return project.hashCode();
    }
}
