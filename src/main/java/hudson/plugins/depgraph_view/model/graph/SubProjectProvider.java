package hudson.plugins.depgraph_view.model.graph;

import hudson.ExtensionPoint;
import hudson.model.AbstractProject;

/**
 * @author wolfs
 */
public interface SubProjectProvider extends ExtensionPoint {
    public Iterable<ProjectNode> getSubProjectsOf(AbstractProject<?,?> project);
}
