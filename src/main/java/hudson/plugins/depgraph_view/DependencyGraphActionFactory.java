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

import java.util.Collection;
import java.util.Collections;

import hudson.Extension;
import hudson.model.AbstractModelObject;
import hudson.model.Action;
import hudson.model.Job;
import jenkins.model.TransientActionFactory;

/**
 * Factory to add a dependency graph view action to each project
 */
@Extension
public class DependencyGraphActionFactory extends TransientActionFactory<Job> {
    /**
     * Shows the connected component of the project
     */
    public static class DependencyGraphProjectAction extends AbstractDependencyGraphAction {
        final private Job<?, ?> project;

        public DependencyGraphProjectAction(Job<?, ?> project) {
            this.project = project;
        }

        @Override
        protected Collection<Job<?, ?>> getProjectsForDepgraph() {
            return Collections.<Job<?, ?>>singleton(project);
        }

        @Override
        public String getTitle() {
            return Messages.AbstractDependencyGraphAction_DependencyGraphOf(project.getDisplayName());
        }

        @Override
        public AbstractModelObject getParentObject() {
        	return project;
        }
    }
    
    @Override
    public Class<Job> type() {
    	return Job.class;
    }

    @Override
    public Collection<? extends Action> createFor(Job target) {
        return Collections.singleton(new DependencyGraphProjectAction(target));
    }

}
