package hudson.plugins.depgraph_view.model;

import hudson.model.AbstractProject;
import hudson.model.DependencyGraph;
import jenkins.model.Jenkins;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wolfs
 */
public class DependencyGraphEdgeProvider implements EdgeProvider {
    @Override
    public Iterable<Edge> getEdgesIncidentWith(AbstractProject<?, ?> project) {
        List<DependencyGraph.Dependency> dependencies = new ArrayList<DependencyGraph.Dependency>();
        DependencyGraph dependencyGraph = Jenkins.getInstance().getDependencyGraph();
        dependencies.addAll(dependencyGraph.getDownstreamDependencies(project));
        dependencies.addAll(dependencyGraph.getUpstreamDependencies(project));

        List<Edge> edges = new ArrayList<Edge>();
        for (DependencyGraph.Dependency dependency : dependencies) {
            edges.add(new DependencyEdge(dependency));
        }

        return edges;
    }

}
