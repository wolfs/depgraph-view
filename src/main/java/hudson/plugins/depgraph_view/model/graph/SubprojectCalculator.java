package hudson.plugins.depgraph_view.model.graph;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Set;

/**
 * @author wolfs
 */
public class SubprojectCalculator {

    private Set<SubProjectProvider> providers;

    @Inject
    SubprojectCalculator(Set<SubProjectProvider> providers) {
        this.providers = providers;
    }

    public ListMultimap<ProjectNode, ProjectNode> generate(DependencyGraph graph) {
        ListMultimap<ProjectNode, ProjectNode> project2Subprojects = ArrayListMultimap.create();
        Collection<ProjectNode> nodes = graph.getNodes();
        for (ProjectNode node : nodes) {
            for (SubProjectProvider provider : providers) {
                project2Subprojects.putAll(node, provider.getSubProjectsOf(node.getProject()));
            }
        }
        return project2Subprojects;
    }
}
