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

import com.google.common.base.Function;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import hudson.model.AbstractProject;
import hudson.model.DependencyGraph;
import hudson.plugins.depgraph_view.model.Edge;
import hudson.plugins.depgraph_view.model.Graph;
import hudson.plugins.depgraph_view.model.ProjectNode;
import net.sf.json.JSONObject;

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

import static com.google.common.collect.Lists.transform;

/**
 * @author Dominik Bartholdi (imod)
 */
public class JsonStringGenerator {
    // Lexicographic order of the dependencies
    private static final Comparator<Edge> DEP_COMPARATOR = new Comparator<Edge>() {
        @Override
        public int compare(Edge o1, Edge o2) {
            int down = (NODE_COMPARATOR.compare(o1.target, o2.target));
            return down != 0 ? down : NODE_COMPARATOR.compare(o1.source, o2.source);
        }
    };

    // Compares projects by name
    private static final Comparator<ProjectNode> NODE_COMPARATOR = new Comparator<ProjectNode>() {
        @Override
        public int compare(ProjectNode o1, ProjectNode o2) {
            return PROJECT_COMPARATOR.compare(o1.getProject(), o2.getProject());
        }
    };

    // Compares projects by name
    private static final Comparator<AbstractProject<?, ?>> PROJECT_COMPARATOR = new Comparator<AbstractProject<?, ?>>() {
        @Override
        public int compare(AbstractProject<?, ?> o1, AbstractProject<?, ?> o2) {
            return o1.getFullDisplayName().compareTo(o2.getFullDisplayName());
        }
    };

    private static final Function<ProjectNode, String> PROJECT_NAME_FUNCTION = new Function<ProjectNode, String>() {
        @Override
        public String apply(ProjectNode from) {
            return from.getName();
        }
    };

//    private static final Function<String, String> ESCAPE = new Function<String, String>() {
//        @Override
//        public String apply(String from) {
//            return escapeString(from);
//        }
//    };

    private ArrayList<ProjectNode> standaloneProjects;
    private List<ProjectNode> projectsInDeps;
    private List<Edge> edges;
    private ListMultimap<AbstractProject<?, ?>, AbstractProject<?, ?>> subJobs;

    public JsonStringGenerator(Graph graph) {
        // TODO: Build subjobs
        this.subJobs = ArrayListMultimap.create();


        this.edges = new ArrayList<Edge>();
        this.edges.addAll(graph.getEdges());

        /* Sort dependencies (by downstream task first) */

        /* Find all projects without dependencies or copied artifacts (stand-alone projects) */
        standaloneProjects = new ArrayList<ProjectNode>();
        standaloneProjects.addAll(graph.getIsolatedNodes());
        Collections.sort(standaloneProjects, NODE_COMPARATOR);

        projectsInDeps = Lists.newArrayList();
        projectsInDeps.addAll(graph.getNodes());
        projectsInDeps.removeAll(standaloneProjects);
        Collections.sort(projectsInDeps, NODE_COMPARATOR);
    }

    Map<String, DepNode> jobs = new HashMap<String, DepNode>();

    /**
     * Generates the json for the given projects and dependencies
     *
     * @return json model
     */
    public String generate() {

        // Stuff not linked to other stuff
        List<String> standaloneNames = transform(standaloneProjects, PROJECT_NAME_FUNCTION);

        List<Map<String, String>> edges = new ArrayList<Map<String, String>>();

        for (Edge edge : this.edges) {
            addEdge(edges, edge);
        }

        for (String name : standaloneNames) {
            // make sure nodes also exist for standalone jobs
            getOrCreateDepNode(name);
        }

        // a cluster is a group of jobs having at least one connection to each other through edges
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
        for (DepNode.Edge edge : node.getParents()) {
            buildCluster(clusterSet, edge.getEndpoint());
        }
        for (DepNode.Edge edge : node.getChilds()) {
            buildCluster(clusterSet, edge.getEndpoint());
        }
    }

    /**
     * Adds an edge between the jobs of the dependency.
     *
     * @param edges
     *            the edges to add the dependency to (results in the json)
     * @param edge
     *            the dependency to to be added
     */
    private void addEdge(List<Map<String, String>> edges, Edge edge) {
        Map<String, String> jsonEdge = new HashMap<String, String>();
        final String fullDisplayNameFrom = edge.source.getName();
        final String fullDisplayNameTo = edge.target.getName();
        jsonEdge.put("from", fullDisplayNameFrom);
        jsonEdge.put("to", fullDisplayNameTo);
        jsonEdge.put("type", edge.getType());
        edges.add(jsonEdge);

        final DepNode from = getOrCreateDepNode(fullDisplayNameFrom);
        final DepNode to = getOrCreateDepNode(fullDisplayNameTo);
        DepNode.Edge.addEdge(from, to);
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

}
