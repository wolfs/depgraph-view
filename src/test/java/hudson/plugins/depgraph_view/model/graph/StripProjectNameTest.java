package hudson.plugins.depgraph_view.model.graph;

import static hudson.plugins.depgraph_view.model.graph.ProjectNode.node;
import static org.junit.Assert.assertTrue;
import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.plugins.depgraph_view.DependencyGraphProperty.DescriptorImpl;
import hudson.plugins.depgraph_view.model.graph.edge.DependencyGraphEdgeProvider;
import hudson.plugins.depgraph_view.model.graph.edge.EdgeProvider;
import hudson.plugins.depgraph_view.model.graph.project.SubProjectProvider;
import hudson.plugins.depgraph_view.model.display.DotStringGenerator;

import java.util.Collections;

import jenkins.model.JenkinsLocationConfiguration;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;

public class StripProjectNameTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void projectNameShouldBeStriped() throws Exception {
        JenkinsLocationConfiguration.get().setUrl("http://localhost");
        j.getInstance().getDescriptorByType(DescriptorImpl.class).setProjectNameStripRegex("com.comp.(.*)");
        j.getInstance().getDescriptorByType(DescriptorImpl.class).setProjectNameStripRegexGroup(1);

        final FreeStyleProject myJob = j.createFreeStyleProject("com.comp.myJob");
        j.getInstance().rebuildDependencyGraph();
        DependencyGraph graph = generateGraph(myJob);
        final SubprojectCalculator subprojectCalculator = new SubprojectCalculator(Collections.<SubProjectProvider>emptySet());
        final ListMultimap<ProjectNode, ProjectNode> subProjects = subprojectCalculator.generate(graph);
        final DotStringGenerator dotStringGenerator = new DotStringGenerator(j.getInstance(), graph, subProjects);
        final String dotString = dotStringGenerator.generate();
        System.err.println(dotString);
        // we can't test for not existing of the original name, because that one is still required for linking, 
        // so we explicitly check for the short/striped version only
        assertTrue("node label should contain stripped job name", dotString.contains(">myJob<"));

    }

    @Test
    public void projectNameShouldBeRelabeled() throws Exception {
        JenkinsLocationConfiguration.get().setUrl("http://localhost");
        j.getInstance().getDescriptorByType(DescriptorImpl.class).setProjectNameStripRegex("(com[.]comp[.])(.*)");
        j.getInstance().getDescriptorByType(DescriptorImpl.class).setProjectNameStripRegexGroup(2);
        j.getInstance().getDescriptorByType(DescriptorImpl.class).setProjectNameSuperscriptRegexGroup(1);
        
        final FreeStyleProject myJob = j.createFreeStyleProject("com.comp.myJob");
        j.getInstance().rebuildDependencyGraph();
        DependencyGraph graph = generateGraph(myJob);
        final SubprojectCalculator subprojectCalculator = new SubprojectCalculator(Collections.<SubProjectProvider>emptySet());
        final ListMultimap<ProjectNode, ProjectNode> subProjects = subprojectCalculator.generate(graph);
        final DotStringGenerator dotStringGenerator = new DotStringGenerator(j.getInstance(), graph, subProjects);
        final String dotString = dotStringGenerator.generate();
        System.err.println(dotString);
        // we can't test for not existing of the original name, because that one is still required for linking, 
        // so we explicitly check for the short/striped version only
        assertTrue("node label should contain superscript and stripped name", dotString.contains("<FONT POINT-SIZE=\"10\">com.comp.</FONT><BR />myJob"));
        
    }

    private DependencyGraph generateGraph(AbstractProject<?,?> from) {
        return new GraphCalculator(getDependencyGraphEdgeProviders()).generateGraph(Collections.singleton(node(from)));
    }
    
    private ImmutableSet<EdgeProvider> getDependencyGraphEdgeProviders() {
        return ImmutableSet.<EdgeProvider>of(new DependencyGraphEdgeProvider(j.getInstance()));
    }
}
