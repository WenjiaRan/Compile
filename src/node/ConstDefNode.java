package node;

import token.Token;
import utils.IOUtils;

import java.util.List;

public class ConstDefNode {
    public List<ConstExpNode> constExpNodes = null;
    public ConstInitValNode constInitValNode = null;
    public Token equalToken = null;
    public Token identToken = null;
    public List<Token> leftBracketsTokens = null;
    public List<Token> rightBracketsTokens = null;

    public ConstDefNode(Token identToken, List<Token> leftBracketsTokens, List<ConstExpNode> constExpNodes,
                        List<Token> rightBracketsTokens, Token equalToken, ConstInitValNode constInitValNode) {
        this.identToken = identToken;
        this.leftBracketsTokens = leftBracketsTokens;
        this.constExpNodes = constExpNodes;
        this.rightBracketsTokens = rightBracketsTokens;
        this.equalToken = equalToken;
        this.constInitValNode = constInitValNode;
    }

    void print() {
        IOUtils.write(identToken.toString());

        int index = 0;
        while (index < leftBracketsTokens.size()) {
            IOUtils.write(leftBracketsTokens.get(index).toString());
            constExpNodes.get(index).print();
            IOUtils.write(rightBracketsTokens.get(index).toString());
            index++;
        }

        IOUtils.write(equalToken.toString());
        constInitValNode.print();
    }
}
