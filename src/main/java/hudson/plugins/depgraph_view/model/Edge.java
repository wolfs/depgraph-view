package hudson.plugins.depgraph_view.model;

import com.google.common.collect.ImmutableSet;

/**
 * @author wolfs
 */
public abstract class Edge {
    public final ProjectNode source;
    public final ProjectNode target;

    public Edge(ProjectNode source, ProjectNode target) {
        this.source = source;
        this.target = target;
    }

    public ImmutableSet<ProjectNode> getNodes() {
        return ImmutableSet.of(source, target);
    }

    public abstract String getType();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Edge edge = (Edge) o;

        if (!source.equals(edge.source)) return false;
        if (!target.equals(edge.target)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = source.hashCode();
        result = 31 * result + target.hashCode();
        return result;
    }
}
