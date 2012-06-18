package hudson.plugins.depgraph_view.operations;

import hudson.tasks.BuildTrigger;

import java.io.IOException;

import jenkins.model.Jenkins;

public class PutEdgeOperation extends EdgeOperation {

    public PutEdgeOperation(String sourceJobName, String targetJobName) {
        super(sourceJobName, targetJobName);
    }

    public void perform() throws IOException {
        if (source != null && target != null) {
            final BuildTrigger buildTrigger = (BuildTrigger) source.getPublishersList().get(BuildTrigger.class);
            if (buildTrigger == null) {
                source.getPublishersList().add(new BuildTrigger(target.getName(), true));
            } else {
                final String childProjectsValue = buildTrigger.getChildProjectsValue() + ", " + target.getName();
                source.getPublishersList().remove(buildTrigger);
                source.getPublishersList().add(new BuildTrigger(childProjectsValue, true));
            }
            source.save();
            target.save();
            Jenkins.getInstance().rebuildDependencyGraph();
        }
    }
}
