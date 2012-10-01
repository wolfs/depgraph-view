package hudson.plugins.depgraph_view.model;

import hudson.model.AbstractProject;

/**
 * @author wolfs
 */
public interface EdgeProvider {
    public Iterable<Edge> getEdgesIncidentWith(AbstractProject<?,?> project);
}
