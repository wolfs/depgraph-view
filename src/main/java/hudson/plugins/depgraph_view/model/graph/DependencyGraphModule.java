/*
 * Copyright (c) 2012 Stefan Wolf
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package hudson.plugins.depgraph_view.model.graph;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.multibindings.Multibinder;

import hudson.Extension;
import hudson.plugins.depgraph_view.model.graph.edge.BuildTriggerEdgeProvider;
import hudson.plugins.depgraph_view.model.graph.edge.CopyArtifactEdgeProvider;
import hudson.plugins.depgraph_view.model.graph.edge.DependencyGraphEdgeProvider;
import hudson.plugins.depgraph_view.model.graph.edge.EdgeProvider;
import hudson.plugins.depgraph_view.model.graph.edge.FanInReverseBuildTriggerEdgeProvider;
import hudson.plugins.depgraph_view.model.graph.edge.ParameterizedTriggerBuilderEdgeProvider;
import hudson.plugins.depgraph_view.model.graph.edge.ParameterizedTriggerEdgeProvider;
import hudson.plugins.depgraph_view.model.graph.edge.PipelineGraphPublisherEdgeProvider;
import hudson.plugins.depgraph_view.model.graph.edge.ReverseBuildTriggerEdgeProvider;
import hudson.plugins.depgraph_view.model.graph.project.ParameterizedTriggerSubProjectProvider;
import hudson.plugins.depgraph_view.model.graph.project.SubProjectProvider;

/**
 * Guice Module for the DependencyGraph
 */
@Extension
public class DependencyGraphModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder<EdgeProvider> edgeProviderMultibinder = Multibinder.newSetBinder(binder(), EdgeProvider.class);
        edgeProviderMultibinder.addBinding().to(BuildTriggerEdgeProvider.class);
        edgeProviderMultibinder.addBinding().to(ReverseBuildTriggerEdgeProvider.class);
        edgeProviderMultibinder.addBinding().to(FanInReverseBuildTriggerEdgeProvider.class);
        edgeProviderMultibinder.addBinding().to(ParameterizedTriggerEdgeProvider.class);
        edgeProviderMultibinder.addBinding().to(ParameterizedTriggerBuilderEdgeProvider.class);
        edgeProviderMultibinder.addBinding().to(CopyArtifactEdgeProvider.class);
        edgeProviderMultibinder.addBinding().to(PipelineGraphPublisherEdgeProvider.class);
        edgeProviderMultibinder.addBinding().to(DependencyGraphEdgeProvider.class);
        Multibinder<SubProjectProvider> subProjectProviderMultibinder = Multibinder.newSetBinder(binder(),Key.get(SubProjectProvider.class));
        subProjectProviderMultibinder.addBinding().to(ParameterizedTriggerSubProjectProvider.class);
    }
}
