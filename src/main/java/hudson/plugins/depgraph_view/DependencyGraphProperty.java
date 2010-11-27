package hudson.plugins.depgraph_view;

import hudson.Extension;
import hudson.Functions;
import hudson.Launcher;
import hudson.Util;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.util.FormValidation;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;


public class DependencyGraphProperty extends JobProperty<AbstractProject<?,?>> {

	@DataBoundConstructor
	public DependencyGraphProperty() {
	}

	@Override
	public Collection<? extends Action> getJobActions(AbstractProject<?, ?> job) {
		return Collections.singleton(new DependencyGraphProjectAction(job));
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
			BuildListener listener) throws InterruptedException, IOException {
		return true;
	}



	@Extension
	public static class DescriptorImpl extends JobPropertyDescriptor {

		private String dotExe;

		public DescriptorImpl() {
			load();
		}

		@Override
        public boolean configure( StaplerRequest req, JSONObject o ) {
			dotExe = Util.fixEmptyAndTrim(o.getString("dotExe"));
            save();

            return true;
        }

		@Override
		public String getDisplayName() {
			return "Dependency Graph Viewer";
		}

		public String getDotExe() {
			return dotExe;
		}

		public String getDotExeOrDefault() {
			if (Util.fixEmptyAndTrim(dotExe) == null) {
				return Functions.isWindows() ? "dot.ext" : "dot";
			} else {
				return dotExe;
			}
		}

		public synchronized void setDotExe(String dotPath) {
			this.dotExe = dotPath;
			save();
		}

		public FormValidation doCheckDotExe(@QueryParameter final String value) {
			return FormValidation.validateExecutable(value);
		}

	}

}
