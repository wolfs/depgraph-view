package hudson.plugins.depgraph_view;

import hudson.Extension;
import hudson.model.RootAction;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.model.View;

import java.util.Collection;
import java.util.List;

@Extension
public class DependencyGraphRootAction extends AbstractDependencyGraphAction
	implements RootAction {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected Collection<? extends AbstractProject<?, ?>> getProjectsForDepgraph() {
		return (List) Hudson.getInstance().getAllItems(AbstractProject.class);
	}

	public View getMainView() {
		return Hudson.getInstance().getPrimaryView();
	}

}
