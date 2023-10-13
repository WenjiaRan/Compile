package node;

import frontend.Parser;
import token.Token;
import utils.IOUtils;

import java.lang.reflect.Parameter;
import java.util.List;

public class EqExpNode {
    //EqExp → RelExp | EqExp ('==' | '!=') RelExp
    //EqExp -> RelExp[('==' | '!=')EqExp]
    public RelExpNode relExpNode;
    public Token operator;
    public EqExpNode eqExpNode;

    public EqExpNode(RelExpNode relExpNode, Token operator, EqExpNode eqExpNode) {
        this.relExpNode = relExpNode;
        this.operator = operator;
        this.eqExpNode = eqExpNode;
    }

    public void print(){
        relExpNode.print();
        IOUtils.write(Parser.nodeType.get(NodeType.EqExp));
        if(operator!=null){
            IOUtils.write(operator.toString());
            eqExpNode.print();
        }
    }












}
