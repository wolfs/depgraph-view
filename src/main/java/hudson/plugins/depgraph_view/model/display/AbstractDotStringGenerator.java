package hudson.plugins.depgraph_view.model.display;

import com.google.common.base.Joiner;
import com.google.common.collect.ListMultimap;
import hudson.plugins.depgraph_view.model.graph.DependencyGraph;
import hudson.plugins.depgraph_view.model.graph.ProjectNode;

/**
 * @author wolfs
 */
public abstract class AbstractDotStringGenerator extends AbstractGraphStringGenerator {
    protected String subProjectColor = "#F0F0F0";

    AbstractDotStringGenerator(DependencyGraph graph, ListMultimap<ProjectNode, ProjectNode> projects2Subprojects) {
        super(graph, projects2Subprojects);
    }

    protected static String escapeString(String toEscape) {
        return "\"" + toEscape + "\"";
    }

    protected String cluster(String name, String contents, String... options) {
        StringBuilder builder = new StringBuilder();
        builder.append("subgraph cluster" + name + " {\n");
        builder.append(contents);
        builder.append(Joiner.on("\n").join(options) + "}\n");
        return builder.toString();
    }
}
