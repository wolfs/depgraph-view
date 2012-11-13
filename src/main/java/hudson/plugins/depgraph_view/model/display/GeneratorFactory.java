package hudson.plugins.depgraph_view.model.display;

import com.google.common.collect.ListMultimap;
import hudson.plugins.depgraph_view.model.graph.DependencyGraph;
import hudson.plugins.depgraph_view.model.graph.ProjectNode;

/**
 * @author wolfs
 */
public abstract class GeneratorFactory {
    abstract public AbstractGraphStringGenerator newGenerator(DependencyGraph graph, ListMultimap<ProjectNode, ProjectNode> subprojects);
    abstract public AbstractGraphStringGenerator newLegendGenerator();
}
