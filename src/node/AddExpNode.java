package node;

import frontend.Parser;
import token.Token;
import utils.IOUtils;

public class AddExpNode {
    public AddExpNode addExpNode = null;
    public MulExpNode mulExpNode = null;
    public Token operation = null;

    public AddExpNode(MulExpNode mulExpNode, Token operation, AddExpNode addExpNode) {
        this.addExpNode = addExpNode;
        this.mulExpNode = mulExpNode;
        this.operation = operation;
    }

    public void print() {
        mulExpNode.print();
        IOUtils.write(Parser.nodeType.get(NodeType.AddExp));
        if (operation != null) {
            IOUtils.write(operation.toString());
            addExpNode.print();
        }
    }
}
