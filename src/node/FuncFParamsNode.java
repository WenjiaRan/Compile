package node;

import frontend.Parser;
import token.Token;
import utils.IOUtils;

import java.util.List;

public class FuncFParamsNode {
    // FuncFParams → FuncFParam { ',' FuncFParam }
    private List<FuncFParamNode> funcFParamNodes;
    private List<Token> commaTokens;

    public FuncFParamsNode(List<FuncFParamNode> funcFParamNodes, List<Token> commaTokens) {
        this.funcFParamNodes = funcFParamNodes;
        this.commaTokens = commaTokens;
    }

    public void print(){
        funcFParamNodes.get(0).print();
        for(int i=0;i<commaTokens.size();i++){
            IOUtils.write(commaTokens.get(i).toString());
            funcFParamNodes.get(i+1).print();
        }
        IOUtils.write(Parser.nodeType.get(NodeType.FuncFParams));
    }












}
