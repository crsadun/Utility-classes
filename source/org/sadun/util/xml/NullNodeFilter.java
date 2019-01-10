package org.sadun.util.xml;

import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeFilter;

/**
 * A DOM NodeFilter that accepts every node. 
 *
 * @author Cristiano Sadun
 */
public class NullNodeFilter implements NodeFilter {

    public short acceptNode(Node n) {
        return NodeFilter.FILTER_ACCEPT;
    }

}
