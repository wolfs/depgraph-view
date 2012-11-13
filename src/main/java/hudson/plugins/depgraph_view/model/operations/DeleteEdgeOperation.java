package hudson.plugins.depgraph_view.model.operations;

import hudson.model.Result;
import hudson.tasks.BuildTrigger;

import java.io.IOException;

import jenkins.model.Jenkins;

public class DeleteEdgeOperation extends EdgeOperation {

    public DeleteEdgeOperation(String sourceJobName, String targetJobName) {
        super(sourceJobName, targetJobName);
    }

    public void perform() throws IOException {
        if (source != null && target != null) {
            final BuildTrigger buildTrigger = (BuildTrigger) source.getPublishersList().get(BuildTrigger.class);
            if (buildTrigger != null) {
                final String childProjectsValue = buildTrigger.getChildProjectsValue().replace(target.getName(), "");
                final Result threshold = buildTrigger.getThreshold();
                source.getPublishersList().remove(buildTrigger);
                source.getPublishersList().add(new BuildTrigger(childProjectsValue, threshold));
                source.save();
                target.save();
                Jenkins.getInstance().rebuildDependencyGraph();
            }
        }
    }
}
