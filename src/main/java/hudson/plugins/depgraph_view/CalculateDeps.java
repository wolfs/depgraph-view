package hudson.plugins.depgraph_view;

import hudson.model.AbstractProject;
import hudson.model.DependencyGraph;
import hudson.model.DependencyGraph.Dependency;
import hudson.model.Hudson;
import hudson.model.Item;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class CalculateDeps {
	private static final Logger LOGGER = Logger.getLogger(CalculateDeps.class.getName());
	private final Set<Dependency> visitedDeps = new HashSet<Dependency>();
	private final Set<AbstractProject<?,?>> visitedProj = new HashSet<AbstractProject<?,?>>();
	private boolean calculated = false;

	public CalculateDeps(Collection<? extends AbstractProject<?, ?>> projects) {
		visitedProj.addAll(projects);
	}

	public void calculateNodesAndDependencies() {
		if (!calculated) {
			calculateNodesAndDependencies(visitedProj);
			calculated = true;
		}
	}

	private void calculateNodesAndDependencies(Set<AbstractProject<?, ?>> fromProjects) {
		Set<AbstractProject<?,?>> newProj = new HashSet<AbstractProject<?, ?>>();
		DependencyGraph depGraph = Hudson.getInstance().getDependencyGraph();
		for (AbstractProject<?,?> project : fromProjects) {
			if (project.hasPermission(Item.READ)) {
				newProj.addAll(
						addNewDependencies(depGraph.getUpstreamDependencies(project),true));
				newProj.addAll(
						addNewDependencies(depGraph.getDownstreamDependencies(project),false));
			}
		}
		visitedProj.addAll(newProj);
		if (!newProj.isEmpty()) {
			calculateNodesAndDependencies(newProj);
		}
	}

	private Set<AbstractProject<?, ?>> addNewDependencies(Collection<Dependency> dependencies, boolean isUpstream) {
		Set<AbstractProject<?,?>> newProj = new HashSet<AbstractProject<?, ?>>();
		for (Dependency dep : dependencies) {
			AbstractProject<?,?> projectToAdd = isUpstream ? dep.getUpstreamProject() : dep.getDownstreamProject();
			if (projectToAdd.hasPermission(Item.READ) && !visitedDeps.contains(dep)) {
				visitedDeps.add(dep);
				if (!visitedProj.contains(projectToAdd)) {
					newProj.add(projectToAdd);
				}
			}
		}
		return newProj;
	}

	public Set<AbstractProject<?, ?>> getProjects() {
		if (!calculated) {
			calculateNodesAndDependencies();
		}
		return Collections.unmodifiableSet(visitedProj);
	}
	public Set<Dependency> getDependencies() {
		if (!calculated) {
			calculateNodesAndDependencies();
		}
		return Collections.unmodifiableSet(visitedDeps);
	}

}
