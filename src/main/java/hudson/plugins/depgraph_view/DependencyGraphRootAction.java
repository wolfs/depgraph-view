package hudson.plugins.depgraph_view;

import hudson.Extension;
import hudson.model.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.kohsuke.stapler.StaplerRequest;


public class DependencyGraphRootAction extends AbstractDependencyGraphAction
	implements Action {

    private View view;

    public DependencyGraphRootAction(View view) {
        this.view = view;
    }

	@Override
	protected Collection<? extends AbstractProject<?, ?>> getProjectsForDepgraph(StaplerRequest req) {
		Collection<TopLevelItem> items = view.getItems();
		List<AbstractProject<?,?>> projects = new ArrayList<AbstractProject<?,?>>();
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

    public View getView() {
        return view;
    }

}
