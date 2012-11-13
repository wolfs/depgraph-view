package hudson.plugins.depgraph_view.model.display;

import com.google.common.base.Function;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import hudson.model.AbstractProject;
import hudson.plugins.depgraph_view.model.graph.DependencyGraph;
import hudson.plugins.depgraph_view.model.graph.Edge;
import hudson.plugins.depgraph_view.model.graph.ProjectNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author wolfs
 */
public abstract class AbstractGraphStringGenerator {
    // Lexicographic order of the dependencies
    protected static final Comparator<Edge> DEP_COMPARATOR = new Comparator<Edge>() {
        @Override
        public int compare(Edge o1, Edge o2) {
            int down = (NODE_COMPARATOR.compare(o1.target, o2.target));
            return down != 0 ? down : NODE_COMPARATOR.compare(o1.source, o2.source);
        }
    };

    // Compares project nodes by name
    protected static final Comparator<ProjectNode> NODE_COMPARATOR = new Comparator<ProjectNode>() {
        @Override
        public int compare(ProjectNode o1, ProjectNode o2) {
            return PROJECT_COMPARATOR.compare(o1.getProject(), o2.getProject());
        }
    };

    // Compares projects by name
    protected static final Comparator<AbstractProject<?, ?>> PROJECT_COMPARATOR = new Comparator<AbstractProject<?, ?>>() {
        @Override
        public int compare(AbstractProject<?, ?> o1, AbstractProject<?, ?> o2) {
            return o1.getFullDisplayName().compareTo(o2.getFullDisplayName());
        }
    };

    protected static final Function<ProjectNode, String> PROJECT_NAME_FUNCTION = new Function<ProjectNode, String>() {
        @Override
        public String apply(ProjectNode from) {
            return from.getName();
        }
    };

    protected ArrayList<ProjectNode> standaloneProjects;
    protected List<ProjectNode> projectsInDeps;
    protected List<Edge> edges;
    protected ListMultimap<ProjectNode, ProjectNode> subJobs;
    protected final DependencyGraph graph;

    protected AbstractGraphStringGenerator(DependencyGraph graph, ListMultimap<ProjectNode, ProjectNode> projects2Subprojects) {
        this.graph = graph;
        this.subJobs = projects2Subprojects;

        /* Sort dependencies (by downstream task first) */
        edges = Lists.newArrayList(graph.getEdges());
        Collections.sort(edges, DEP_COMPARATOR);

        /* Find all projects without dependencies or copied artifacts (stand-alone projects) */
        standaloneProjects = Lists.newArrayList(graph.getIsolatedNodes());
        Collections.sort(standaloneProjects, NODE_COMPARATOR);

        projectsInDeps = Lists.newArrayList(graph.getNodes());
        projectsInDeps.removeAll(standaloneProjects);
        Collections.sort(projectsInDeps, NODE_COMPARATOR);
    }

    public abstract String generate();

}
