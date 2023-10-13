package frontend;

import node.*;
import token.Token;
import token.TokenType;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import static node.NodeType.*;


public class Parser {
    public List<Token> tokens;
    public int index =0;// token的索引
    public CompUnitNode compUnitNode;
    public static Map<NodeType, String> nodeType;
    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        nodeType = new HashMap<>();
        nodeType.put(NodeType.CompUnit, "<CompUnit>\n");
        nodeType.put(NodeType.Decl, "<Decl>\n");
        nodeType.put(NodeType.ConstDecl, "<ConstDecl>\n");
        nodeType.put(BType, "<BType>\n");
        nodeType.put(NodeType.ConstDef, "<ConstDef>\n");
        nodeType.put(NodeType.ConstInitVal, "<ConstInitVal>\n");
        nodeType.put(NodeType.VarDecl, "<VarDecl>\n");
        nodeType.put(NodeType.VarDef, "<VarDef>\n");
        nodeType.put(NodeType.InitVal, "<InitVal>\n");
        nodeType.put(NodeType.FuncDef, "<FuncDef>\n");
        nodeType.put(NodeType.MainFuncDef, "<MainFuncDef>\n");
        nodeType.put(NodeType.FuncType, "<FuncType>\n");
        nodeType.put(NodeType.FuncFParams, "<FuncFParams>\n");
        nodeType.put(NodeType.FuncFParam, "<FuncFParam>\n");
        nodeType.put(NodeType.Block, "<Block>\n");
        nodeType.put(NodeType.BlockItem, "<BlockItem>\n");
        nodeType.put(NodeType.Stmt, "<Stmt>\n");
        nodeType.put(NodeType.Exp, "<Exp>\n");
        nodeType.put(NodeType.Cond, "<Cond>\n");
        nodeType.put(NodeType.LVal, "<LVal>\n");
        nodeType.put(NodeType.PrimaryExp, "<PrimaryExp>\n");
        nodeType.put(NodeType.Number, "<Number>\n");
        nodeType.put(NodeType.UnaryExp, "<UnaryExp>\n");
        nodeType.put(NodeType.UnaryOp, "<UnaryOp>\n");
        nodeType.put(NodeType.FuncRParams, "<FuncRParams>\n");
        nodeType.put(NodeType.MulExp, "<MulExp>\n");
        nodeType.put(NodeType.AddExp, "<AddExp>\n");
        nodeType.put(NodeType.RelExp, "<RelExp>\n");
        nodeType.put(NodeType.EqExp, "<EqExp>\n");
        nodeType.put(NodeType.LAndExp, "<LAndExp>\n");
        nodeType.put(NodeType.LOrExp, "<LOrExp>\n");
        nodeType.put(NodeType.ConstExp, "<ConstExp>\n");
        nodeType.put(NodeType.ForStmt, "<ForStmt>\n");
    }
    public enum StmtType {
        LValAssignExp, Exp, Block, If, For, Break, Continue, Return, LValAssignGetint, Printf
    }
    public void analyze() {
        this.compUnitNode = CompUnit();
    }

    public Token match(TokenType tokenType) {
        if (tokens.get(index).getType() == tokenType) {
            return tokens.get(index++);
        } else {
            throw new RuntimeException("Syntax error: " + tokens.get(index).toString() + " at line " + tokens.get(index).getLineNumber());
        }
    }
    public CompUnitNode CompUnit() {
        // CompUnit -> {Decl} {FuncDef} MainFuncDef
        List<DeclNode> declNodes = new ArrayList<>();
        List<FuncDefNode> funcDefNodes = new ArrayList<>();
        MainFuncDefNode mainFuncDefNode;

        // 判断是否直接就是main
        for (TokenType nextType = tokens.get(index + 1).getType(), nextNextType = tokens.get(index + 2).getType();
             nextType != TokenType.MAINTK && nextNextType != TokenType.LPARENT;
             nextType = tokens.get(index + 1).getType(), nextNextType = tokens.get(index + 2).getType()) {
            // 可能有Decl
            DeclNode declNode = Decl();
            declNodes.add(declNode);
        }

        for (TokenType nextType = tokens.get(index + 1).getType();
             nextType != TokenType.MAINTK;
             nextType = tokens.get(index + 1).getType()) {
            // FuncDef
            FuncDefNode funcDefNode = funcDef();
            funcDefNodes.add(funcDefNode);
        }

        mainFuncDefNode = MainFuncDef();
        return new CompUnitNode(declNodes, funcDefNodes, mainFuncDefNode);
    }


    public DeclNode Decl() {
        //Decl → ConstDecl | VarDecl
        ConstDeclNode constDeclNode;
        VarDeclNode varDeclNode;
        if (tokens.get(index).getType()==TokenType.CONSTTK) {
            constDeclNode = ConstDecl();
            return new DeclNode(constDeclNode);
        }
        else {
            varDeclNode = VarDecl();
            return new DeclNode(varDeclNode);
        }
    }

    public ConstDeclNode ConstDecl() {
        // ConstDecl -> 'const' BType ConstDef { ',' ConstDef } ';'
        Token constToken = match(TokenType.CONSTTK);
        BTypeNode bTypeNode = BType();
        List<ConstDefNode> constDefList = new ArrayList<>();
        List<Token> commaList = new ArrayList<>();
        Token semicnToken ;

        constDefList.add(ConstDef());

        while(tokens.get(index).getType()==TokenType.COMMA) {
            commaList.add(match(TokenType.COMMA));
            constDefList.add(ConstDef());
        }
        semicnToken = match(TokenType.SEMICN);
        return new ConstDeclNode(constToken,bTypeNode,constDefList,commaList,semicnToken);
    }

    public BTypeNode BType(){
        Token token=match(TokenType.INTTK);
        return new BTypeNode(token);
    }
    public ConstDefNode ConstDef(){
        // ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
        Token ident=match(TokenType.IDENFR);
        List<Token> leftSquareBrackList=new ArrayList<>();
        List<ConstExpNode> constExpNodeList = new ArrayList<>();
        List<Token> rightSquareBrackList=new ArrayList<>();
        Token assignToken ;
        ConstInitValNode constInit ;
        while(tokens.get(index).getType()==TokenType.LBRACK){
            leftSquareBrackList.add(match(TokenType.LBRACK));
            constExpNodeList.add(ConstExp());
            rightSquareBrackList.add(match(TokenType.RBRACK));
        }
        assignToken=match(TokenType.ASSIGN);
        constInit=ConstInitVal();
        return new ConstDefNode(ident, leftSquareBrackList, constExpNodeList,rightSquareBrackList,assignToken,constInit);
    }
    public ConstInitValNode ConstInitVal(){
        // ConstInitVal → ConstExp| '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
        if (tokens.get(index).getType() !=TokenType.LBRACE){
            ConstExpNode constExpNode=ConstExp();
            return new ConstInitValNode(constExpNode);
        }
        else {
            Token leftBrace=match(TokenType.LBRACE);
            List<ConstInitValNode> constInitValNodes=new ArrayList<ConstInitValNode>();
            List<Token> commaTokens =new ArrayList<>();
            Token rightBrace;
            if (tokens.get(index).getType() !=TokenType.RBRACE){
                constInitValNodes.add(ConstInitVal());
                while(tokens.get(index).getType() ==TokenType.COMMA){
                    commaTokens.add(match(TokenType.COMMA));
                    constInitValNodes.add(ConstInitVal());
                }

            }
            rightBrace=match(TokenType.RBRACE);
            return new ConstInitValNode(leftBrace,constInitValNodes,commaTokens,rightBrace);
        }
    }

    public VarDeclNode VarDecl(){
        //VarDecl → BType VarDef { ',' VarDef } ';'
        BTypeNode bTypeNode;
        List<VarDefNode> varDefNodeList = new ArrayList<VarDefNode>();
        List<Token> commaTokens = new ArrayList<Token>();
        Token semicn;

        bTypeNode=BType();
        varDefNodeList.add(VarDef());
        while(tokens.get(index).getType()==TokenType.COMMA){
            commaTokens.add(match(TokenType.COMMA));
            varDefNodeList.add(VarDef());
        }
        semicn=match(TokenType.SEMICN);
        return new VarDeclNode(bTypeNode,varDefNodeList,commaTokens,semicn);
    }

    public VarDefNode VarDef() {
//        VarDef → Ident { '[' ConstExp ']' }
        //| Ident { '[' ConstExp ']' } '=' InitVal
        Token identToken;
        List<Token> leftBracks=new ArrayList<Token>();
        List<ConstExpNode> constExpNodeList=new ArrayList<ConstExpNode>();
        List<Token> rightBracks=new ArrayList<Token>();

        Token assignToken;
        InitValNode initValNode;

        identToken=match(TokenType.IDENFR);
        while(tokens.get(index).content.equals('[')){
            leftBracks.add(match(TokenType.LBRACK));
            constExpNodeList.add(ConstExp());
            rightBracks.add(match(TokenType.RBRACK));
        }
        if(!tokens.get(index).content.equals('=')){
            return new VarDefNode(identToken,leftBracks,constExpNodeList,rightBracks,null,null);
        }
        else {
            assignToken=match(TokenType.ASSIGN );
            initValNode=InitVal();
            return new VarDefNode(identToken,leftBracks,constExpNodeList,rightBracks,assignToken,initValNode);
        }
    }
    public InitValNode InitVal() {
        //InitVal → Exp | '{' [ InitVal { ',' InitVal } ]
        ExpNode
    }
}












