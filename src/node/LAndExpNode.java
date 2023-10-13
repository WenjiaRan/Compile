package node;

import frontend.Parser;
import token.Token;
import utils.IOUtils;

public class LAndExpNode {
    // LAndExp → EqExp | LAndExp '&&' EqExp
    // LAndExp → EqExp['&&' LAndExp]
    public EqExpNode eqExpNode;
    public Token token;
    public LAndExpNode lAndExpNode;

    public LAndExpNode(EqExpNode eqExpNode, Token token, LAndExpNode lAndExpNode) {
        this.eqExpNode = eqExpNode;
        this.token = token;
        this.lAndExpNode = lAndExpNode;
    }

    public void print(){
        eqExpNode.print();
        IOUtils.write(Parser.nodeType.get(NodeType.LAndExp));
        if(lAndExpNode!=null){
            IOUtils.write(token.toString());
            lAndExpNode.print();
        }
    }
}
