package hudson.plugins.depgraph_view.model;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

import java.util.Set;

/**
 * @author wolfs
 */
public class MyGraph {

    private Set<Edge> edges = Sets.newHashSet();
    private Set<ProjectNode> nodes = Sets.newHashSet();
    private SetMultimap<ProjectNode, Edge> node2OutgoingEdges = HashMultimap.create();
    private SetMultimap<ProjectNode, Edge> node2IncomingEdges = HashMultimap.create();

    public void addEdge(Edge edge) {
        edges.add(edge);
        node2IncomingEdges.put(edge.target, edge);
        node2OutgoingEdges.put(edge.source, edge);
    }

    public <T extends Edge> Set<ProjectNode> addEdgesWithNodes(Iterable<T> edges) {
        Set<ProjectNode> newNodes = Sets.newHashSet();
        for (T edge : edges) {
            for (ProjectNode node: edge.getNodes()) {
                if (nodes.add(node)) {
                    newNodes.add(node);
                }
            }
            addEdge(edge);
        }
        return newNodes;
    }

    public ImmutableSet<Edge> getEdges() {
        return ImmutableSet.copyOf(edges);
    }

    public Set<ProjectNode> getNodes() {
        return ImmutableSet.copyOf(nodes);
    }

    public Set<ProjectNode> getIsolatedNodes() {
        return Sets.difference(nodes, Sets.newHashSet(Sets.union(
                Sets.newHashSet(node2OutgoingEdges.keySet()),
                Sets.newHashSet(node2IncomingEdges.keySet()))));
    }

}
