package hudson.plugins.depgraph_view.model.graph;

import com.google.common.base.Preconditions;
import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.plugins.parameterizedtrigger.BlockableBuildTriggerConfig;
import hudson.plugins.parameterizedtrigger.TriggerBuilder;
import hudson.tasks.Builder;
import jenkins.model.Jenkins;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static hudson.plugins.depgraph_view.model.graph.ProjectNode.node;

/**
 * @author wolfs
 */
public class ParameterizedTriggerSubProjectProvider implements SubProjectProvider {

    private final boolean isParameterizedTriggerPluginInstalled;

    @Inject
    ParameterizedTriggerSubProjectProvider(Jenkins jenkins) {
        isParameterizedTriggerPluginInstalled = jenkins.getPlugin("parameterized-trigger") != null;
    }

    @Override
    public Iterable<ProjectNode> getSubProjectsOf(AbstractProject<?, ?> project) {
        Preconditions.checkNotNull(project);
        if (!isParameterizedTriggerPluginInstalled) {
            return Collections.emptyList();
        }

        List<ProjectNode> subProjects = new ArrayList<ProjectNode>();
        if(project instanceof FreeStyleProject) {
            FreeStyleProject proj = (FreeStyleProject) project;
            List<Builder> builders = proj.getBuilders();
            for (Builder builder : builders) {
                if (builder instanceof TriggerBuilder) {
                    TriggerBuilder tBuilder = (TriggerBuilder) builder;
                    for (BlockableBuildTriggerConfig config : tBuilder.getConfigs()) {
                        for (AbstractProject<?,?> abstractProject : config.getProjectList(null)) {
                            subProjects.add(node(abstractProject) );
                        }
                    }
                }
            }
        }
        return subProjects;
    }
}
