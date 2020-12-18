package hudson.plugins.depgraph_view;

import java.util.Collection;
import java.util.Collections;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.FreeStyleProject;
import hudson.model.TransientProjectActionFactory;

@Extension
public class DependencyGraphProjectActionFactory extends TransientProjectActionFactory {

    @Override
    public Collection<? extends Action> createFor(AbstractProject target) {
        if (target instanceof FreeStyleProject) {
            // the menu link is contributed by the other factory
            return Collections.emptyList();
        } else {
            return new DependencyGraphActionFactory().createFor(target);
        }
    }

}
