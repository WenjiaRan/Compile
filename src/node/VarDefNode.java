package node;

import frontend.Parser;
import token.Token;
import utils.IOUtils;

import java.util.List;

public class VarDefNode {
    // VarDef → Ident { '[' ConstExp ']' }| Ident { '[' ConstExp ']' } '=' InitVal
    private Token identToken;
    private List<Token> leftSquareBrackTokens;
    private List<ConstExpNode> constExpNodes;
    private List<Token> rightSquareBrackTokens;

    private Token assignToken=null;
    private InitValNode initValNode;

    public VarDefNode(Token identToken, List<Token> leftSquareBrackTokens, List<ConstExpNode> constExpNodes, List<Token> rightSquareBrackTokens, Token assignToken, InitValNode initValNode) {
        this.identToken = identToken;
        this.leftSquareBrackTokens = leftSquareBrackTokens;
        this.constExpNodes = constExpNodes;
        this.rightSquareBrackTokens = rightSquareBrackTokens;
        this.assignToken = assignToken;
        this.initValNode = initValNode;
    }

    public void print(){
        IOUtils.write(identToken.toString());
        for(int i=0;i<leftSquareBrackTokens.size();i++){
            IOUtils.write(leftSquareBrackTokens.get(i).toString());
            constExpNodes.get(i).print();
            IOUtils.write(rightSquareBrackTokens.get(i).toString());
        }
        if (assignToken!=null){
            IOUtils.write(assignToken.toString());
            InitValNode.print();
        }
        IOUtils.write(Parser.nodeType.get(NodeType.VarDef));
    }
}
