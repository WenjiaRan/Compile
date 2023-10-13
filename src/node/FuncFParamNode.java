package node;

import frontend.Parser;
import token.Token;
import utils.IOUtils;

import java.util.List;

public class FuncFParamNode {
    // FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
    private BTypeNode bTypeNode;
    private Token ident;
    private List<Token>leftSqareBrack;
    private List<Token>rightSqareBrack;
    private List<ConstExpNode> constExpNodes;

    public FuncFParamNode(BTypeNode bTypeNode, Token ident, List<Token> leftSqareBrack, List<Token> rightSqareBrack, List<ConstExpNode> constExpNodes) {
        this.bTypeNode = bTypeNode;
        this.ident = ident;
        this.leftSqareBrack = leftSqareBrack;
        this.rightSqareBrack = rightSqareBrack;
        this.constExpNodes = constExpNodes;
    }

    public void print(){
        bTypeNode.print();
        IOUtils.write(ident.toString());
        if(leftSqareBrack.size()>0){
            IOUtils.write(leftSqareBrack.get(0).toString());
            IOUtils.write(rightSqareBrack.get(0).toString());
            for(int i=0;i< constExpNodes.size();i++){
                IOUtils.write(leftSqareBrack.get(i + 1).toString());
                constExpNodes.print();
                IOUtils.write(rightSqareBrack.get(i+1).toString());
            }
        }
        IOUtils.write(Parser.nodeType.get(NodeType.FuncFParam));
    }






















}
