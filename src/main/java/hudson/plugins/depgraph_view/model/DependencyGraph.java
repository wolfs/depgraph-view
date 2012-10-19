package hudson.plugins.depgraph_view.model;

import com.google.common.collect.Sets;
import edu.uci.ics.jung.algorithms.filters.VertexPredicateFilter;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import org.apache.commons.collections15.Predicate;

import java.util.Collection;
import java.util.Set;

/**
 * @author wolfs
 */
public class DependencyGraph {

    private DirectedSparseMultigraph<ProjectNode, Edge> graph = new DirectedSparseMultigraph<ProjectNode, Edge>();

    public void addEdge(Edge edge) {
        graph.addEdge(edge, edge.getNodes());
    }

    public <T extends Edge> Set<ProjectNode> addEdgesWithNodes(Iterable<T> edges) {
        Set<ProjectNode> newNodes = Sets.newHashSet();
        for (T edge : edges) {
            for (ProjectNode node: edge.getNodes()) {
                if (!graph.containsVertex(node)) {
                    graph.addVertex(node);
                    newNodes.add(node);
                }
            }
            addEdge(edge);
        }
        return newNodes;
    }

    public <T extends ProjectNode> void addNodes(Iterable<T> nodes) {
        for (T node : nodes) {
            graph.addVertex(node);
        }
    }

    public Collection<Edge> getEdges() {
        return graph.getEdges();
    }

    public Collection<Edge> findEdgeSet(ProjectNode from, ProjectNode to) {
        return graph.findEdgeSet(from, to);
    }

    public Collection<ProjectNode> getNodes() {
        return graph.getVertices();
    }

    public Collection<ProjectNode> getIsolatedNodes() {
        return new VertexPredicateFilter<ProjectNode, Edge>(new Predicate<ProjectNode>() {
            @Override
            public boolean evaluate(ProjectNode projectNode) {
                return graph.degree(projectNode) == 0;
            }
        }).transform(graph).getVertices();
    }

    public Graph getGraph() {
        return graph;
    }

}
