package hudson.plugins.depgraph_view;

import hudson.model.AbstractProject;

import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;

import org.kohsuke.stapler.StaplerRequest;



public final class DependencyGraphProjectAction extends AbstractDependencyGraphAction {
	Logger LOGGER = Logger.getLogger(DependencyGraphProperty.class.getName());

	final private AbstractProject<?, ?> project;

	public DependencyGraphProjectAction(AbstractProject<?, ?> project) {
		this.project = project;
	}

	public AbstractProject<?, ?> getProject() {
		return project;
	}

	@Override
	protected Collection<AbstractProject<?, ?>> getProjectsForDepgraph(StaplerRequest req) {
		return Collections.<AbstractProject<?, ?>>singleton(project);
	}
}