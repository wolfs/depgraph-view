package hudson.plugins.depgraph_view.model.graph;


import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import hudson.model.AbstractProject;
import hudson.model.Item;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

import static hudson.plugins.depgraph_view.model.graph.ProjectNode.node;

/**
 * @author wolfs
 */
public class GraphCalculator {

    private Set<EdgeProvider> edgeProviders;

    @Inject
    public GraphCalculator(Set<EdgeProvider> edgeProviders) {
        this.edgeProviders = Sets.newHashSet(edgeProviders);
    }

    public DependencyGraph generateGraph(Iterable<ProjectNode> initialProjects) {
        DependencyGraph graph = new DependencyGraph();
        graph.addNodes(initialProjects);
        extendGraph(graph, initialProjects);
        return graph;
    }

    private void extendGraph(DependencyGraph graph, Iterable<ProjectNode> fromProjects) {
        List<Edge> newEdges = Lists.newArrayList();
        for (ProjectNode projectNode : fromProjects) {
            AbstractProject<?,?> project = projectNode.getProject();
            if (project.hasPermission(Item.READ)) {
                for (EdgeProvider edgeProvider : edgeProviders) {
                    Iterables.addAll(newEdges, edgeProvider.getEdgesIncidentWith(project));
                }
            }
        }
        Set<ProjectNode> newProj = graph.addEdgesWithNodes(newEdges);
        if (!newProj.isEmpty()) {
            extendGraph(graph, newProj);
        }
    }

    public static Iterable<ProjectNode> abstractProjectSetToProjectNodeSet(Iterable<? extends AbstractProject<?,?>> projects) {
        return Iterables.transform(projects, new Function<AbstractProject<?, ?>, ProjectNode>() {
            @Override
            public ProjectNode apply(AbstractProject<?, ?> input) {
                return node(input);
            }
        });
    }
}
