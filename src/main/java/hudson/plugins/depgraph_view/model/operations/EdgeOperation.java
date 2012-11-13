package hudson.plugins.depgraph_view.model.operations;

import java.io.IOException;

import hudson.model.AbstractProject;
import jenkins.model.Jenkins;

public abstract class EdgeOperation {
    protected final AbstractProject<?, ?> source;
    protected final AbstractProject<?, ?> target;

    public EdgeOperation(String sourceJobName, String targetJobName) {
        this.source = Jenkins.getInstance().getItemByFullName(sourceJobName, AbstractProject.class);
        this.target = Jenkins.getInstance().getItemByFullName(targetJobName, AbstractProject.class);
    }

    public abstract void perform() throws IOException;
}
