package node;

import frontend.Parser;
import token.Token;
import utils.IOUtils;

public class FuncDefNode {
    // FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
    public FuncTypeNode funcTypeNode;
    public Token ident;
    public Token leftBracket;
    public FuncFParamsNode funcFParamsNode=null;
    public Token rightBracket;
    public BlockNode blockNode;

    public FuncDefNode(FuncTypeNode funcTypeNode, Token ident, Token leftBracket, FuncFParamsNode funcFParamsNode, Token rightBracket, BlockNode blockNode) {
        this.funcTypeNode = funcTypeNode;
        this.ident = ident;
        this.leftBracket = leftBracket;
        this.funcFParamsNode = funcFParamsNode;
        this.rightBracket = rightBracket;
        this.blockNode = blockNode;
    }

    public void print(){
        funcTypeNode.print();
        IOUtils.write(ident.toString());
        IOUtils.write(leftBracket.toString());
        if (funcFParamsNode!=null){
            funcFParamsNode.print();
        }
        IOUtils.write(rightBracket.toString());
        blockNode.print();
        IOUtils.write(Parser.nodeType.get(NodeType.FuncDef));
    }










}
