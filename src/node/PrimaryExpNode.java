package node;

import frontend.Parser;
import token.Token;
import utils.IOUtils;

public class PrimaryExpNode {
    //PrimaryExp → '(' Exp ')' | LVal | Number
    private Token leftBracketToken;
    private ExpNode expNode;
    private Token rightBracketToken;

    private LValNode lValNode;

    private NumberNode numberNode;

    public PrimaryExpNode(NumberNode numberNode) {
        this.numberNode = numberNode;
    }

    public PrimaryExpNode(LValNode lValNode) {
        this.lValNode = lValNode;
    }

    public PrimaryExpNode(Token leftBracketToken, ExpNode expNode, Token rightBracketToken) {
        this.leftBracketToken = leftBracketToken;
        this.expNode = expNode;
        this.rightBracketToken = rightBracketToken;
    }

    public void print(){
        if (leftBracketToken!=null) {
            IOUtils.write(leftBracketToken.toString());
            expNode.print();
            IOUtils.write(rightBracketToken.toString());
        }
        else if(lValNode!=null) {
            lValNode.print();
        }
        else {
            numberNode.print();
        }
        IOUtils.write(Parser.nodeType.get(NodeType.PrimaryExp));
    }

















}
