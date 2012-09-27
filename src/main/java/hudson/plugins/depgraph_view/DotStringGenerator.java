/*
 * The MIT License
 *
 * Copyright (c) 2011, Stefan Wolf
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
import com.google.common.base.Joiner;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import hudson.model.AbstractProject;
import hudson.model.DependencyGraph;
import hudson.model.Hudson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Functions.compose;
import static com.google.common.collect.Lists.transform;

/**
 * @author wolfs
 */
public class DotStringGenerator {
    //  Lexicographic order of the dependencies
    private static final Comparator<DependencyGraph.Dependency> DEP_COMPARATOR = new Comparator<DependencyGraph.Dependency>() {
        @Override
        public int compare(DependencyGraph.Dependency o1, DependencyGraph.Dependency o2) {
            int down = (PROJECT_COMPARATOR.compare(o1.getDownstreamProject(),o2.getDownstreamProject()));
            return down != 0 ? down : PROJECT_COMPARATOR
                    .compare(o1.getUpstreamProject(),o2.getUpstreamProject());
        }
    };

    // Compares projects by name
    private static final Comparator<AbstractProject<?,?>> PROJECT_COMPARATOR = new Comparator<AbstractProject<?,?>>() {
        @Override
        public int compare(AbstractProject<?,?> o1, AbstractProject<?,?> o2) {
            return o1.getFullDisplayName().compareTo(o2.getFullDisplayName());
        }
    };

