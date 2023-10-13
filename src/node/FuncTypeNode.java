package node;

import frontend.Parser;
import token.Token;
import utils.IOUtils;

public class FuncTypeNode {
    private Token token;


    public FuncTypeNode(Token token) {
        this.token = token;
    }

    void print(){
        IOUtils.write(token.toString());
        IOUtils.write(Parser.nodeType.get(NodeType.FuncType));
    }
}
