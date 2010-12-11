package hudson.plugins.depgraph_view;

import hudson.Extension;
import hudson.model.*;
import org.kohsuke.stapler.StaplerRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * User: wolfs
 * Date: 10.12.10
 * Time: 15:41
 */
@Extension
public class DependencyGraphViewActionFactory extends TransientViewActionFactory {
    public static class DependencyGraphRootAction extends AbstractDependencyGraphAction
        implements Action {

        private View view;

        public DependencyGraphRootAction(View view) {
            this.view = view;
        }

        @Override
        protected Collection<? extends AbstractProject<?, ?>> getProjectsForDepgraph(StaplerRequest req) {
            Collection<TopLevelItem> items = view.getItems();
            Collection<AbstractProject<?,?>> projects = new ArrayList<AbstractProject<?,?>>();
            for (TopLevelItem item : items) {
                if (item instanceof AbstractProject<?, ?>) {
                    projects.add((AbstractProject<?, ?>) item);
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
        return Collections.<Action>singletonList(new DependencyGraphRootAction(v));
    }

}
