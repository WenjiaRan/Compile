package node;

import frontend.Parser;
import token.Token;
import utils.IOUtils;

import java.util.List;

public class VarDeclNode {
    public BTypeNode bTypeNode = null;
    public List<Token> commaTokens = null;
    public Token semicolon = null;
    public List<VarDefNode> varDefNodes = null;

    public VarDeclNode(BTypeNode bTypeNode, List<VarDefNode> varDefNodes, List<Token> commaTokens, Token semicolon) {
        this.bTypeNode = bTypeNode;
        this.varDefNodes = varDefNodes;
        this.commaTokens = commaTokens;
        this.semicolon = semicolon;
    }

    void print() {
        bTypeNode.print();
        varDefNodes.get(0).print();

        int index = 0;
        while (index < commaTokens.size()) {
            IOUtils.write(commaTokens.get(index).toString());
            varDefNodes.get(index + 1).print();
            index++;
        }

        IOUtils.write(semicolon.toString());
        IOUtils.write(Parser.nodeType.get(NodeType.VarDecl));
    }
}
