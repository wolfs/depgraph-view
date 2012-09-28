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

import hudson.Launcher;
import hudson.model.Action;
import hudson.model.AbstractModelObject;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.plugins.depgraph_view.DependencyGraphProperty.DescriptorImpl;
import hudson.plugins.depgraph_view.operations.DeleteEdgeOperation;
import hudson.plugins.depgraph_view.operations.PutEdgeOperation;
import hudson.util.LogTaskListener;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.google.common.collect.ImmutableMap;

/**
 * Basic action for creating a Dot-Image of the DependencyGraph
 *
 * @author wolfs
 */
public abstract class AbstractDependencyGraphAction implements Action {
    private final Logger LOGGER = Logger.getLogger(Logger.class.getName());
    
    private static final Pattern EDGE_PATTERN = Pattern.compile("/(.*)/(.*[^/])(.*)");

    /**
     * Maps the extension of the requested file to the content type and the
     * argument for the -T option of the graphviz tools
     */
    protected static final ImmutableMap<String, SupportedImageType> extension2Type =
            ImmutableMap.of(
                    "png",SupportedImageType.of("image/png", "png"),
                    "svg",SupportedImageType.of("image/svg", "svg"),
                    "map",SupportedImageType.of("image/cmapx", "cmapx"),
                    "gv",SupportedImageType.of("text/plain", "gv"), // Special case - do no processing
                    "json",SupportedImageType.of("text/plain", "json")
            );

    // Data Structure to encode the content type and the -T argument for the graphviz tools
    protected static class SupportedImageType {
        final String contentType;
        final String dotType;

        private SupportedImageType(String contentType,
                                   String dotType) {
            this.contentType = contentType;
            this.dotType = dotType;
        }

        public static SupportedImageType of(String contentType, String dotType) {
            return new SupportedImageType(contentType, dotType);
        }

    }
    
    public void doEdge(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, InterruptedException {
        String path = req.getRestOfPath();
        Matcher m = EDGE_PATTERN.matcher(path);
        if (m.find( )) {
          try {
            final String sourceJobName = m.group(1);
              final String targetJobName = m.group(2);
              if ("PUT".equalsIgnoreCase(req.getMethod())) {
                 new PutEdgeOperation(sourceJobName, targetJobName).perform();
              } else if ("DELETE".equalsIgnoreCase(req.getMethod())) {
                 new DeleteEdgeOperation(sourceJobName, targetJobName).perform();
              }
            } catch (Exception e) {
                rsp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);    
            }
        } else {
            rsp.sendError(HttpServletResponse.SC_NOT_FOUND);            
        }
        return;
    }
    
    /**
     * graph.{png,gv,...} is mapped to the corresponding output
     */
    public void doDynamic(StaplerRequest req, StaplerResponse rsp)  throws IOException, ServletException, InterruptedException {
        String path = req.getRestOfPath();
        if (path.startsWith("/graph.")) {
            String extension = path.substring("/graph.".length());
            if (extension2Type.containsKey(extension.toLowerCase())) {
                SupportedImageType imageType = extension2Type.get(extension.toLowerCase());
                CalculateDeps calculateDeps = new CalculateDeps(getProjectsForDepgraph());
                if ("json".equalsIgnoreCase(extension)) {
                    JsonStringGenerator jsonStringGenerator = new JsonStringGenerator(
                            calculateDeps.getProjects(),
                            calculateDeps.getDependencies(),
                            calculateDeps.getSubJobs(),
                            calculateDeps.getCopiedArtifacts());
                    String graphJson = jsonStringGenerator.generate();
                    rsp.getWriter().append(graphJson).close();
                }else{
                    DotStringGenerator dotStringGenerator = new DotStringGenerator(
                            calculateDeps.getProjects(),
                            calculateDeps.getDependencies(),
                            calculateDeps.getSubJobs(),
                            calculateDeps.getCopiedArtifacts());
                    String graphDot = dotStringGenerator.generate();
    
                    rsp.setContentType(imageType.contentType);
                    if ("gv".equalsIgnoreCase(extension)) {
                        rsp.getWriter().append(graphDot).close();
                    } else {
                        runDot(rsp.getOutputStream(), new ByteArrayInputStream(graphDot.getBytes()), imageType.dotType);
                    }
                }
            }
        } else {
            rsp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
            return;
        }
    }

    
    /**
     * Execute the dot commando with given input and output stream
     * @param type the parameter for the -T option of the graphviz tools
     */
    protected void runDot(OutputStream output, InputStream input, String type)
            throws IOException {
        DescriptorImpl descriptor = Hudson.getInstance().getDescriptorByType(DependencyGraphProperty.DescriptorImpl.class);
        String dotPath = descriptor.getDotExeOrDefault();
        Launcher launcher = Hudson.getInstance().createLauncher(new LogTaskListener(LOGGER, Level.CONFIG));
        try {
            launcher.launch()
                    .cmds(dotPath,"-T" + type)
                    .stdin(input)
                    .stdout(output).start().join();
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Interrupted while waiting for dot-file to be created",e);
        }
        finally {
            if (output != null) {
                output.close();
            }
        }
    }

    /**
     * @return projects for which the dependency graph should be calculated
     */
    protected abstract Collection<? extends AbstractProject<?, ?>> getProjectsForDepgraph();

    /**
     * @return title of the dependency graph page
     */
    public abstract String getTitle();

    /**
     * @return object for which the sidepanel.jelly will be shown
     */
    public abstract AbstractModelObject getParentObject();

    @Override
    public String getIconFileName() {
        return "graph.gif";
    }

    @Override
    public String getDisplayName() {
        return Messages.AbstractDependencyGraphAction_DependencyGraph();
    }

    @Override
    public String getUrlName() {
        return "depgraph-view";
    }

}
