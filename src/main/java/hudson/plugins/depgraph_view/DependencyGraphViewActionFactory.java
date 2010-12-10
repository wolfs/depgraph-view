package hudson.plugins.depgraph_view;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.TransientViewActionFactory;
import hudson.model.View;

import java.util.Collections;
import java.util.List;

/**
 * User: wolfs
 * Date: 10.12.10
 * Time: 15:41
 */
@Extension
public class DependencyGraphViewActionFactory extends TransientViewActionFactory {
    @Override
    public List<Action> createFor(View v) {
        return Collections.<Action>singletonList(new DependencyGraphRootAction(v));
    }
}
