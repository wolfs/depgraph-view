package hudson.plugins.depgraph_view;

import hudson.model.AbstractProject;
import hudson.model.DependencyGraph;
import hudson.model.DependencyGraph.Dependency;
import hudson.model.Hudson;
import hudson.model.Item;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CalculateDeps {
	private static final Logger LOGGER = Logger.getLogger(CalculateDeps.class.getName());
	private final Set<Dependency> visitedDeps = new HashSet<Dependency>();
	private final Set<AbstractProject<?,?>> visitedProj = new HashSet<AbstractProject<?,?>>();
	private boolean calculated = false;

	public CalculateDeps(AbstractProject<?, ?> proj) {
		visitedProj.add(proj);
	}

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
						addNewDependencies(getRealDependencies(depGraph.getUpstreamDependencies(project)),true));
				newProj.addAll(
						addNewDependencies(getRealDependencies(depGraph.getDownstreamDependencies(project)),false));
			}
		}
		visitedProj.addAll(newProj);
		if (!newProj.isEmpty()) {
			calculateNodesAndDependencies(newProj);
		}
	}

	private Set<Dependency> getRealDependencies(Collection<Dependency> depGroups) {
		Set<Dependency> deps = new HashSet<Dependency>();
		for (Dependency dependency : depGroups) {
			deps.addAll(getDependenciesFromDepGroup(dependency));
		}
		return deps;
	}

	private Set<AbstractProject<?, ?>> addNewDependencies(Set<Dependency> dependencies, boolean isUpstream) {
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

	private Set<Dependency> getDependenciesFromDepGroup(Dependency dep) {
		Class<?>[] declaredClasses = DependencyGraph.class.getDeclaredClasses();
		Class<?> depGroup = null;
		for (Class<?> clazz : declaredClasses) {
			if (clazz.getName().endsWith("DependencyGroup")) {
				depGroup = clazz;
				break;
			}
		}
		if (depGroup == null) {
			LOGGER.log(Level.SEVERE,"Error extracting dependencies vom DependencyGroup");
			return Collections.singleton(dep);
		}
		try {
			Field declaredField = depGroup.getDeclaredField("group");
			declaredField.setAccessible(true);
			@SuppressWarnings("unchecked")
			Set<Dependency> subDeps = (Set<Dependency>) declaredField.get(dep);
			return subDeps;
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE,"Error extracting dependencies vom DependencyGroup",e);
			return Collections.singleton(dep);
		}
	}


	public Set<AbstractProject<?, ?>> getProjects() {
		if (!calculated) {
			calculateNodesAndDependencies();
		}
		return visitedProj;
	}
	public Set<Dependency> getDependencies() {
		if (!calculated) {
			calculateNodesAndDependencies();
		}
		return visitedDeps;
	}

}
