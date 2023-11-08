package frontend;

import error.Error;
import error.ErrorHandler;
import node.*;
import token.Token;
import token.TokenType;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import static frontend.Lexer.instance;
import static node.NodeType.*;


public class Parser {
    public static final Parser instance = new Parser();
    public List<Token> tokens;
    public int index =0;// token的索引
    public CompUnitNode compUnitNode;
//    public static Map<NodeType, String> nodeType;
    public Token now;
    public static Map<NodeType, String> nodeType = new HashMap<NodeType, String>() {{
        put(NodeType.CompUnit, "<CompUnit>\n");
        put(NodeType.Decl, "<Decl>\n");
        put(NodeType.ConstDecl, "<ConstDecl>\n");
        put(NodeType.BType, "<BType>\n");
        put(NodeType.ConstDef, "<ConstDef>\n");
        put(NodeType.ConstInitVal, "<ConstInitVal>\n");
        put(NodeType.VarDecl, "<VarDecl>\n");
        put(NodeType.VarDef, "<VarDef>\n");
        put(NodeType.InitVal, "<InitVal>\n");
        put(NodeType.FuncDef, "<FuncDef>\n");
        put(NodeType.MainFuncDef, "<MainFuncDef>\n");
        put(NodeType.FuncType, "<FuncType>\n");
        put(NodeType.FuncFParams, "<FuncFParams>\n");
        put(NodeType.FuncFParam, "<FuncFParam>\n");
        put(NodeType.Block, "<Block>\n");
        put(NodeType.BlockItem, "<BlockItem>\n");
        put(NodeType.Stmt, "<Stmt>\n");
        put(NodeType.Exp, "<Exp>\n");
        put(NodeType.Cond, "<Cond>\n");
        put(NodeType.LVal, "<LVal>\n");
        put(NodeType.PrimaryExp, "<PrimaryExp>\n");
        put(NodeType.Number, "<Number>\n");
        put(NodeType.UnaryExp, "<UnaryExp>\n");
        put(NodeType.UnaryOp, "<UnaryOp>\n");
        put(NodeType.FuncRParams, "<FuncRParams>\n");
        put(NodeType.MulExp, "<MulExp>\n");
        put(NodeType.AddExp, "<AddExp>\n");
        put(NodeType.RelExp, "<RelExp>\n");
        put(NodeType.EqExp, "<EqExp>\n");
        put(NodeType.LAndExp, "<LAndExp>\n");
        put(NodeType.LOrExp, "<LOrExp>\n");
        put(NodeType.ConstExp, "<ConstExp>\n");
    }};

    public static Parser getInstance() {
        return instance;
    }

