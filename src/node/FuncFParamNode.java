package node;

import frontend.Parser;
import token.Token;
import utils.IOUtils;

import java.util.List;

public class FuncFParamNode {
    public BTypeNode bTypeNode;
    public Token ident;
    public List<ConstExpNode> constExpNodes;
    public List<Token> leftSqareBrack;
    public List<Token> rightSqareBrack;

    public FuncFParamNode(BTypeNode bTypeNode, Token ident, List<Token> leftSqareBrack, List<Token> rightSqareBrack, List<ConstExpNode> constExpNodes) {
        this.bTypeNode = bTypeNode;
        this.ident = ident;
        this.leftSqareBrack = leftSqareBrack;
        this.rightSqareBrack = rightSqareBrack;
        this.constExpNodes = constExpNodes;
    }

    public void print() {
        bTypeNode.print();
        IOUtils.write(ident.toString());

        if (!leftSqareBrack.isEmpty()) {
            IOUtils.write(leftSqareBrack.get(0).toString());
            IOUtils.write(rightSqareBrack.get(0).toString());

            int index = 0;
            while (index < constExpNodes.size()) {
                IOUtils.write(leftSqareBrack.get(index + 1).toString());
                constExpNodes.get(index).print();
                IOUtils.write(rightSqareBrack.get(index + 1).toString());
                index++;
            }
        }
        IOUtils.write(Parser.nodeType.get(NodeType.FuncFParam));
    }
}
