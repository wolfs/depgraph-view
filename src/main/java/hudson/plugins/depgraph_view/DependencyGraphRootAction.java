package hudson.plugins.depgraph_view;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.model.RootAction;
import hudson.model.TopLevelItem;
import hudson.model.View;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.kohsuke.stapler.StaplerRequest;

@Extension
public class DependencyGraphRootAction extends AbstractDependencyGraphAction
	implements RootAction {

	@Override
	protected Collection<? extends AbstractProject<?, ?>> getProjectsForDepgraph(StaplerRequest req) {
		View view = req.findAncestorObject(View.class);
		if (view == null) {
			view = Hudson.getInstance().getPrimaryView();
		}
		Collection<TopLevelItem> items = view.getItems();
		List<AbstractProject<?,?>> projects = new ArrayList<AbstractProject<?,?>>();
		for (TopLevelItem item : items) {
			if (item instanceof AbstractProject<?, ?>) {
				projects.add((AbstractProject<?, ?>) item);
			}
		}
		return projects;
	}

	public View getMainView() {
		return Hudson.getInstance().getPrimaryView();
	}

	public View getAncestorView(StaplerRequest req) {
		return req.findAncestorObject(View.class);
	}

}
