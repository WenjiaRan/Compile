package node;

import frontend.Parser;
import token.Token;
import utils.IOUtils;

import java.util.List;

public class VarDefNode {
    public Token assignToken = null;
    public List<ConstExpNode> constExpNodes = null;
    public Token identToken = null;
    public InitValNode initValNode = null;
    public List<Token> leftSquareBrackTokens = null;
    public List<Token> rightSquareBrackTokens = null;

    public VarDefNode(Token identToken, List<Token> leftSquareBrackTokens, List<ConstExpNode> constExpNodes, List<Token> rightSquareBrackTokens, Token assignToken, InitValNode initValNode) {
        this.identToken = identToken;
        this.leftSquareBrackTokens = leftSquareBrackTokens;
        this.constExpNodes = constExpNodes;
        this.rightSquareBrackTokens = rightSquareBrackTokens;
        this.assignToken = assignToken;
        this.initValNode = initValNode;
    }

    public void print() {
        IOUtils.write(identToken.toString());

        int index = 0;
        while (index < leftSquareBrackTokens.size()) {
            IOUtils.write(leftSquareBrackTokens.get(index).toString());
            constExpNodes.get(index).print();
            IOUtils.write(rightSquareBrackTokens.get(index).toString());
            index++;
        }

        if (assignToken != null) {
            IOUtils.write(assignToken.toString());
            initValNode.print();
        }

        IOUtils.write(Parser.nodeType.get(NodeType.VarDef));
    }
}
