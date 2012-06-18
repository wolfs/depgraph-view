/*
 * The MIT License
 *
 * Copyright (c) 2012, Dominik Bartholdi
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

import static com.google.common.base.Functions.compose;
import static com.google.common.collect.Lists.transform;
import hudson.model.DependencyGraph;
import hudson.model.AbstractProject;
import hudson.plugins.depgraph_view.DepNode.Edge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.json.JSONObject;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

/**
 * @author Dominik Bartholdi (imod)
 */
public class JsonStringGenerator {
    // Lexicographic order of the dependencies
    private static final Comparator<DependencyGraph.Dependency> DEP_COMPARATOR = new Comparator<DependencyGraph.Dependency>() {
        @Override
        public int compare(DependencyGraph.Dependency o1, DependencyGraph.Dependency o2) {
            int down = (PROJECT_COMPARATOR.compare(o1.getDownstreamProject(), o2.getDownstreamProject()));
            return down != 0 ? down : PROJECT_COMPARATOR.compare(o1.getUpstreamProject(), o2.getUpstreamProject());
        }
    };

    // Compares projects by name
    private static final Comparator<AbstractProject<?, ?>> PROJECT_COMPARATOR = new Comparator<AbstractProject<?, ?>>() {
        @Override
        public int compare(AbstractProject<?, ?> o1, AbstractProject<?, ?> o2) {
            return o1.getFullDisplayName().compareTo(o2.getFullDisplayName());
        }
    };

    private static final Function<AbstractProject<?, ?>, String> PROJECT_NAME_FUNCTION = new Function<AbstractProject<?, ?>, String>() {
        @Override
        public String apply(AbstractProject<?, ?> from) {
            return from.getFullDisplayName();
        }
    };

    private static final Function<String, String> ESCAPE = new Function<String, String>() {
        @Override
        public String apply(String from) {
            return escapeString(from);
        }
    };

    private List<AbstractProject<?, ?>> standaloneProjects;
    private List<AbstractProject<?, ?>> projectsInDeps;
    private List<DependencyGraph.Dependency> updownstreamDeps;
    private ListMultimap<AbstractProject<?, ?>, AbstractProject<?, ?>> subJobs;
    private List<DependencyGraph.Dependency> copiedArtifactDeps;

    public JsonStringGenerator(Set<AbstractProject<?, ?>> projects, Set<DependencyGraph.Dependency> deps,
            ListMultimap<AbstractProject<?, ?>, AbstractProject<?, ?>> subJobs, Set<DependencyGraph.Dependency> copied) {
        this.subJobs = subJobs;

        /* Sort dependencies (by downstream task first) */
        updownstreamDeps = new ArrayList<DependencyGraph.Dependency>(deps);
        Collections.sort(updownstreamDeps, DEP_COMPARATOR);

        Set<AbstractProject<?, ?>> depProjects = listUniqueProjectsInDependencies(updownstreamDeps);

        /* Sort artifact-copy dependencies (by downstream task first) */
        copiedArtifactDeps = new ArrayList<DependencyGraph.Dependency>(copied);
        Collections.sort(copiedArtifactDeps, DEP_COMPARATOR);
        /**/

        Set<AbstractProject<?, ?>> artifactsCopiedProjects = listUniqueProjectsInDependencies(copiedArtifactDeps);

        projectsInDeps = Lists.newArrayList();
        projectsInDeps.addAll(depProjects);
        projectsInDeps.addAll(artifactsCopiedProjects);
        Collections.sort(projectsInDeps, PROJECT_COMPARATOR);

        /* Find all projects without dependencies or copied artifacts (stand-alone projects) */
        standaloneProjects = new ArrayList<AbstractProject<?, ?>>(projects);
        standaloneProjects.removeAll(projectsInDeps);
        Collections.sort(standaloneProjects, PROJECT_COMPARATOR);

    }

    Map<String, DepNode> jobs = new HashMap<String, DepNode>();

