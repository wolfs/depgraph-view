package hudson.plugins.depgraph_view.model;

import hudson.model.DependencyGraph;

/**
 * @author wolfs
 */
public class DependencyEdge extends Edge {


    private final DependencyGraph.Dependency dependency;

    public DependencyEdge(DependencyGraph.Dependency dependency) {
        super(new ProjectNode(dependency.getUpstreamProject()), new ProjectNode(dependency.getDownstreamProject()));
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
