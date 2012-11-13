package hudson.plugins.depgraph_view.model.display;

import com.google.common.collect.ListMultimap;
import hudson.plugins.depgraph_view.model.graph.DependencyGraph;
import hudson.plugins.depgraph_view.model.graph.ProjectNode;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * @author wolfs
 */
public class JsonGeneratorFactory extends GeneratorFactory {
    @Override
    public AbstractGraphStringGenerator newGenerator(DependencyGraph graph, ListMultimap<ProjectNode, ProjectNode> subprojects) {
        return new JsonStringGenerator(graph, subprojects);
    }

    @Override
    public AbstractGraphStringGenerator newLegendGenerator() {
        throw new NotImplementedException();
    }
}
