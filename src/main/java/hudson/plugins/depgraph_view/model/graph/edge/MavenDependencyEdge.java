package hudson.plugins.depgraph_view.model.graph.edge;

import hudson.model.Job;

public class MavenDependencyEdge extends DependencyEdge {

    public MavenDependencyEdge(Job<?, ?> upstreamProject, Job<?, ?> downstreamProject) {
        super(upstreamProject, downstreamProject);
    }

    @Override
    public String getType() {
        return "maven";
    }

    @Override
    public String getColor() {
        return "green";
    }

}
