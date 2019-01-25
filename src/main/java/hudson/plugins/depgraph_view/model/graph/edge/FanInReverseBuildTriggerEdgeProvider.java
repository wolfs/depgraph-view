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

package hudson.plugins.depgraph_view.model.graph.edge;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.lonkar.jobfanin.FanInReverseBuildTrigger;

import hudson.model.Items;
import hudson.model.Job;
import hudson.triggers.Trigger;
import jenkins.model.Jenkins;
import jenkins.model.ParameterizedJobMixIn.ParameterizedJob;

/**
 * {@link EdgeProvider} yielding the dependencies of the JobFanIn Plugin {@link FanInReverseBuildTrigger} trigger.
 */
public class FanInReverseBuildTriggerEdgeProvider implements EdgeProvider {

	private final Jenkins jenkins;

	@Inject
	public FanInReverseBuildTriggerEdgeProvider(Jenkins jenkins) {
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
		if (project instanceof ParameterizedJob<?, ?>) {
			for (Trigger<?> trigger : ((ParameterizedJob<?, ?>) project).getTriggers().values()) {
				if (trigger instanceof FanInReverseBuildTrigger) {
					for (Job<?, ?> upstream : Items.fromNameList(project.getParent(),
							((FanInReverseBuildTrigger) trigger).getUpstreamProjects(), Job.class)) {
						edges.add(new DependencyEdge(upstream, project));
					}
				}
			}
		}
		return edges;
	}

	private List<Edge> getDownstreamEdges(Job<?, ?> project) {
		List<Edge> edges = new ArrayList<>();
		for (ParameterizedJob<?, ?> downstream : jenkins.allItems(ParameterizedJob.class)) {
			for (Trigger<?> trigger : downstream.getTriggers().values()) {
				if (downstream instanceof Job<?, ?> && trigger instanceof FanInReverseBuildTrigger
						&& Items.fromNameList(project.getParent(),
								((FanInReverseBuildTrigger) trigger).getUpstreamProjects(), Job.class)
								.contains(project)) {
					edges.add(new DependencyEdge(project, (Job<?, ?>) downstream));
				}
			}
		}
		return edges;
	}

}
