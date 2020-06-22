package hudson.plugins.depgraph_view.model.graph.edge;

import hudson.model.Job;

public class ParameterizedTriggerEdge extends DependencyEdge {

    public ParameterizedTriggerEdge(Job<?, ?> upstreamProject, Job<?, ?> downstreamProject) {
        super(upstreamProject, downstreamProject);
    }

    @Override
    public String getColor() {
        return "blue";
    }

}
