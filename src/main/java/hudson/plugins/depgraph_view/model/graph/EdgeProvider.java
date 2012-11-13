package hudson.plugins.depgraph_view.model.graph;

import hudson.ExtensionPoint;
import hudson.model.AbstractProject;

/**
 * This is an extension point which makes it possible to add edges
 * to the DependencyGraph which gets drawn. Note that in order to add your own
 * EdgeProvider you must not annotate the corresponding subclass with {@link hudson.Extension}
 * but instead add a {@link com.google.inject.Module} with a {@link com.google.inject.multibindings.Multibinder}
 * which has the {@link hudson.Extension} annotation. For example see {@link DependencyGraphModule}
 * and {@link DependencyGraphEdgeProvider}
 * @author wolfs
 */
public interface EdgeProvider extends ExtensionPoint {
    public Iterable<Edge> getEdgesIncidentWith(AbstractProject<?,?> project);
}
