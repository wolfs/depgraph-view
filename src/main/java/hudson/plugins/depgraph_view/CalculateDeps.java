/*
 * Copyright (c) 2010 Stefan Wolf
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

/**
 * Class to calculate the connected components of the
 * dependency graph containing a set of projects.
 * After calculation one can obtain the set of dependencies and
 * the set of projects of the connected components.
 * Respects read permission of the projects.
 *
 * @author wolfs
 */
public class CalculateDeps {
    private static final Logger LOGGER = Logger.getLogger(CalculateDeps.class.getName());
    private final Set<Dependency> visitedDeps = new HashSet<Dependency>();
    private final Set<AbstractProject<?,?>> visitedProj = new HashSet<AbstractProject<?,?>>();
    private boolean calculated = false;
    private DependencyGraph dependencyGraph;

    public CalculateDeps(Collection<? extends AbstractProject<?, ?>> projects) {
        this.dependencyGraph = Hudson.getInstance().getDependencyGraph();
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
        for (AbstractProject<?,?> project : fromProjects) {
            if (project.hasPermission(Item.READ)) {
                newProj.addAll(
                        addNewDependencies(dependencyGraph.getUpstreamDependencies(project),true));
                newProj.addAll(
                        addNewDependencies(dependencyGraph.getDownstreamDependencies(project),false));
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

    /**
     * Calculates the connected components if necessary
     * @return projects (nodes) in the connected components
     */
    public Set<AbstractProject<?, ?>> getProjects() {
        if (!calculated) {
            calculateNodesAndDependencies();
        }
        return Collections.unmodifiableSet(visitedProj);
    }

    /**
     * Calculates the connected components if necessary
     * @return dependencies (edges) in the connected components
     */
    public Set<Dependency> getDependencies() {
        if (!calculated) {
            calculateNodesAndDependencies();
        }
        return Collections.unmodifiableSet(visitedDeps);
    }

}
