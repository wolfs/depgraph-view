package hudson.plugins.depgraph_view.model.display;

import com.google.common.collect.ArrayListMultimap;
import hudson.plugins.depgraph_view.model.graph.DependencyGraph;
import hudson.plugins.depgraph_view.model.graph.ProjectNode;

/**
 * @author wolfs
 */
public class LegendDotStringGenerator extends AbstractDotStringGenerator {

    public LegendDotStringGenerator() {
        super(new DependencyGraph(), ArrayListMultimap.<ProjectNode, ProjectNode>create());
    }

    @Override
    public String generate() {
            /**** Build the dot source file ****/
            StringBuilder builder = new StringBuilder();

            builder.append("digraph {\n");
            builder.append("node [shape=box, style=rounded];\n");

            builder.append(cluster("Legend", legend()));

            builder.append("}");
            return builder.toString();
    }

    private String legend() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("label=\"Legend:\" labelloc=t centered=false color=black node [shape=plaintext]")
                .append("\"Dependency Graph\"\n")
                .append("\"Copy Artifact\"\n")
                .append("\"Sub-Project\"\n")
                .append("node [style=invis]\n")
                .append("a [label=\"\"] b [label=\"\"]")
                .append(" c [fillcolor=" + escapeString(subProjectColor) + " style=filled fontcolor="
                        + escapeString(subProjectColor) + "]\n")
                .append("a -> b [style=invis]\n")
                .append("{rank=same a -> \"Dependency Graph\" [color=black style=bold minlen=2]}\n")
                .append("{rank=same b -> \"Copy Artifact\" [color=lightblue minlen=2]}\n")
                .append("{rank=same c -> \"Sub-Project\" [ style=invis]}\n");
        return stringBuilder.toString();
    }

}
