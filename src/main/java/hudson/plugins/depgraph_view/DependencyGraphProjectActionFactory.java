package hudson.plugins.depgraph_view;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.TransientProjectActionFactory;

import java.util.Collection;
import java.util.Collections;

/**
 * User: wolfs
 * Date: 10.12.10
 * Time: 16:28
 */
@Extension
public class DependencyGraphProjectActionFactory extends TransientProjectActionFactory {
    @Override
    public Collection<? extends Action> createFor(AbstractProject target) {
        return Collections.singleton(new DependencyGraphProjectAction(target));
    }
}