    public enum StmtType {
        LValAssignExp, Exp, Block, If, For, Break, Continue, Return, LValAssignGetint, Printf
    }
    public void analyze() {
        this.compUnitNode = CompUnit();
    }
    public void setTokens(List<Token> tokens) {
        this.tokens = tokens;
        now = tokens.get(index);
    }
    public void printParseAns() {
        compUnitNode.print();
    }
    public Token match(TokenType tokenType) {
        if (tokens.get(index).getType() == tokenType) {
            return tokens.get(index++);
        } else {
//            throw new RuntimeException("Syntax error: " + tokens.get(index).toString()
//                    + "should be "+tokenType+"\n at line " + tokens.get(index).getLineNumber()+ "\n "
//                    + tokens.get(index-2).toString()+tokens.get(index-1).toString() +tokens.get(index).toString() +
//                    tokens.get(index+1).toString()+tokens.get(index+2).toString()  );
            // 弱报错, 上面被注释的是强报错, 但注意上面的索引可能越界! 谨慎使用
//            throw new RuntimeException("Syntax error: " + tokens.get(index).toString()
//                    + "should be "+tokenType+"\n at line " + tokens.get(index).getLineNumber()+ "\n ");

            switch (tokenType){
                case RBRACK:
                    //TODO: why index-1?
                    ErrorHandler.instance.addError(new Error(tokens.get(index-1).lineNumber, Error.ErrorType.k));
                    return new Token(TokenType.RBRACK,tokens.get(index-1).lineNumber,"]");
                case SEMICN:
                    ErrorHandler.instance.addError(new Error(tokens.get(index-1).lineNumber, Error.ErrorType.i));
                    return new Token(TokenType.SEMICN, tokens.get(index - 1).lineNumber, ";");
                case RPARENT:
                    ErrorHandler.instance.addError(new Error(tokens.get(index-1).lineNumber, Error.ErrorType.j));
                    return new Token(TokenType.RPARENT, tokens.get(index-1).lineNumber, ")");
                default:
                    throw new RuntimeException("Syntax error at line " + now.getLineNumber() + ": " + now.getContent() + " is not " + tokenType);
            }
        }
    }
    public CompUnitNode CompUnit() {
        // CompUnit -> {Decl} {FuncDef} MainFuncDef
        List<DeclNode> declNodes = new ArrayList<>();
        List<FuncDefNode> funcDefNodes = new ArrayList<>();
        MainFuncDefNode mainFuncDefNode;

        // 判断是否直接就是main
        for (TokenType nextType = tokens.get(index + 1).getType(), nextNextType = tokens.get(index + 2).getType();
             tokens.get(index + 1).getType() != TokenType.MAINTK && tokens.get(index + 2).getType() != TokenType.LPARENT;
             nextType = tokens.get(index + 1).getType(), nextNextType = tokens.get(index + 2).getType()) {
            // 可能有Decl
            DeclNode declNode = Decl();
            declNodes.add(declNode);
        }

        for (TokenType nextType = tokens.get(index + 1).getType();
             tokens.get(index + 1).getType() != TokenType.MAINTK;
             nextType = tokens.get(index + 1).getType()) {
            // FuncDef
            FuncDefNode funcDefNode = FuncDef();
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
        while(tokens.get(index).content.equals("[")){
            leftBracks.add(match(TokenType.LBRACK));
            constExpNodeList.add(ConstExp());
            rightBracks.add(match(TokenType.RBRACK));
        }
        if(!tokens.get(index).content.equals("=")){
            return new VarDefNode(identToken,leftBracks,constExpNodeList,rightBracks,null,null);
        }
        else {
            assignToken=match(TokenType.ASSIGN );
            initValNode=InitVal();
            return new VarDefNode(identToken,leftBracks,constExpNodeList,rightBracks,assignToken,initValNode);
        }
    }
    public InitValNode InitVal() {
        //IInitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'//
        ExpNode expNode ;

        Token leftbrack;
        Token rightbrack;
        List <InitValNode > initValNodeList = new ArrayList<>();
        List <Token> commaList = new ArrayList<>();

        if (tokens.get(index).content.equals("{")){
            leftbrack=match(TokenType.LBRACE);
            if(!tokens.get(index).content.equals("}")) {
                initValNodeList.add(InitVal());
                while(!tokens.get(index).content.equals("}")){
                    commaList.add(match(TokenType.COMMA));
                    initValNodeList.add(InitVal());
                }

            }
            rightbrack=match(TokenType.RBRACE);
            return new InitValNode(leftbrack,initValNodeList,commaList,rightbrack);
        }
        else {
            return new InitValNode(Exp());
        }
    }
    public FuncDefNode FuncDef(){
        //FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
        FuncTypeNode funcTypeNode;
        Token ident;
        Token leftParent;
        Token rightParent = null;;
        FuncFParamsNode funcFParamsNode=null;
        BlockNode blockNode;

        funcTypeNode=FuncType();
        ident=match(TokenType.IDENFR);
        leftParent=match(TokenType.LPARENT);
        if(!tokens.get(index).content.equals("int")&&!tokens.get(index).content.equals(")")) {// 有语法错误, 没有')'
            ErrorHandler.instance.addError(new Error(tokens.get(index).lineNumber, Error.ErrorType.j));
        }
        else{
            if(!tokens.get(index).content.equals(")")){
                funcFParamsNode=FuncFParams();
            }
            rightParent=match(TokenType.RPARENT);
        }
        blockNode=Block();
        return new FuncDefNode(funcTypeNode,ident,leftParent,funcFParamsNode,rightParent,blockNode);
    }

    public MainFuncDefNode MainFuncDef() {
//        MainFuncDef → 'int' 'main' '(' ')' Block //
        Token intToken=match(TokenType.INTTK);
        Token mainToken=match(TokenType.MAINTK);
        Token leftParent=match(TokenType.LPARENT);
        Token rightParent = match(TokenType.RPARENT);
        BlockNode blockNode=Block();
        return new MainFuncDefNode(intToken,mainToken,leftParent,rightParent,blockNode);
    }
    public FuncTypeNode FuncType() {
        if(tokens.get(index).content.equals("void")){
            return new FuncTypeNode(match(TokenType.VOIDTK));
        }
        return new FuncTypeNode(match(TokenType.INTTK));
    }
    public FuncFParamsNode FuncFParams(){
        //FuncFParams → FuncFParam { ',' FuncFParam } //
        List<FuncFParamNode > funcFParamNodeList = new ArrayList<>();
        List<Token> commaTokenList = new ArrayList<>();
        funcFParamNodeList.add(FuncFParam());
        while(tokens.get(index).content.equals(",")){
            commaTokenList.add(match(TokenType.COMMA));
            funcFParamNodeList.add(FuncFParam());

        }
        return new FuncFParamsNode(funcFParamNodeList,commaTokenList);
    }
    public  FuncFParamNode FuncFParam(){
        //FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
        BTypeNode bTypeNode=BType();
        Token identToken=match(TokenType.IDENFR);
        List<Token> lbrackTokenList = new ArrayList<>();
        List<Token> rbrackTokenList = new ArrayList<>();
        List<ConstExpNode> constExpNodeList = new ArrayList<>();
        if(tokens.get(index).content.equals("[")){
            lbrackTokenList.add(match(TokenType.LBRACK));
            rbrackTokenList.add(match(TokenType.RBRACK));
            while(tokens.get(index).content.equals("[")){
                lbrackTokenList.add(match(TokenType.LBRACK));
                constExpNodeList.add(ConstExp());
                rbrackTokenList.add(match(TokenType.RBRACK));
            }
        }
        return new FuncFParamNode(bTypeNode,identToken,lbrackTokenList,rbrackTokenList,constExpNodeList);
    }
    public BlockNode Block(){
        //Block → '{' { BlockItem } '}'
        Token lbraceToken=match(TokenType.LBRACE);
        List<BlockItemNode> blockItemNodeList = new ArrayList<>();
        while(!tokens.get(index).content.equals("}")){
            blockItemNodeList.add(BlockItem());
        }
        Token rbraceToken=match(TokenType.RBRACE);
        return new BlockNode(lbraceToken,blockItemNodeList,rbraceToken);
    }
    public BlockItemNode BlockItem(){
//        BlockItem → Decl | Stmt
        //ConstDecl | VarDecl
        if(tokens.get(index).content.equals("const")||tokens.get(index).content.equals("int")){
            return new BlockItemNode(Decl());
        }
        return new BlockItemNode(Stmt());
    }
    public StmtNode Stmt(){
        if(tokens.get(index).content.equals("break")){
            int x=2;
            x=x+3;
        }
        //Block
        if (tokens.get(index).content.equals("{")){
            return new StmtNode(StmtNode.StmtType.Block,Block());
        }
        //'if' '(' Cond ')' Stmt [ 'else' Stmt ]
        else if(tokens.get(index).content.equals("if")){
            Token ifToken=match(TokenType.IFTK);
            Token lParentToken=match(TokenType.LPARENT);
            CondNode condNode = Cond();
            Token rParentToken=match(TokenType.RPARENT);
            List<StmtNode> stmtNodeList = new ArrayList<>();
            stmtNodeList.add(Stmt());
            Token elseToken=null;
            if(tokens.get(index).content.equals("else")){
                elseToken=match(TokenType.ELSETK);
                stmtNodeList.add(Stmt());
            }
            return new StmtNode(StmtNode.StmtType.If,ifToken,lParentToken,condNode,rParentToken,stmtNodeList,elseToken);
        }
        //'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
        else if(tokens.get(index).content.equals("for")){
            Token forToken=match(TokenType.FORTK);
            Token lParentToken=match(TokenType.LPARENT);
            List<ForStmtNode> forStmtNodeList = new ArrayList<>();
            List<Token> semiTokenList = new ArrayList<>();

            CondNode condNode=null;
            if(!tokens.get(index).content.equals(")")){
                if(!tokens.get(index).content.equals(";")){
                    forStmtNodeList.add(ForStmt());
                }
                else{
                    forStmtNodeList.add(null);
                }
                semiTokenList.add(match(TokenType.SEMICN));
                if(!tokens.get(index).content.equals(";")){
                    condNode=Cond();
                }
                semiTokenList.add(match(TokenType.SEMICN));
                if(!tokens.get(index).content.equals(")")){
                    forStmtNodeList.add(ForStmt());
                }
                else{
                    forStmtNodeList.add(null);
                }
            }

            Token rParentToken=match(TokenType.RPARENT);
            StmtNode stmtNode = Stmt();
            return new StmtNode(StmtNode.StmtType.For,forToken,lParentToken,forStmtNodeList,semiTokenList,
                    condNode,rParentToken,stmtNode);
        }
        //'break' ';' | 'continue' ';'
        else if(tokens.get(index).content.equals("break")&&
                tokens.get(index+1).content.equals(";")){
            Token breakToken=match(TokenType.BREAKTK);
            Token semiToken=match(TokenType.SEMICN  );
            return new StmtNode(StmtNode.StmtType.Break,breakToken,semiToken);
        }
        else if(tokens.get(index).content.equals("continue")&&
                tokens.get(index+1).content.equals(";")){
            Token breakToken=match(TokenType.CONTINUETK);
            Token semiToken=match(TokenType.SEMICN  );
            return new StmtNode(StmtNode.StmtType.Continue,breakToken,semiToken);
        }
        //'return' [Exp] ';' //
        else if(tokens.get(index).content.equals("return")){
            Token returnToken=match(TokenType.RETURNTK);
            ExpNode expNode=null;
            if(!tokens.get(index).content.equals(";")){
                expNode=Exp();
            }
            Token semicnToken=match(TokenType.SEMICN);
            return new StmtNode(StmtNode.StmtType.Return,returnToken,expNode,semicnToken);
        }
        // 'printf''('FormatString{','Exp}')'';'
        else if(tokens.get(index).content.equals("printf")){
            Token printfToken=match(TokenType.PRINTFTK);
            Token lParentToken=match(TokenType.LPARENT);
            Token formatStringNode=match(TokenType.STRCON);
            List<Token> commaTokenList = new ArrayList<>();
            List<ExpNode> expNodeList = new ArrayList<>();
            while(!tokens.get(index).content.equals(")")){
                commaTokenList.add(match(TokenType.COMMA));
                expNodeList.add(Exp()   );
            }
            Token rParentToken=match(TokenType.RPARENT);
            Token semiToken=match(TokenType.SEMICN);
            return new StmtNode(StmtNode.StmtType.Printf,printfToken,lParentToken,
                    formatStringNode,commaTokenList,expNodeList,rParentToken,semiToken);
        }
        // 先搞左值, 找等号嘛!!!
        int i=index,isLVal=0;
        for(;i<tokens.size();i++){
            if(tokens.get(i).content.equals(";")){
                break;//语句结束了, 也没找到=
            }
            if(tokens.get(i).content.equals("=")){
                isLVal=1;
                break;
            }
        }
        //[Exp] ';'
        if(isLVal==0){
            ExpNode expNode=null;
            Token semiToken=null;
            if(tokens.get(index).content.equals(";")){
                semiToken=match(TokenType.SEMICN);
            }
            else{
                expNode=Exp();
                semiToken=match(TokenType.SEMICN);
            }
            return new StmtNode(StmtNode.StmtType.Exp,expNode,semiToken);
        }
        //PrimaryExp | Ident
        //(
        // LVal → Ident {'[' Exp ']'}
        // 只剩左值了
        LValNode lValNode = LVal();
        Token assignToken  =match(TokenType.ASSIGN);
        if(tokens.get(index).content.equals("getint")){
            Token getintToken = match(TokenType.GETINTTK);
            Token lParentToken = match(TokenType.LPARENT);
            Token rParentToken = match(TokenType.RPARENT);
            Token setintToken = match(TokenType.SEMICN);
            return new StmtNode(StmtNode.StmtType.LValGetint,lValNode,assignToken,getintToken,
                    lParentToken,rParentToken,setintToken   );
        }
        else{
            ExpNode expNode=Exp();
            Token setintToken = match(TokenType.SEMICN);
            return new StmtNode(StmtNode.StmtType.LValAssignExp,lValNode,
                    assignToken,expNode,setintToken );
        }


    }

    public ForStmtNode ForStmt(){
        //ForStmt → LVal '=' Exp
        LValNode lValNode=LVal();
        Token assginToken=match(TokenType.ASSIGN);
        ExpNode expNode=Exp();
        return new ForStmtNode(lValNode,assginToken,expNode);
    }
    public ExpNode Exp(){
        //Exp → AddExp
        return new ExpNode(AddExp());
    }
    public CondNode Cond(){
        return new CondNode(LOrExp());
    }
    public LValNode LVal(){
        //LVal → Ident {'[' Exp ']'}
        if(tokens.get(index).content.equals("break")){
            int x=3+3;
            x=2+x;
        }
        Token identToken=match(TokenType.IDENFR);
        List<Token> lBrackList = new ArrayList<>();
        List<Token> rBrackList = new ArrayList<>();
        List<ExpNode> expNodeList = new ArrayList<>();
        while(tokens.get(index).content.equals("[")){
            lBrackList.add(match(TokenType.LBRACK));
            expNodeList.add(Exp());
            rBrackList.add(match(TokenType.RBRACK));
        }
        return new LValNode(identToken,lBrackList,expNodeList,rBrackList);
    }
    public PrimaryExpNode PrimaryExp(){
        //PrimaryExp → '(' Exp ')' | LVal | Number //
        if(tokens.get(index).content.equals("(")){
            Token lParentToken=match(TokenType.LPARENT);
            ExpNode expNode=Exp();
            Token rParentToken=match(TokenType.RPARENT);

            return new PrimaryExpNode(lParentToken,expNode,rParentToken);
        }
        else if(tokens.get(index).type==TokenType.INTCON){
            return new PrimaryExpNode(Number());
        }
        else return new PrimaryExpNode(LVal());
    }
    public NumberNode Number() {
        return new NumberNode(match(TokenType.INTCON));
    }
    public UnaryExpNode UnaryExp(){
        //UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')'|UnaryOp UnaryExp
        //UnaryOp UnaryExp
        switch(tokens.get(index).content) {
            case "+": case "-": case "!":
                return new UnaryExpNode(UnaryOp(),UnaryExp());
        }
        //Ident '(' [FuncRParams] ')'
        if(tokens.get(index).type==TokenType.IDENFR&&
                tokens.get(index+1).content.equals("(")){

            Token identToken=match(TokenType.IDENFR);
            Token lParentToken=match(TokenType.LPARENT);
            Token rParentToken = null;
            FuncRParamsNode funcRParamsNode=null;
            if(!tokens.get(index).content.equals(")")){
                if(tokens.get(index).type!=TokenType.IDENFR&&tokens.get(index).type!=TokenType.INTCON){
                    ErrorHandler.instance.addError(new Error(tokens.get(index).lineNumber, Error.ErrorType.j));
                }
                else
                {
                    funcRParamsNode=FuncRParams();
                    rParentToken=match(TokenType.RPARENT);
                }
            }else{
                rParentToken=match(TokenType.RPARENT);
            }

            return new UnaryExpNode(identToken,lParentToken,funcRParamsNode,rParentToken);
        }

        return new UnaryExpNode(PrimaryExp());
    }
    public UnaryOpNode UnaryOp(){
        //UnaryOp → '+' | '−' | '!'
        Token token=null;

        switch (tokens.get(index).content){
            case "+":token=match(TokenType.PLUS);break;
            case "-":token=match(TokenType.MINU);break;
            case "!":token=match(TokenType.NOT);break;
        }
        return new UnaryOpNode(token);
    }
    public FuncRParamsNode FuncRParams(){
        //FuncRParams → Exp { ',' Exp }
        List<ExpNode> expNodeList = new ArrayList<>();
        List<Token> commaTokenList = new ArrayList<>();
        expNodeList.add(Exp());
        while(tokens.get(index).content.equals(",")){
             commaTokenList.add(match(TokenType.COMMA));
             expNodeList.add(Exp());
        }
        return new FuncRParamsNode(expNodeList,commaTokenList);
    }
    public MulExpNode MulExp(){
        // MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
        // MulExp → UnaryExp [('*' | '/' | '%') MulExp]
        UnaryExpNode unaryExpNode=UnaryExp();
        Token token=null;
        MulExpNode mulExpNode=null;
        switch (tokens.get(index).content){
            case "*":
                token=match(TokenType.MULT  );
                mulExpNode=MulExp();
                break;
            case "/":
                token=match(TokenType.DIV  );
                mulExpNode=MulExp();
                break;
            case "%":
                token=match(TokenType.MOD  );
                mulExpNode=MulExp();
                break;
        }
        return new MulExpNode(unaryExpNode,token,mulExpNode);
    }
    public AddExpNode AddExp(){
        // // AddExp -> MulExp | AddExp ('+' | '−') MulExp
        // AddExp -> MulExp [ ('+' | '−') AddExp]
        MulExpNode mulExpNode=MulExp();
        Token token=null;
        AddExpNode addExpNode =null;
        switch (tokens.get(index).content){
            case "+":
                token=match(TokenType.PLUS);
                addExpNode=AddExp();
                break;
            case "-":
                token=match(TokenType.MINU);
                addExpNode=AddExp();
                break;
        }
        return new AddExpNode(mulExpNode,token,addExpNode);
    }
    public RelExpNode RelExp(){
        //RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
        //RelExp -> AddExp [ ('<' | '>' | '<=' | '>=') RelExp]
        AddExpNode addExpNode = AddExp();
        Token token=null;
        RelExpNode  relExpNode = null;
        switch (tokens.get(index).content) {
            case "<" -> {
                token = match(TokenType.LSS);
                relExpNode = RelExp();
            }
            case ">" -> {
                token = match(TokenType.GRE);
                relExpNode = RelExp();
            }
            case "<=" -> {
                token = match(TokenType.LEQ);
                relExpNode = RelExp();
            }
            case ">=" -> {
                token = match(TokenType.GEQ);
                relExpNode = RelExp();
            }
        }
        return new RelExpNode(addExpNode,token, relExpNode);
    }
    public EqExpNode EqExp(){
        //EqExp → RelExp | EqExp ('==' | '!=') RelExp
        //EqExp -> RelExp [('==' | '!=') EqExp]
        RelExpNode relExpNode=RelExp();
        Token token=null;
        EqExpNode eqExpNode=null;
        switch(tokens.get(index).content){
            case "==":token=match(TokenType.EQL);eqExpNode=EqExp();break;
            case "!=":token=match(TokenType.NEQ);eqExpNode=EqExp();break;

        }
        return new EqExpNode(relExpNode,token,eqExpNode);
    }
    public LAndExpNode LAndExp(){
//        LAndExp → EqExp | LAndExp '&&' EqExp
        // LAndExp-> EqExp ['&&' LAndExp]
        EqExpNode eqExpNode=EqExp();
        Token token=null;
        LAndExpNode lAndExpNode=null;
        switch(tokens.get(index).content){
            case "&&":token=match(TokenType.AND);lAndExpNode=LAndExp();break;

        }
        return new LAndExpNode(eqExpNode,token,lAndExpNode);
    }
    public LOrExpNode LOrExp(){
        // LOrExp -> LAndExp ['||' LOrExp]
        LAndExpNode lAndExpNode=LAndExp();
        Token token=null;
        LOrExpNode lOrExpNode=null;
        switch(tokens.get(index).content){
            case "||":token=match(TokenType.OR);lOrExpNode=LOrExp();
        }
        return new LOrExpNode(lAndExpNode,token,lOrExpNode);
    }
    public ConstExpNode ConstExp(){
        return new ConstExpNode(AddExp());
    }
}














