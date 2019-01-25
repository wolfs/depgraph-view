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

import hudson.Extension;
import hudson.model.AbstractModelObject;
import hudson.model.Action;
import hudson.model.TopLevelItem;
import hudson.model.TransientViewActionFactory;
import hudson.model.View;
import hudson.model.Job;

import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Factory to a dependency graph view action to all views
 */
@Extension
public class DependencyGraphViewActionFactory extends TransientViewActionFactory {
    /**
     * Shows the connected components containing the projects of the view
     */
    public static class DependencyGraphViewAction extends AbstractDependencyGraphAction
            implements Action {

        private View view;

        public DependencyGraphViewAction(View view) {
            this.view = view;
        }

        @Override
        protected Collection<? extends Job<?, ?>> getProjectsForDepgraph() {
            Collection<TopLevelItem> items = view.getItems();
            Collection<Job<?,?>> projects = new ArrayList<>();
            for (TopLevelItem item : items) {
                if (item instanceof Job<?, ?>) {
                    projects.add((Job<?, ?>) item);
                } else if (item instanceof WorkflowMultiBranchProject) {
                	projects.addAll(item.getAllJobs().stream().map(i -> (Job<?, ?>) i).collect(Collectors.toList()));
                }
            }
            return projects;
        }

        @Override
        public String getTitle() {
            return Messages.AbstractDependencyGraphAction_DependencyGraphOf(view.getDisplayName());
        }

        @Override
        public AbstractModelObject getParentObject() {
            return view;
        }
    }

    @Override
    public List<Action> createFor(View v) {
        return Collections.<Action>singletonList(new DependencyGraphViewAction(v));
    }

}
