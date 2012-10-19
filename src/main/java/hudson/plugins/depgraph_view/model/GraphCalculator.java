package hudson.plugins.depgraph_view.model;


import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import hudson.model.AbstractProject;
import hudson.model.Item;

import java.util.List;
import java.util.Set;

/**
 * @author wolfs
 */
public class GraphCalculator {

    private Set<ProjectNode> initialProjects;
    private Set<EdgeProvider> edgeProviders;

    public GraphCalculator(Iterable<ProjectNode> initialProjects, Iterable<? extends EdgeProvider> edgeProviders) {
        this.initialProjects = Sets.newHashSet(initialProjects);
        this.edgeProviders = Sets.newHashSet(edgeProviders);
    }

    public DependencyGraph generateGraph() {
        DependencyGraph graph = new DependencyGraph();
        graph.addNodes(initialProjects);
        extendGraph(graph, initialProjects);
        return graph;
    }

    private void extendGraph(DependencyGraph graph, Set<ProjectNode> fromProjects) {
        Set<ProjectNode> newProj = Sets.newHashSet();
        List<Edge> newEdges = Lists.newArrayList();
        for (ProjectNode projectNode : fromProjects) {
            AbstractProject<?,?> project = projectNode.getProject();
            if (project.hasPermission(Item.READ)) {
                for (EdgeProvider edgeProvider : edgeProviders) {
                    Iterables.addAll(newEdges, edgeProvider.getEdgesIncidentWith(project));
                }
            }
        }
        newProj = graph.addEdgesWithNodes(newEdges);
        if (!newProj.isEmpty()) {
            extendGraph(graph, newProj);
        }
    }

    public static Iterable<ProjectNode> abstractProjectSetToProjectNodeSet(Iterable<? extends AbstractProject<?,?>> projects) {
        return Iterables.transform(projects, new Function<AbstractProject<?, ?>, ProjectNode>() {
            @Override
            public ProjectNode apply(AbstractProject<?, ?> input) {
                return new ProjectNode(input);
            }
        });
    }
}
