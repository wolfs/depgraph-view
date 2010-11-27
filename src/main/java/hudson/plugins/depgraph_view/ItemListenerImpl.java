package hudson.plugins.depgraph_view;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.model.listeners.ItemListener;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
public class ItemListenerImpl extends ItemListener {

	private static final Logger LOGGER = Logger.getLogger(ItemListener.class.getName());

	@Override
	public void onLoaded() {
		for (AbstractProject<?,?> project : Hudson.getInstance().getAllItems(AbstractProject.class)) {
			DependencyGraphProperty property = project.getProperty(DependencyGraphProperty.class);
			if (property == null) {
				try {
					project.addProperty(new DependencyGraphProperty());
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE, "Failed to persist " + project, e);
				}
			}
		}
	}

}
