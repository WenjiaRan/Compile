package node;

import frontend.Parser;
import token.Token;
import utils.IOUtils;

import java.util.List;

public class InitValNode {
    public List<Token> commaTokens;
    public List<InitValNode> initValNodes;
    public ExpNode expNode;
    public Token leftBrace;
    public Token rightBrace;

    public InitValNode(ExpNode expNode) {
        this.expNode = expNode;
    }

    public InitValNode(Token leftBrace, List<InitValNode> initValNodes, List<Token> commaTokens, Token rightBrace) {
        this.commaTokens = commaTokens;
        this.initValNodes = initValNodes;
        this.leftBrace = leftBrace;
        this.rightBrace = rightBrace;
    }
    // InitVal → Exp | '{' [ InitVal { ',' InitVal } ]
    void print() {
        if (expNode != null) {
            expNode.print();
        } else {
            IOUtils.write(leftBrace.toString());

            if (!initValNodes.isEmpty()) {
                initValNodes.get(0).print();

                int index = 0;
                while (index <= commaTokens.size()) {
                    IOUtils.write(commaTokens.get(index).toString());
                    initValNodes.get(index+1).print();
                    index++;
                }
            }

            IOUtils.write(rightBrace.toString());
        }

        IOUtils.write(Parser.nodeType.get(NodeType.InitVal));
    }
}
