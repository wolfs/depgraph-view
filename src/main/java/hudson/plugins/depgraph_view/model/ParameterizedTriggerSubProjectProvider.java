package hudson.plugins.depgraph_view.model;

import hudson.Plugin;
import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.model.Hudson;
import hudson.plugins.parameterizedtrigger.BlockableBuildTriggerConfig;
import hudson.plugins.parameterizedtrigger.TriggerBuilder;
import hudson.tasks.Builder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wolfs
 */
public class ParameterizedTriggerSubProjectProvider implements SubProjectProvider {

    @Override
    public Iterable<ProjectNode> getSubProjectsOf(AbstractProject<?, ?> project) {
        List<ProjectNode> subProjects = new ArrayList<ProjectNode>();

        Plugin parameterizedTrigger = Hudson.getInstance().getPlugin("parameterized-trigger");
        if (parameterizedTrigger != null) {

            if(project instanceof FreeStyleProject) {

                FreeStyleProject proj = (FreeStyleProject) project;
                List<Builder> builders = proj.getBuilders();

                for (Builder builder : builders) {

                    if (builder instanceof TriggerBuilder) {

                        TriggerBuilder tBuilder = (TriggerBuilder) builder;
                        for (BlockableBuildTriggerConfig config : tBuilder.getConfigs()) {

                            for (AbstractProject<?,?> abstractProject : config.getProjectList(null)) {
                                subProjects.add( new ProjectNode(abstractProject) );
                            }
                        }
                    }
                }
            }
        }
        return subProjects;
    }
}
