package node;

import frontend.Parser;
import token.Token;
import utils.IOUtils;

import java.util.List;

public class ConstDeclNode {
    public BTypeNode bTypeNode = null;
    public List<ConstDefNode> constDefNodes = null;
    public Token constToken = null;
    public List<Token> commasToken = null;
    public Token semicnToken = null;

    public ConstDeclNode(Token constToken, BTypeNode bTypeNode, List<ConstDefNode> constDefNodes, List<Token> commasToken, Token semicnToken) {
        this.bTypeNode = bTypeNode;
        this.constDefNodes = constDefNodes;
        this.constToken = constToken;
        this.commasToken = commasToken;
        this.semicnToken = semicnToken;
    }

    void print() {
        IOUtils.write(constToken.toString());
        bTypeNode.print();
        constDefNodes.get(0).print();

        int index = 0;
        while (index < commasToken.size()) {
            IOUtils.write(commasToken.get(index).toString());
            constDefNodes.get(index + 1).print();
            index++;
        }

        IOUtils.write(semicnToken.toString());
        IOUtils.write(Parser.nodeType.get(NodeType.ConstDecl));
    }
}