    /**
     * Generates the json for the given projects and dependencies
     * 
     * @return json model
     */
    public String generate() {

        // Stuff not linked to other stuff
        List<String> standaloneNames = transform(standaloneProjects, compose(ESCAPE, PROJECT_NAME_FUNCTION));
        standaloneNames.removeAll(projectsInDeps);

        List<Map<String, String>> edges = new ArrayList<Map<String, String>>();

        for (DependencyGraph.Dependency dep : updownstreamDeps) {
            addEdge(edges, "dep", dep);
        }

        // copied artifact dependencies
        for (DependencyGraph.Dependency dep : copiedArtifactDeps) {
            addEdge(edges, "copy", dep);
        }

        for (String name : standaloneNames) {
            // make sure nodes also exist for standalone jobs
            getOrCreateDepNode(name);
        }

        List<Set<DepNode>> clusters = Lists.newArrayList();

        for (DepNode node : jobs.values()) {
            boolean inCluster = false;
            for (Set<DepNode> cluster : clusters) {
                if (cluster.contains(node)) {
                    inCluster = true;
                    break;
                }
            }
            if (!inCluster) {
                final Set<DepNode> newHashSet = Sets.newHashSet();
                buildCluster(newHashSet, node);
                clusters.add(newHashSet);
            }
        }

        List<Map<Integer, List<String>>> clusterList = Lists.newArrayList();

        for (Set<DepNode> set : clusters) {
            Function<DepNode, Integer> levelFunction = new Function<DepNode, Integer>() {
                public Integer apply(DepNode depNode) {
                    return depNode.getLevel();
                }
            };

            final ImmutableListMultimap<Integer, DepNode> jobsByLevel = Multimaps.index(set, levelFunction);
            final ImmutableSet<Entry<Integer, Collection<DepNode>>> jobsByLevelEntries = jobsByLevel.asMap().entrySet();
            final Map<Integer, List<String>> levelsWithJobs = Maps.newHashMap();

            for (Entry<Integer, Collection<DepNode>> entry : jobsByLevelEntries) {
                final Collection<DepNode> nodes = entry.getValue();
                List<String> levelNodes = Lists.newArrayList();
                for (DepNode job : nodes) {
                    levelNodes.add(job.getName());
                }
                levelsWithJobs.put(entry.getKey(), levelNodes);
            }
            clusterList.add(levelsWithJobs);
        }

        JSONObject json = new JSONObject();
        json.put("edges", edges);
        json.put("clusters", clusterList);

        final String jsonStr = json.toString(2);
//        System.out.println(jsonStr);
        return jsonStr;
    }

    /**
     * Performs full tree traversal using recursion.
     */
    public void buildCluster(Set<DepNode> clusterSet, DepNode node) {
        if (clusterSet.contains(node)) {
            return;
        }
        clusterSet.add(node);
        // traverse all nodes that belong to the parent
        for (Edge edge : node.getParents()) {
            buildCluster(clusterSet, edge.getEndpoint());
        }
        for (Edge edge : node.getChilds()) {
            buildCluster(clusterSet, edge.getEndpoint());
        }
    }

    /**
     * Adds an edge between the jobs of the dependency.
     * 
     * @param edges
     *            the edges to add the dependency to (results in the json)
     * @param dep
     *            the dependency to to be added
     */
    private void addEdge(List<Map<String, String>> edges, String type, DependencyGraph.Dependency dep) {
        Map<String, String> edge = new HashMap<String, String>();
        final String fullDisplayNameFrom = dep.getUpstreamProject().getFullDisplayName();
        final String fullDisplayNameTo = dep.getDownstreamProject().getFullDisplayName();
        edge.put("from", fullDisplayNameFrom);
        edge.put("to", fullDisplayNameTo);
        edge.put("type", type);
        edges.add(edge);

        final DepNode from = getOrCreateDepNode(fullDisplayNameFrom);
        final DepNode to = getOrCreateDepNode(fullDisplayNameTo);
        Edge.addEdge(from, to);
    }

    private DepNode getOrCreateDepNode(final String name) {
        DepNode node = jobs.get(name);
        if (node == null) {
            node = new DepNode(name);
            jobs.put(name, node);
        }
        return node;
    }

    private Set<AbstractProject<?, ?>> listUniqueProjectsInDependencies(List<DependencyGraph.Dependency> dependencies) {
        Set<AbstractProject<?, ?>> set = new HashSet<AbstractProject<?, ?>>();
        for (DependencyGraph.Dependency dependency : dependencies) {
            set.add(dependency.getUpstreamProject());
            set.add(dependency.getDownstreamProject());
        }
        return set;
    }

    private static String escapeString(String toEscape) {
        return toEscape;
    }
}
