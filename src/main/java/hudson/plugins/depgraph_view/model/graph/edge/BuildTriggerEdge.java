package hudson.plugins.depgraph_view.model.graph.edge;

import hudson.model.Job;

public class BuildTriggerEdge extends DependencyEdge {

    public BuildTriggerEdge(Job<?, ?> upstreamProject, Job<?, ?> downstreamProject) {
        super(upstreamProject, downstreamProject);
    }

    @Override
    public String getColor() {
        return "black";
    }

}
