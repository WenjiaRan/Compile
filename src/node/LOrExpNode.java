package node;

import frontend.Parser;
import token.Token;
import utils.IOUtils;

public class LOrExpNode {
    //LOrExp → LAndExp | LOrExp '||' LAndExp
    //LOrExp → LAndExp['||' LOrExp]
    public LAndExpNode lAndExpNode;
    public Token token;
    public LOrExpNode lOrExpNode;;

    public LOrExpNode(LAndExpNode lAndExpNode, Token token, LOrExpNode lOrExpNode) {
        this.lAndExpNode = lAndExpNode;
        this.token = token;
        this.lOrExpNode = lOrExpNode;
    }

    public void print(){
        lAndExpNode.print();
        IOUtils.write(Parser.nodeType.get(NodeType.LOrExp));
        if(lOrExpNode!=null){
            IOUtils.write(token.toString());
            lOrExpNode.print();
        }
    }
















}
