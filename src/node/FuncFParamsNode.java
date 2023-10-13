package node;

import frontend.Parser;
import token.Token;
import utils.IOUtils;

import java.util.List;

public class FuncFParamsNode {
    public List<Token> commaTokens;
    public List<FuncFParamNode> funcFParamNodes;

    public FuncFParamsNode(List<FuncFParamNode> funcFParamNodes, List<Token> commaTokens) {
        this.commaTokens = commaTokens;
        this.funcFParamNodes = funcFParamNodes;
    }

    public void print() {
        if (!funcFParamNodes.isEmpty()) {
            funcFParamNodes.get(0).print();

            int index = 0;
            while (index < commaTokens.size()) {
                IOUtils.write(commaTokens.get(index).toString());
                funcFParamNodes.get(index + 1).print();
                index++;
            }
        }

        IOUtils.write(Parser.nodeType.get(NodeType.FuncFParams));
    }
}
