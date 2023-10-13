package node;

import frontend.Parser;
import token.Token;
import utils.IOUtils;

import java.util.List;

public class LValNode {
    public Token ident;
    public List<ExpNode> expNodes;
    public List<Token> leftBrackets;
    public List<Token> rightBrackets;

    public LValNode(Token ident, List<Token> leftBrackets, List<ExpNode> expNodes, List<Token> rightBrackets) {
        this.ident = ident;
        this.expNodes = expNodes;
        this.leftBrackets = leftBrackets;
        this.rightBrackets = rightBrackets;
    }

    public void print() {
        IOUtils.write(ident.toString());

        int index = 0;
        while (index < leftBrackets.size()) {
            IOUtils.write(leftBrackets.get(index).toString());
            expNodes.get(index).print();
            IOUtils.write(rightBrackets.get(index).toString());
            index++;
        }

        IOUtils.write(Parser.nodeType.get(NodeType.LVal));
    }
}
