package hudson.plugins.depgraph_view.model;

import com.google.common.collect.Sets;
import hudson.Plugin;
import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.model.Hudson;
import hudson.plugins.copyartifact.CopyArtifact;
import hudson.tasks.Builder;
import jenkins.model.Jenkins;

import java.util.List;
import java.util.Set;

/**
 * @author wolfs
 */
public class CopyArtifactEdgeProvider implements EdgeProvider {
    @Override
    public Iterable<Edge> getEdgesIncidentWith(AbstractProject<?, ?> project) {
        Set<Edge> artifactEdges = Sets.newHashSet();

        Plugin copyartifact = Hudson.getInstance().getPlugin("copyartifact");
        if (copyartifact != null) {
            if(project instanceof FreeStyleProject) {

                FreeStyleProject proj = (FreeStyleProject) project;
                List<Builder> builders = proj.getBuilders();

                for (Builder builder : builders) {

                    if (builder instanceof CopyArtifact) {

                        CopyArtifact caBuilder = (CopyArtifact) builder;
                        String projectName = caBuilder.getProjectName();
                        Jenkins jenkins = Jenkins.getInstance();
                        AbstractProject<?,?> projectFromName = jenkins.getItemByFullName(projectName, AbstractProject.class);

                        if (projectFromName != null) {
                            artifactEdges.add(
                                    new CopyArtifactEdge(new ProjectNode(projectFromName), new ProjectNode(project)));
                        }
                    }
                }
            }
        }
        return artifactEdges;
    }
}
