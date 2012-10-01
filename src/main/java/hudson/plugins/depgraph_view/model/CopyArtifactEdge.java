package hudson.plugins.depgraph_view.model;

/**
 * @author wolfs
 */
public class CopyArtifactEdge extends Edge {
    public CopyArtifactEdge(ProjectNode source, ProjectNode target) {
        super(source, target);
    }

    @Override
    public String getType() {
        return "copy";
    }
}
