package hudson.plugins.depgraph_view;

import java.util.Collection;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.TransientProjectActionFactory;

@Extension
public class DependencyGraphProjectActionFactory extends TransientProjectActionFactory {

    @Override
    public Collection<? extends Action> createFor(AbstractProject target) {
        return new DependencyGraphActionFactory().createFor(target);
    }
    
}
