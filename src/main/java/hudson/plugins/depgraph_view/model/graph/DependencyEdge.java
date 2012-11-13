package hudson.plugins.depgraph_view.model.graph;

import hudson.model.DependencyGraph;

import static hudson.plugins.depgraph_view.model.graph.ProjectNode.node;

/**
 * @author wolfs
 */
public class DependencyEdge extends Edge {


    private final DependencyGraph.Dependency dependency;

    public DependencyEdge(DependencyGraph.Dependency dependency) {
        super(node(dependency.getUpstreamProject()), node(dependency.getDownstreamProject()));
        this.dependency = dependency;
    }

    @Override
    public String getType() {
        return "dep";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DependencyEdge that = (DependencyEdge) o;

        if (!dependency.equals(that.dependency)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return dependency.hashCode();
    }
}
