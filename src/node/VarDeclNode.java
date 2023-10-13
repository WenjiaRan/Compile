package node;

import frontend.Parser;
import token.Token;
import utils.IOUtils;

import java.util.List;

public class VarDeclNode {
//    VarDecl → BType VarDef { ',' VarDef } ';'
    private BTypeNode bTypeNode;
//    private VarDefNode varDefNode;
    private List<Token> commaTokens;
    private List<VarDefNode> varDefNodes;
    private Token semicolon;

    public VarDeclNode(BTypeNode bTypeNode, List<VarDefNode> varDefNodes,  List<Token> commaTokens, Token semicolon) {
        this.bTypeNode = bTypeNode;
//        this.varDefNode = varDefNode;
        this.commaTokens = commaTokens;
        this.varDefNodes = varDefNodes;
        this.semicolon = semicolon;
    }

    void print(){
        bTypeNode.print();
        varDefNodes.get(0).print();
        for(int i=0;i<commaTokens.size();i++){
            IOUtils.write(commaTokens.get(i).toString());
            varDefNodes.get(i+1).print();
        }
        IOUtils.write(semicolon.toString());
        IOUtils.write(Parser.nodeType.get(NodeType.VarDecl));
    }











}
