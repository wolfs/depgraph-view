package hudson.plugins.depgraph_view;

import hudson.model.AbstractProject;

import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;



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
	protected Collection<AbstractProject<?, ?>> getProjectsForDepgraph() {
		return Collections.<AbstractProject<?, ?>>singleton(project);
	}
}