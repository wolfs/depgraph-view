package hudson.plugins.depgraph_view;

import hudson.Extension;
import hudson.Functions;
import hudson.Launcher;
import hudson.Util;
import hudson.model.*;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import hudson.util.FormValidation;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;


public class DependencyGraphProperty extends NodeProperty<Node> {

	@DataBoundConstructor
	public DependencyGraphProperty() {
	}

	@Extension
	public static class DescriptorImpl extends NodePropertyDescriptor {

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
