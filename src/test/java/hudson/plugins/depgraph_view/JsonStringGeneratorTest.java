package hudson.plugins.depgraph_view;

import hudson.plugins.depgraph_view.DepNode.Edge;
import junit.framework.TestCase;

import org.junit.Assert;

public class JsonStringGeneratorTest extends TestCase {

    public void testGetLevel() throws Exception {

        DepNode j1 = new DepNode("j1");
        DepNode j2 = new DepNode("j2");
        DepNode j3 = new DepNode("j3");
        DepNode j4 = new DepNode("j4");
        DepNode j5 = new DepNode("j5");
        DepNode j6 = new DepNode("j6");
        Edge.addEdge(j1, j2);
        Edge.addEdge(j2, j3);
        Edge.addEdge(j3, j4);
        Edge.addEdge(j3, j5);
        Edge.addEdge(j5, j6);

        assertEquals(1, j1.getLevel());
        assertEquals(2, j2.getLevel());
        assertEquals(3, j3.getLevel());
        assertEquals(4, j4.getLevel());
        assertEquals(4, j5.getLevel());
        assertEquals(5, j6.getLevel());

    }

    public void testNonRecursive() {
        DepNode a = new DepNode("A");
        DepNode b = new DepNode("B");
        DepNode c = new DepNode("C");
        DepNode d = new DepNode("D");
        DepNode e = new DepNode("E");
        DepNode g = new DepNode("G");

        Edge.addEdge(a, b);
        Edge.addEdge(b, c);
        Edge.addEdge(b, e);
        Edge.addEdge(d, c);
        Edge.addEdge(g, a);
        Edge.addEdge(g, d);

        Assert.assertEquals("Level of node A", 2, a.getLevel());
        Assert.assertEquals("Level of node B", 3, b.getLevel());
        Assert.assertEquals("Level of node C", 4, c.getLevel());
        Assert.assertEquals("Level of node D", 2, d.getLevel());
        Assert.assertEquals("Level of node E", 4, e.getLevel());
        Assert.assertEquals("Level of node G", 1, g.getLevel());
    }

    public void testRecursive() throws DepNode.CyclicJobsException {
        DepNode a = new DepNode("A");
        DepNode b = new DepNode("B");
        DepNode c = new DepNode("C");
        DepNode d = new DepNode("D");
        DepNode e = new DepNode("E");
        DepNode g = new DepNode("G");

        Edge.addEdge(a, b);
        Edge.addEdge(b, c);
        Edge.addEdge(b, e);
        Edge.addEdge(d, c);
        Edge.addEdge(g, a);
        Edge.addEdge(g, d);

        // recursion:
        Edge.addEdge(c, g);

        // call to get exception
        try {
            a.getLevelPreventEndlessLoop(null);
            fail("should not get here, because of exception");
        } catch (DepNode.CyclicJobsException e1) {
            // expected
        }
    }
}
