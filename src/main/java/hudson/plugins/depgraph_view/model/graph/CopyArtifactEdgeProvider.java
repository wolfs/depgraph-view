package hudson.plugins.depgraph_view.model.graph;

import com.google.common.collect.Sets;
import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.plugins.copyartifact.CopyArtifact;
import hudson.tasks.Builder;
import jenkins.model.Jenkins;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

import static hudson.plugins.depgraph_view.model.graph.ProjectNode.node;

/**
 * @author wolfs
 */
public class CopyArtifactEdgeProvider implements EdgeProvider {

    private boolean copyartifactIsInstalled;

    @Inject
    CopyArtifactEdgeProvider(Jenkins jenkins) {
        copyartifactIsInstalled = jenkins.getPlugin("copyartifact") != null;
    }

    @Override
    public Iterable<Edge> getEdgesIncidentWith(AbstractProject<?, ?> project) {
        Set<Edge> artifactEdges = Sets.newHashSet();

        if (copyartifactIsInstalled) {
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
                                    new CopyArtifactEdge(node(projectFromName), node(project)));
                        }
                    }
                }
            }
        }
        return artifactEdges;
    }
}
