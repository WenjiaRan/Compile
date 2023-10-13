package node;

import frontend.Parser;
import token.Token;
import utils.IOUtils;

import java.util.List;

public class InitValNode {
    // InitVal → Exp | '{' [ InitVal { ',' InitVal } ]
    private ExpNode expNode;

    private Token leftBrace;
    private List<InitValNode> initValNodes;
    private List<Token> commaTokens;
    private Token rightBrace;

    public InitValNode(ExpNode expNode) {
        this.expNode = expNode;
    }

    public InitValNode(Token leftBrace, List<InitValNode> initValNodes, List<Token> commaTokens, Token rightBrace) {
        this.leftBrace = leftBrace;
        this.initValNodes = initValNodes;
        this.commaTokens = commaTokens;
        this.rightBrace = rightBrace;
    }

    void print(){
        if(expNode!=null){
            expNode.print();
        }
        else {
            IOUtils.write(leftBrace.toString());
            if(initValNodes.size()>0){
                initValNodes.get(0).print();
                for(int i=0;i<commaTokens.size();i++){
                    IOUtils.write(commaTokens.get(i).toString());
                    initValNodes.get(i+1).print();
                }
            }
            IOUtils.write(rightBrace.toString());
        }
        IOUtils.write(Parser.nodeType.get(NodeType.InitVal));
    }
}
