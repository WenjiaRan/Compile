package node;

import frontend.Parser;
import utils.IOUtils;

public class ConstExpNode {
    // ConstExp → AddExp
    public AddExpNode addExpNode;

    public ConstExpNode(AddExpNode addExpNode) {
        this.addExpNode = addExpNode;
    }

    public void print(){
        addExpNode.print();
        IOUtils.write(Parser.nodeType.get(NodeType.ConstExp));
    }
}
