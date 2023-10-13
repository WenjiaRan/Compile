package node;

import frontend.Parser;
import token.Token;
import utils.IOUtils;

import java.util.List;

public class ConstInitValNode {
    public ConstExpNode constExpNode = null;
    public List<ConstInitValNode> constInitValNodes = null;
    public List<Token> commaTokens = null;
    public Token leftBraceToken = null;
    public Token rightBraceToken = null;

    public ConstInitValNode(ConstExpNode constExpNode) {
        this.constExpNode = constExpNode;
    }

    public ConstInitValNode(Token leftBraceToken, List<ConstInitValNode> constInitValNodes, List<Token> commaTokens, Token rightBraceToken) {
        this.leftBraceToken = leftBraceToken;
        this.constInitValNodes = constInitValNodes;
        this.commaTokens = commaTokens;
        this.rightBraceToken = rightBraceToken;
    }

    void print() {
        if (constExpNode != null) {
            constExpNode.print();
        } else {
            printListValues();
        }
        IOUtils.write(Parser.nodeType.get(NodeType.ConstInitVal));
    }

    public void printListValues() {
        IOUtils.write(leftBraceToken.toString());
        if (constInitValNodes.size() > 0) {
            constInitValNodes.get(0).print();

            int index = 0;
            while (index < commaTokens.size()) {
                IOUtils.write(commaTokens.get(index).toString());
                constInitValNodes.get(index + 1).print();
                index++;
            }
        }
        IOUtils.write(rightBraceToken.toString());
    }

}
