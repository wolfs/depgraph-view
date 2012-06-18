package hudson.plugins.depgraph_view;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class DepNode {

    public static class Edge {
        private final DepNode endpoint;

        public Edge(DepNode endpoint) {
            this.endpoint = endpoint;
        }

        public DepNode getEndpoint() {
            return endpoint;
        }

        @Override
        public String toString() {
            return "[Edge:" + endpoint.getName() + "]";
        }

        public static void addEdge(DepNode from, DepNode to) {
            from.addChild(new Edge(to));
            to.addParent(new Edge(from));
        }
    }

    private final String name;
    private Set<Edge> parents = new HashSet<Edge>();
    private Set<Edge> childs = new HashSet<Edge>();

    public DepNode(String name) {
        this.name = name;
    }

    public DepNode(String name, Edge parent) {
        this.name = name;
        this.parents.add(parent);
    }

    public DepNode addChild(Edge child) {
        this.childs.add(child);
        return this;
    }

    public DepNode addParent(Edge parent) {
        this.parents.add(parent);
        return this;
    }

    public String getName() {
        return name;
    }

    public Set<Edge> getChilds() {
        return childs;
    }

    public Set<Edge> getParents() {
        return parents;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DepNode other = (DepNode) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    public int getLevel() {
        if (parents.size() == 0) {
            return 1;
        } else {
            int parentLevel = 0;
            for (Edge parent : parents) {
                int thisParentLevel = parent.getEndpoint().getLevel();
                parentLevel = Math.max(parentLevel, thisParentLevel);
            }
            return parentLevel + 1;
        }
    }

    public int getLevelPreventEndlessLoop(Stack<DepNode> processedNodes) throws CyclicJobsException {
        if (parents.size() == 0) {
            return 1;
        } else {
            if (processedNodes == null) {
                processedNodes = new Stack<DepNode>();
            }
            if (processedNodes.contains(this)) {
                throw new CyclicJobsException("Cyclic jobs for Node " + name);
            }
            processedNodes.add(this);
            int parentLevel = 0;
            for (Edge parent : parents) {
                int thisParentLevel = parent.getEndpoint().getLevelPreventEndlessLoop(processedNodes);
                parentLevel = Math.max(parentLevel, thisParentLevel);
            }
            processedNodes.remove(this);
            return parentLevel + 1;
        }
    }

    public static class CyclicJobsException extends Exception {
        private static final long serialVersionUID = 1L;

        private CyclicJobsException(String message) {
            super(message);
        }
    }

    @Override
    public String toString() {
        return "[DepNode:" + name + "]";
    }

}
