package node;
import frontend.Parser;
import utils.IOUtils;

import java.util.List;
public class CompUnitNode {
    public List<DeclNode> declNodes = null;
    public List<FuncDefNode> funcDefNodes = null;
    public MainFuncDefNode mainFuncDefNode = null;

    public CompUnitNode(List<DeclNode> declNodes, List<FuncDefNode> funcDefNodes, MainFuncDefNode mainFuncDefNode) {
        this.declNodes = declNodes;
        this.funcDefNodes = funcDefNodes;
        this.mainFuncDefNode = mainFuncDefNode;
    }

    public void print() {
        int declIndex = 0;
        while (declIndex < declNodes.size()) {
            declNodes.get(declIndex).print();
            declIndex++;
        }

        int funcDefIndex = 0;
        while (funcDefIndex < funcDefNodes.size()) {
            funcDefNodes.get(funcDefIndex).print();
            funcDefIndex++;
        }

        mainFuncDefNode.print();
        IOUtils.write(Parser.nodeType.get(NodeType.CompUnit));
    }
}