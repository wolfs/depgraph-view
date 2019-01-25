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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import hudson.model.AbstractProject;
import hudson.model.Items;
import hudson.model.Job;
import hudson.plugins.parameterizedtrigger.BuildTrigger;
import hudson.plugins.parameterizedtrigger.BuildTriggerConfig;
import jenkins.model.Jenkins;

/**
 * {@link EdgeProvider} yielding the dependencies of the Parameterized Trigger Plugin {@link BuildTrigger} publisher.
 */
public class ParameterizedTriggerEdgeProvider implements EdgeProvider {

	private final Jenkins jenkins;

	@Inject
	public ParameterizedTriggerEdgeProvider(Jenkins jenkins) {
		this.jenkins = jenkins;
	}

	@Override
	public Iterable<Edge> getEdgesIncidentWith(Job<?, ?> project) {

		List<Edge> edges = getUpstreamEdges(project);
		edges.addAll(getDownstreamEdges(project));

		return edges;
	}

	private List<Edge> getUpstreamEdges(Job<?, ?> project) {
		List<Edge> edges = new ArrayList<>();
		for (AbstractProject<?, ?> upstream : jenkins.allItems(AbstractProject.class)) {
			BuildTrigger buildTrigger = upstream.getPublishersList().get(BuildTrigger.class);
			if (buildTrigger != null) {
				for (BuildTriggerConfig config : buildTrigger.getConfigs()) {
					if (Items.fromNameList(upstream.getParent(), config.getProjects(), Job.class).contains(project)) {
						edges.add(new DependencyEdge(upstream, project));
					}
				}
			}
		}
		return edges;
	}

	private List<Edge> getDownstreamEdges(Job<?, ?> project) {
		List<Edge> edges = new ArrayList<>();
		if (project instanceof AbstractProject<?, ?>) {
			BuildTrigger buildTrigger = ((AbstractProject<?, ?>) project).getPublishersList().get(BuildTrigger.class);
			if (buildTrigger != null) {
				for (BuildTriggerConfig config : buildTrigger.getConfigs()) {
					for (Job<?, ?> downstream : Items.fromNameList(project.getParent(), config.getProjects(),
							Job.class)) {
						edges.add(new DependencyEdge(project, downstream));
					}
				}
			}
		}
		return edges;
	}

}
