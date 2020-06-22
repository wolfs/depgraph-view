package hudson.plugins.depgraph_view.model.graph.edge;

import hudson.model.Job;

public class ReverseBuildTriggerEdge extends DependencyEdge {

    public ReverseBuildTriggerEdge(Job<?, ?> upstreamProject, Job<?, ?> downstreamProject) {
        super(upstreamProject, downstreamProject);
    }

    @Override
    public String getColor() {
        return "red";
    }

}
