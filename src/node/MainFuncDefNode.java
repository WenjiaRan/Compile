package node;

import frontend.Parser;
import token.Token;
import utils.IOUtils;

public class MainFuncDefNode {
    // MainFuncDef → 'int' 'main' '(' ')' Block
    private Token intToken;
    private Token mainToken;
    private Token leftBrackToken;
    private Token rightBrackToken;
    private BlockNode blockNode;

    public MainFuncDefNode(Token intToken, Token mainToken, Token leftBrackToken, Token rightBrackToken, BlockNode blockNode) {
        this.intToken = intToken;
        this.mainToken = mainToken;
        this.leftBrackToken = leftBrackToken;
        this.rightBrackToken = rightBrackToken;
        this.blockNode = blockNode;
    }

    void print(){
        IOUtils.write(intToken.toString());
        IOUtils.write(mainToken.toString());
        IOUtils.write(leftBrackToken.toString());
        IOUtils.write(rightBrackToken.toString());
        blockNode.print();
        IOUtils.write(Parser.nodeType.get(NodeType.MainFuncDef));
    }











}
