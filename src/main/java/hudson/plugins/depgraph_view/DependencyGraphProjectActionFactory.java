package hudson.plugins.depgraph_view;

import hudson.Extension;
import hudson.model.AbstractModelObject;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.TransientProjectActionFactory;
import org.kohsuke.stapler.StaplerRequest;

import java.util.Collection;
import java.util.Collections;

/**
 * User: wolfs
 * Date: 10.12.10
 * Time: 16:28
 */
@Extension
public class DependencyGraphProjectActionFactory extends TransientProjectActionFactory {

    public static final class DependencyGraphProjectAction extends AbstractDependencyGraphAction {
        final private AbstractProject<?, ?> project;

        public DependencyGraphProjectAction(AbstractProject<?, ?> project) {
            this.project = project;
        }

        @Override
        protected Collection<AbstractProject<?, ?>> getProjectsForDepgraph(StaplerRequest req) {
            return Collections.<AbstractProject<?, ?>>singleton(project);
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
    public Collection<? extends Action> createFor(AbstractProject target) {
        return Collections.singleton(new DependencyGraphProjectAction(target));
    }

}
