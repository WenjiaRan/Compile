package node;

import frontend.Parser;
import utils.IOUtils;

public class DeclNode {
    // 声明 Decl → ConstDecl | VarDecl
    private ConstDeclNode constDeclNode=null;
    private VarDeclNode varDeclNode=null;

    public DeclNode(VarDeclNode varDeclNode) {
        this.varDeclNode = varDeclNode;
    }

    public DeclNode(ConstDeclNode constDeclNode) {
        this.constDeclNode = constDeclNode;
    }

    void print(){
        if(constDeclNode!=null){
            constDeclNode.print();
        }
        else varDeclNode.print();
//        IOUtils.write(Parser.nodeType.get(NodeType.Decl));
    }














}
