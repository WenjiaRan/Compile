package node;

import frontend.Parser;
import token.Token;
import utils.IOUtils;

public class RelExpNode {
    // RelExp -> AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
    // RELExp -> AddExp RELExp'
    // RelExp' -> ('<' | '>' | '<=' | '>=') AddExp RELExp'|E
    // RELExp -> AddExp[('<' | '>' | '<=' | '>=')RELExp]
    public AddExpNode addExpNode;
    public Token operator;
    public RelExpNode relExpNode;


    public RelExpNode(AddExpNode addExpNode, Token operator, RelExpNode relExpNode) {
        this.addExpNode = addExpNode;
        this.operator = operator;
        this.relExpNode = relExpNode;
    }

    public void print() {
        addExpNode.print();
        IOUtils.write(Parser.nodeType.get(NodeType.RelExp));
        if (operator != null) {
            IOUtils.write(operator.toString());
            relExpNode.print();
        }
    }
}