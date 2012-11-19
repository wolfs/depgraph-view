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

package hudson.plugins.depgraph_view.model.display;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.algorithms.filters.FilterUtils;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.Graph;
import hudson.plugins.depgraph_view.model.layout.JungSugiyama;
import hudson.plugins.depgraph_view.model.graph.DependencyGraph;
import hudson.plugins.depgraph_view.model.graph.Edge;
import hudson.plugins.depgraph_view.model.graph.ProjectNode;
import net.sf.json.JSONObject;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static com.google.common.collect.Ordering.natural;

/**
 * Generates a Json representation of the graph
 */
public class JsonStringGenerator extends AbstractGraphStringGenerator {

    public JsonStringGenerator(DependencyGraph graph, ListMultimap projects2Subprojects) {
        super(graph, projects2Subprojects);
    }

    /**
     * Generates the json for the given projects and dependencies
     *
     * @return json model
     */
    @Override
    public String generate() {

        // Stuff not linked to other stuff
        List<String> standaloneNames = transform(standaloneProjects, PROJECT_NAME_FUNCTION);

        List<Map<String, String>> edges = new ArrayList<Map<String, String>>();

        for (Edge edge : this.edges) {
            addEdge(edges, edge);
        }

        // a cluster is a group of jobs having at least one connection to each other through edges
        List<Set<ProjectNode>> clusters = newArrayList(new WeakComponentClusterer<ProjectNode, Edge>().transform(graph.getGraph()));
        Collections.sort(clusters, natural().onResultOf(new Function<Set<ProjectNode>, Integer>() {
            @Override
            public Integer apply(Set<ProjectNode> input) {
                return input.size();
            }
        }).reverse());



        List<Graph<ProjectNode, Edge>> subgraphs = newArrayList(FilterUtils.createAllInducedSubgraphs(clusters, graph.getGraph()));
        List<Map<String, Object>> clusterList = newArrayList();
        for (Graph<ProjectNode, Edge> subgraph : subgraphs) {
            Map<String, Object> cluster = Maps.newHashMap();
            Layout<ProjectNode, Edge> layout = new JungSugiyama<ProjectNode, Edge>(subgraph);
            layout.setSize(new Dimension(300,800));
            layout.initialize();
            if (layout instanceof IterativeContext) {
                IterativeContext context = (IterativeContext) layout;
                for (int i = 0; i < 700; i ++) {
                    context.step();
                    if (context.done()) {
                        break;
                    }
                }
            }

            List<Map<String,Object>> nodeList = newArrayList();
            double minX = 300;
            double maxX = 0;
            double minY = 800;
            double maxY = 0;
            for (ProjectNode node : subgraph.getVertices()) {
                Point2D point = layout.transform(node);
                if (point.getX() > maxX) {
                    maxX = point.getX();
                }
                if (point.getX() < minX) {
                    minX = point.getX();
                }
                if (point.getY() > maxY) {
                    maxY = point.getY();
                }
                if (point.getY() < minY) {
                    minY = point.getY();
                }
            }

            for (ProjectNode node : subgraph.getVertices()) {
                Point2D point = layout.transform(node);
                nodeList.add(ImmutableMap.<String, Object>builder()
                        .put("name", node.getName())
                        .put("fullName", node.getProject().getFullName())
                        .put("url", node.getProject().getAbsoluteUrl())
                        .put("x", point.getX() - minX)
                        .put("y", point.getY() - minY)
                        .build());
            }
            cluster.put("nodes", nodeList);
            cluster.put("hSize", maxX - minX);
            cluster.put("vSize", maxY - minY);
            clusterList.add(cluster);
        }


        JSONObject json = new JSONObject();
        json.put("edges", edges);
        json.put("clusters", clusterList);

        final String jsonStr = json.toString(2);
        return jsonStr;
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
    }

}
