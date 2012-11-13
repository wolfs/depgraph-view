package hudson.plugins.depgraph_view.model.graph;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import hudson.Extension;

/**
 * @author wolfs
 */
@Extension
public class DependencyGraphModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder<EdgeProvider> edgeProviderMultibinder = Multibinder.newSetBinder(binder(), EdgeProvider.class);
        edgeProviderMultibinder.addBinding().to(DependencyGraphEdgeProvider.class);
        edgeProviderMultibinder.addBinding().to(CopyArtifactEdgeProvider.class);
        Multibinder<SubProjectProvider> subProjectProviderMultibinder = Multibinder.newSetBinder(binder(), SubProjectProvider.class);
        subProjectProviderMultibinder.addBinding().to(ParameterizedTriggerSubProjectProvider.class);
    }
}
