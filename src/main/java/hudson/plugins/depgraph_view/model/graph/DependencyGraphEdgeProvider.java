package hudson.plugins.depgraph_view.model.graph;

import hudson.model.AbstractProject;
import hudson.model.DependencyGraph;
import jenkins.model.Jenkins;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wolfs
 */
public class DependencyGraphEdgeProvider implements EdgeProvider {

    private DependencyGraph dependencyGraph;

    @Inject
    DependencyGraphEdgeProvider(Jenkins jenkins) {
        dependencyGraph = jenkins.getDependencyGraph();
    }

    @Override
    public Iterable<Edge> getEdgesIncidentWith(AbstractProject<?, ?> project) {
        List<DependencyGraph.Dependency> dependencies = new ArrayList<DependencyGraph.Dependency>();
        dependencies.addAll(dependencyGraph.getDownstreamDependencies(project));
        dependencies.addAll(dependencyGraph.getUpstreamDependencies(project));

        List<Edge> edges = new ArrayList<Edge>();
        for (DependencyGraph.Dependency dependency : dependencies) {
            edges.add(new DependencyEdge(dependency));
        }

        return edges;
    }

}