    private static final Function<AbstractProject<?,?>, String> PROJECT_NAME_FUNCTION = new Function<AbstractProject<?, ?>, String>() {
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

    private String subProjectColor = "#F0F0F0";
    private String copyArtifactColor = "lightblue";
    private List<AbstractProject<?, ?>> standaloneProjects;
    private List<AbstractProject<?, ?>> projectsInDeps;
    private List<DependencyGraph.Dependency> updownstreamDeps;
    private ListMultimap<AbstractProject<?,?>, AbstractProject<?,?>> subJobs;
    private List<DependencyGraph.Dependency> copiedArtifactDeps;

    public DotStringGenerator subProjectColor(String color) {
        subProjectColor = color;
        return this;
    }

    public DotStringGenerator copyArtifactColor(String color) {
        copyArtifactColor = color;
        return this;
    }

    public DotStringGenerator(Set<AbstractProject<?, ?>> projects,
                           Set<DependencyGraph.Dependency> deps,
                           ListMultimap<AbstractProject<?, ?>, AbstractProject<?, ?>> subJobs,
                           Set<DependencyGraph.Dependency> copied) {
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

    /**
     * Generates the graphviz code for the given projects and dependencies
     * @return graphviz code
     */
    public String generate() {
        /**** Build the dot source file ****/
        StringBuilder builder = new StringBuilder();

        builder.append("digraph {\n");
        builder.append("node [shape=box, style=rounded];\n");

        /**** First define all the objects and clusters ****/

        // up/downstream linked jobs
        builder.append(cluster("Main", projectsInDependenciesNodes(), "color=invis;"));

        // Stuff not linked to other stuff
        List<String> standaloneNames = transform(standaloneProjects, compose(ESCAPE, PROJECT_NAME_FUNCTION));
        builder.append(cluster("Standalone", standaloneProjectNodes(standaloneNames),"color=invis;"));


        /****Now define links between objects ****/

        // plain normal dependencies (up/downstream)
        for (DependencyGraph.Dependency dep : updownstreamDeps) {
            builder.append(dependencyToEdgeString(dep));
            builder.append(";\n");
        }

        //  copied artifact dependencies
        for (DependencyGraph.Dependency dep : copiedArtifactDeps) {
            builder.append(dependencyToCopiedArtifactString(dep));
            builder.append(";\n");
        }

        if (!standaloneNames.isEmpty()) {
            builder.append("edge[style=\"invisible\",dir=\"none\"];\n" + Joiner.on(" -> ").join(standaloneNames) + ";\n");
            builder.append("edge[style=\"invisible\",dir=\"none\"];\n" + standaloneNames.get(standaloneNames.size() - 1) + " -> \"Dependency Graph\"");
        }


        builder.append("}");

        return builder.toString();
    }

    public String generateLegend() {
        /**** Build the dot source file ****/
        StringBuilder builder = new StringBuilder();

        builder.append("digraph {\n");
        builder.append("node [shape=box, style=rounded];\n");

        builder.append(cluster("Legend", legend()));

        builder.append("}");
        return builder.toString();
    }

    private String standaloneProjectNodes(List<String> standaloneNames) {
        StringBuilder builder = new StringBuilder();
        for (AbstractProject<?, ?> proj : standaloneProjects) {
            builder.append(projectToNodeString(proj, subJobs.get(proj)));
            builder.append(";\n");
        }
        if (!standaloneNames.isEmpty()) {
            builder.append("edge[style=\"invisible\",dir=\"none\"];\n" + Joiner.on(" -> ").join(standaloneNames) + ";\n");
        }
        return builder.toString();
    }

    private String projectsInDependenciesNodes() {
        StringBuilder stringBuilder = new StringBuilder();
        for (AbstractProject<?, ?> proj : projectsInDeps) {
            if (subJobs.containsKey(proj)) {
                stringBuilder.append(projectToNodeString(proj, subJobs.get(proj)));
            }
            else {
                stringBuilder.append(projectToNodeString(proj));
            }
            stringBuilder.append(";\n");
        }
        return stringBuilder.toString();
    }

    private String legend() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("label=\"Legend:\" labelloc=t centered=false color=black node [shape=plaintext]")
                .append("\"Dependency Graph\"\n")
                .append("\"Copy Artifact\"\n")
                .append("\"Sub-Project\"\n")
                .append("node [style=invis]\n")
                .append("a [label=\"\"] b [label=\"\"]")
                .append(" c [fillcolor=" + escapeString(subProjectColor) + " style=filled fontcolor="
                        + escapeString(subProjectColor) + "]\n")
                .append("a -> b [style=invis]\n")
                .append("{rank=same a -> \"Dependency Graph\" [color=black style=bold minlen=2]}\n")
                .append("{rank=same b -> \"Copy Artifact\" [color=lightblue minlen=2]}\n")
                .append("{rank=same c -> \"Sub-Project\" [ style=invis]}\n");
        return stringBuilder.toString();
    }

    private String cluster(String name, String contents, String... options) {
        StringBuilder builder = new StringBuilder();
        builder.append("subgraph cluster" + name + " {\n");
        builder.append(contents);
        builder.append(Joiner.on("\n").join(options) + "}\n");
        return builder.toString();
    }

    private Set<AbstractProject<?, ?>> listUniqueProjectsInDependencies(List<DependencyGraph.Dependency> dependencies)
    {
        Set<AbstractProject<?, ?>> set = new HashSet<AbstractProject<?, ?>>();
        for (DependencyGraph.Dependency dependency : dependencies)
        {
            set.add(dependency.getUpstreamProject());
            set.add(dependency.getDownstreamProject());
        }
        return set;
    }


    private String projectToNodeString(AbstractProject<?, ?> proj) {
        return escapeString(proj.getFullDisplayName()) +
                " [href=" +
                getEscapedProjectUrl(proj) + "]";
    }

    private String projectToNodeString(AbstractProject<?, ?> proj, List<AbstractProject<?,?>> subprojects) {
        StringBuilder builder = new StringBuilder();
        builder.append(escapeString(proj.getFullDisplayName()))
                .append(" [shape=\"Mrecord\" href=")
                .append(getEscapedProjectUrl(proj))
                .append(" label=<<table border=\"0\" cellborder=\"0\" cellpadding=\"3\" bgcolor=\"white\">\n");
        builder.append(getProjectRow(proj));
        for (AbstractProject<?, ?> subproject : subprojects) {
            builder.append(getProjectRow(subproject, "bgcolor=" + escapeString(subProjectColor))).append("\n");
        }
        builder.append("</table>>]");
        return builder.toString();
    }

    private String getProjectRow(AbstractProject<?,?> project, String... extraColumnProperties) {
        return String.format("<tr><td align=\"center\" href=%s %s>%s</td></tr>", getEscapedProjectUrl(project), Joiner.on(" ").join(extraColumnProperties),
                project.getFullDisplayName());
    }

    private String getEscapedProjectUrl(AbstractProject<?, ?> proj) {
        return escapeString(Hudson.getInstance().getRootUrlFromRequest() + proj.getUrl());
    }

    private String dependencyToCopiedArtifactString(DependencyGraph.Dependency dep) {
        return dependencyToEdgeString(dep,"color=" + copyArtifactColor);
    }

    private String dependencyToEdgeString(DependencyGraph.Dependency dep, String... options) {
        return escapeString(dep.getUpstreamProject().getFullDisplayName()) + " -> " +
                escapeString(dep.getDownstreamProject().getFullDisplayName()) + " [ " + Joiner.on(" ").join(options) +" ] ";
    }

    private static String escapeString(String toEscape) {
        return "\"" + toEscape + "\"";
    }
}
