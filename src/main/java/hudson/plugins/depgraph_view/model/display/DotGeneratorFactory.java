package hudson.plugins.depgraph_view.model.display;

import com.google.common.collect.ListMultimap;
import hudson.plugins.depgraph_view.model.graph.DependencyGraph;
import hudson.plugins.depgraph_view.model.graph.ProjectNode;

/**
 * @author wolfs
 */
public class DotGeneratorFactory extends GeneratorFactory {
    @Override
    public AbstractGraphStringGenerator newGenerator(DependencyGraph graph, ListMultimap<ProjectNode, ProjectNode> subprojects) {
        return new DotStringGenerator(graph, subprojects);
    }

    @Override
    public AbstractGraphStringGenerator newLegendGenerator() {
        return new LegendDotStringGenerator();
    }
}
