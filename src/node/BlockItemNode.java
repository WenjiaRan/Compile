package node;

public class BlockItemNode {
    // BlockItem → Decl | Stmt
    private  DeclNode declNode;
    private StmtNode stmtNode;

    public BlockItemNode(DeclNode declNode) {
        this.declNode = declNode;
    }

    public BlockItemNode(StmtNode stmtNode) {
        this.stmtNode = stmtNode;
    }

    public void print(){
        if(declNode != null){
            declNode.print();
        }
        else {
            stmtNode.print();
        }

    }















}
