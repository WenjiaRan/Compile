package node;

public class BlockItemNode {
    public DeclNode declNode = null;
    public StmtNode stmtNode = null;

    public BlockItemNode(DeclNode declNode, StmtNode stmtNode) {
        this.declNode = declNode;
        this.stmtNode = stmtNode;
    }

    public void print() {
        if (declNode != null) {
            declNode.print();
        } else {
            stmtNode.print();
        }}
}
