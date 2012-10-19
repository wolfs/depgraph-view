package hudson.plugins.depgraph_view.model;

import hudson.model.AbstractProject;

/**
 * @author wolfs
 */
public interface SubProjectProvider {
    public Iterable<ProjectNode> getSubProjectsOf(AbstractProject<?,?> project);
}
