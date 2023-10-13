package node;

import frontend.Parser;
import token.Token;
import utils.IOUtils;

import java.util.List;

public class FuncRParamsNode {
    public List<Token> commas;
    public List<ExpNode> expNodes;

    public FuncRParamsNode(List<ExpNode> expNodes, List<Token> commas) {
        this.commas = commas;
        this.expNodes = expNodes;
    }
    // FuncRParams -> Exp { ',' Exp }
    public void print() {
        if (!expNodes.isEmpty()) {
            expNodes.get(0).print();

            int index = 0;
            while (index < commas.size()) {
                IOUtils.write(commas.get(index ).toString());
                expNodes.get(index+1).print();
                index++;
            }
        }

        IOUtils.write(Parser.nodeType.get(NodeType.FuncRParams));
    }
}
