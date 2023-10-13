package node;

import frontend.Parser;
import token.Token;
import utils.IOUtils;

import java.util.List;

public class ConstDeclNode {
    // 常量声明 ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';' // 1.花括号内重复0
    //次 2.花括号内重复多次

    private Token constToken;
    private BTypeNode bTypeNode;
    private List<ConstDefNode> constDefNodes;
    private List<Token> commasToken;
    private Token semicnToken;

    public ConstDeclNode(Token constToken, BTypeNode bTypeNode, List<ConstDefNode> constDefNodes, List<Token> commasToken, Token semicnToken) {
        this.constToken = constToken;
        this.bTypeNode = bTypeNode;
        this.constDefNodes = constDefNodes;
        this.commasToken = commasToken;
        this.semicnToken = semicnToken;
    }

    void print(){
        IOUtils.write(constToken.toString());
        bTypeNode.print();
        constDefNodes.get(0).print();
        for(int i=0;i<commasToken.size();i++){
            IOUtils.write(commasToken.get(i).toString());
            constDefNodes.get(i+1).print();
        }
        IOUtils.write(semicnToken.toString());
        IOUtils.write(Parser.nodeType.get(NodeType.ConstDecl));
    }
}
