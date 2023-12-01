package llvmir;

import llvmir.type.Type;
import node.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import symbol.ArraySymbol;
import symbol.FuncParam;
import symbol.FuncSymbol;
import symbol.Symbol;
import token.TokenType;
import utils.Triple;


class Double {
    static enum Type {
        FOR,
        IF
    }

    static enum StmtType {
        ForStmt1,//useless
        CondLable,
        ForStmt2,
        ForMainStmt,
        NextBlock,

        IfCond,
        IfStmt,
        ElseStmt,

        Continue,//读到就进栈, 这样就知道
        Break,


    }


    public Type type;//"for" "if"
    public Map<StmtType ,String> map=new HashMap<>();//<stmtType,regName>

    public Double(Type type, Map<StmtType, String> map) {
        this.type = type;//"
        this.map = map;
    }

    public Double() {
    }

    public Double(Type type) {
        this.type = type;
    }
}

public class IRGenerator {
    public Stack<Boolean> localArrayInFunc = new Stack<Boolean>();// 函数里的本地数组(非函数参数)处理有问题, 需要设一个栈, 这这部分本地数组,在main相关的部分处理
    public Boolean continueInIf=false;
    public Boolean returnInIf=false;
    public Stack<Double> branchStack = new Stack<>();// 这个栈专门记录for,if的嵌套,和各个类型语句(ifstmt,elsestmt...)的标签
    public Stack<Boolean> isNotInFuncStack = new Stack<>();
    public Stack<List<Integer>> funcRParamsArrayStack = new Stack<>();
    public List<Integer> funcRParamsArray=new ArrayList<>();
    public Stack<Boolean> isFuncParamStack = new Stack<>();
    public Stack<Boolean> isInExpStack=new Stack<>();//用栈来标志, 防止递归的时候把isInExp改了
    public Boolean isArray=false;
    public Boolean isReturn=false;
    public  Boolean isElseBreak=false;
    public Boolean isElseContinue=false;
    public Boolean isContinue=false;
    public  Boolean isBreak=false;
    public HashMap<String,Integer> regHash=new HashMap<>();//reg的名字和存的值的类型1OR32, 为了方便起见, 只存类型为1bit的
    public Integer tmpAns;
    public Boolean isRelation=false;
    public Boolean isIfCondEnd=false;
    public StmtNode globalIfStmt=null;
    public static Boolean isInBranch=false;//如下面的注释的情况
//    7:                                                ; preds = %0
//            %8 = load i32, i32* %2, align 4
//            %9 = load i32, i32* %3, align 4
//            %10 = icmp slt i32 %8, %9
//    br i1 %10, label %11, label %12
    public static Boolean isAnd=false;
    public static Boolean isOr=false;
    public static Integer copyRegNum;//保存全局regNum的值
    //    public static Integer regNum;
    public static boolean isInFunc = false;
    public static boolean isInFuncWithNoParams = false;
    public List<Type> tmpTypeList = null;
    public static boolean isInExp = false;
    public static boolean isConst;
    public static boolean isGlobal = false;
    public static int regNum = 1;
    public static Integer saveValue = null;
    public static String tmpValue = null;
    public static String saveOp = null;
    public static IRGenerator irGenerator = new IRGenerator();
    public static StringBuilder llvmir = new StringBuilder();
    public List<Triple<Map<String, Symbol>, Boolean, FuncSymbol.ReturnType>>
            symbolTables = new ArrayList<>();


    public String findIndentation() {//获取代码缩进
//        int size=1;
//        StringBuilder sb = new StringBuilder();
//        for(int i=0; i<size*4; i++) {
//            sb.append(" ");
//        }
//        return sb.toString();
        return "";
    }

    public String getValueFromVar(String ident) {
        for (int i = symbolTables.size() - 1; i >= 0; i--) {
            if (symbolTables.get(i).first.containsKey(ident)) {
                Symbol symbol = symbolTables.get(i).first.get(ident);
                if (symbol instanceof ArraySymbol) {
                    if (((ArraySymbol) symbol).dimension == 0) {
                        return ((ArraySymbol) symbol).value;
                    }

                }

            }
        }
        return null;
    }


    public void changeValueFromVar(String ident, Integer newValue) {//值变了,寄存器也要变!
        for (int i = symbolTables.size() - 1; i >= 0; i--) {
            if (symbolTables.get(i).first.containsKey(ident)) {
                Symbol symbol = symbolTables.get(i).first.get(ident);
                if (symbol instanceof ArraySymbol) {
                    ((ArraySymbol) symbol).value = String.valueOf(newValue);
//                    ((ArraySymbol) symbol).regiName= String.valueOf(regNum);
                }

            }
        }
    }

    public String getRegiNameFromVar(String ident) {
        for (int i = symbolTables.size() - 1; i >= 0; i--) {
            if (symbolTables.get(i).first.containsKey(ident)) {
                Symbol symbol = symbolTables.get(i).first.get(ident);
                if (symbol instanceof ArraySymbol) {
                    return ((ArraySymbol) symbol).regiName;
                }

            }
        }
        return null;
    }
    private String getValueFromVar(String ident, Integer x, Integer y) {
        for (int i = symbolTables.size() - 1; i >= 0; i--) {
            if (symbolTables.get(i).first.containsKey(ident)) {
                Symbol symbol = symbolTables.get(i).first.get(ident);
                if (symbol instanceof ArraySymbol) {
                    if (y == null) {
                        return String.valueOf(((ArraySymbol) symbol).value1d.get(x));
                    }
                    else{
                        return String.valueOf(((ArraySymbol) symbol).value2d.get(x).get(y));
                    }
                }

            }
        }
        return null;
    }

    public String getValueFromRegiName(String regiName) {
        for (int i = symbolTables.size() - 1; i >= 0; i--) {
            Map<String, Symbol> symbol = symbolTables.get(i).first;
            for (Map.Entry<String, Symbol> entry : symbol.entrySet()) {
                String key = entry.getKey();
                if (entry.getValue() instanceof ArraySymbol) {
                    ArraySymbol value = (ArraySymbol) entry.getValue();
                    if (Objects.equals(value.regiName, regiName)) {
                        if (Objects.equals(value.value, "null")) {
                            return null;
                        }
                        return value.value;
                    }
                }


            }
        }
        return null;
    }

    public void addSymbolTable(boolean isFunc, FuncSymbol.ReturnType funcType) {
        symbolTables.add(new Triple<>(new HashMap<>(), isFunc, funcType));
    }

    public int isInCurTable(String ident) { // 变量是否在当前的基本块符号表
        if (symbolTables.get(symbolTables.size() - 1).first.containsKey(ident)) {
            return 1;
        }
        return 0;
    }

    public FuncSymbol.ReturnType findCurFunc() {//在函数体内时, 获得符号表关于该函数的内容
        for (int i = symbolTables.size() - 1; i >= 0; i--) {
            if (symbolTables.get(i).third != null) {
                return symbolTables.get(i).third;
            }
        }
        return null;
    }

    public void removeSymbolTable() {
        symbolTables.remove(symbolTables.size() - 1);
    }

    // 计算方法
    public int calculate(String op, int a, int b) {
        return switch (op) {
            case "+", "add" -> a + b;
            case "-","sub" -> a - b;
            case "*","mul" -> a * b;
            case "/","sdiv" -> a / b;
            case "%","srem" -> a % b;
            default -> 0;
        };
    }


    public void remainRegNum() { //定义函数的时候需要用到一套新的reg
        copyRegNum = regNum;
        regNum = 0;
    }

    public void restoreRegNum() {// 恢复寄存器的值, 回到main函数
        regNum = copyRegNum;
    }

    public void visitCompUnit(CompUnitNode compUnitNode) {
        //CompUnit    → {Decl} {FuncDef} MainFuncDef
        addSymbolTable(false, null);
        llvmir.append("""
                declare i32 @getint()
                declare void @putint(i32)
                declare void @putch(i32)
                declare void @putstr(i8*)
                                
                """);
        // CompUnit -> {Decl} {FuncDef} MainFuncDef
        for (DeclNode declNode : compUnitNode.declNodes) {
            isGlobal = true;
            visitDecl(declNode);
            isGlobal = false;
        }

        for (FuncDefNode funcDefNode : compUnitNode.funcDefNodes) {
            remainRegNum();
            regHash.clear();
            visitFuncDef(funcDefNode);
            restoreRegNum();
        }
        isGlobal = false;
        regHash.clear();
        visitMainFuncDef(compUnitNode.mainFuncDefNode);
    }

    private void visitFuncDef(FuncDefNode funcDefNode) {
        //FuncDef     → FuncType Ident '(' [FuncFParams] ')' Block
        isGlobal = false;
        isInFunc = true;
        // 函数名进符号表
        FuncSymbol.ReturnType returnType = Objects.equals(funcDefNode.funcTypeNode.token.content, "void") ?
                FuncSymbol.ReturnType.VOID : FuncSymbol.ReturnType.INT;
        //无参数
        if (funcDefNode.funcFParamsNode == null) {
            //void
            if (returnType == FuncSymbol.ReturnType.VOID) {
                llvmir.append("define dso_local void " + "@" + funcDefNode.ident.content + "(");
                isInFuncWithNoParams = true;
                put(funcDefNode.ident.content, new FuncSymbol
                        ("@" + funcDefNode.ident.content,
                                Objects.equals(funcDefNode.funcTypeNode.token.content, "void") ?
                                        FuncSymbol.ReturnType.VOID : FuncSymbol.ReturnType.INT, new ArrayList<>()));
            }
            //int
            else {
                llvmir.append("define dso_local i32 " + "@" + funcDefNode.ident.content + "(");
                put(funcDefNode.ident.content, new FuncSymbol
                        ("@" + funcDefNode.ident.content, Objects.equals(funcDefNode.funcTypeNode.token.content, "void") ?
                                FuncSymbol.ReturnType.VOID : FuncSymbol.ReturnType.INT, new ArrayList<>()));
            }

        }
        else {
            // 有参数
            //void
            //FuncDef     → FuncType Ident '(' [FuncFParams] ')' Block
            if (returnType == FuncSymbol.ReturnType.VOID) {
                isInFuncWithNoParams = false;
                List<FuncParam> params = new ArrayList<>();
                int i = 0;
                for (FuncFParamNode funcFParamNode :
                        funcDefNode.funcFParamsNode.funcFParamNodes) {
                    i++;
                    if (funcFParamNode.leftSqareBrack.size() < 2) {
                        params.add(new FuncParam(funcFParamNode.ident.content,
                                funcFParamNode.leftSqareBrack.size()));
                    }
                    else {
                        isGlobal=true;
                        visitConstExp(funcFParamNode.constExpNodes.get(0));
                        isGlobal=false;
                        int value=saveValue;
                        saveValue=null;
                        params.add(new FuncParam(funcFParamNode.ident.content,
                                funcFParamNode.leftSqareBrack.size(), value));
                    }
                }
                llvmir.append("define dso_local void " + "@" + funcDefNode.ident.content + "(");

                put(funcDefNode.ident.content, new FuncSymbol
                        ("@" + funcDefNode.ident.content, Objects.equals(funcDefNode.funcTypeNode.token.content, "void") ?
                                FuncSymbol.ReturnType.VOID : FuncSymbol.ReturnType.INT, params));
            }
            //int
            else {
                isInFuncWithNoParams = false;
                List<FuncParam> params = new ArrayList<>();
                int i = 0;
                for (FuncFParamNode funcFParamNode :
                        funcDefNode.funcFParamsNode.funcFParamNodes) {
                    i++;
                    if (funcFParamNode.leftSqareBrack.size() < 2) {
                        params.add(new FuncParam(funcFParamNode.ident.content,
                                funcFParamNode.leftSqareBrack.size()));
                    }
                    else {
                        isGlobal=true;
                        visitConstExp(funcFParamNode.constExpNodes.get(0));
                        isGlobal=false;
                        int value=saveValue;
                        saveValue=null;
                        params.add(new FuncParam(funcFParamNode.ident.content,
                                funcFParamNode.leftSqareBrack.size(), value));
                    }
                }
                llvmir.append("define dso_local i32 " + "@" + funcDefNode.ident.content + "(");

                put(funcDefNode.ident.content, new FuncSymbol
                        ("@" + funcDefNode.ident.content, Objects.equals(funcDefNode.funcTypeNode.token.content, "void") ?
                                FuncSymbol.ReturnType.VOID : FuncSymbol.ReturnType.INT, params));
            }


        }

        addSymbolTable(true, returnType);
        if (funcDefNode.funcFParamsNode != null) {
            visitfuncFParams(funcDefNode.funcFParamsNode);
            llvmir.delete(llvmir.length() - 2, llvmir.length());
        }
        llvmir.append(") {\n");
        //进入block了, regNum要++
        if (isInFunc) {
            regNum++;
        }
        if (isInFuncWithNoParams == false) {
            // 如果有参数, 必须先如下操作!
//            %3 = alloca i32, align 4
//            %4 = alloca i32, align 4
//            store i32 %0, i32* %3, align 4
//            store i32 %1, i32* %4, align 4

            Map<String, Symbol> param_params = symbolTables.get(symbolTables.size() - 1).first;
            FuncSymbol params= (FuncSymbol) symbolTables.get(0).first.get(funcDefNode.ident.content);

            // 遍历所有键
            int i=0;
            for (FuncParam entry : params.funcParams) {
                String key = entry.name;
                String originRegName  ;
                String curRegName = "* %" + regNum;
                ArraySymbol arraySymbol = (ArraySymbol) param_params.get(key);
                if (entry.dimension == 0) {
                    originRegName="%"+i;
                    llvmir.append("%" + regNum + " = alloca i32\n");
                    curRegName = "* %" + regNum;
                    llvmir.append("store i32 " + originRegName + ", i32" + curRegName + "\n");
                }
                else if (entry.dimension == 1) {
                    originRegName="*%"+i;
                    llvmir.append("%" + regNum + " = alloca i32*\n");
                    curRegName = "* * %" + regNum;
                    llvmir.append("store i32 " + originRegName + ", i32" + curRegName + "\n");
                }
                else if (entry.dimension == 2) {
                    originRegName="["+entry.size+" x i32] *%"+i;
                    llvmir.append("%" + regNum + " = alloca ["+entry.size+ " x i32]*\n");
                    curRegName ="["+entry.size+" x i32]* * %"+ regNum;
                    llvmir.append("store " + originRegName + ", " + curRegName + "\n");
                }


                arraySymbol.regiName = curRegName;

                regNum++;
                i++;
            }

        }

        visitBlock(funcDefNode.blockNode);
        //有的void函数没有返回值,
        List<BlockItemNode> blockItemNodes=funcDefNode.blockNode.blockItemNodes;
        if (blockItemNodes.size() == 0 ||
                blockItemNodes.get(blockItemNodes.size() - 1).stmtNode == null ||
                blockItemNodes.get(blockItemNodes.size() - 1).stmtNode.stmtType != StmtNode.StmtType.Return
        ) {
            if (symbolTables.get(symbolTables.size() - 1).third == FuncSymbol.ReturnType.VOID) {
                llvmir.append("ret void\n");
            }

        }
        llvmir.append("\n}\n");
        regNum = 0;

        isInFunc = false;
        removeSymbolTable();
    }

    private void visitfuncFParams(FuncFParamsNode funcFParamsNode) {
        //函数形参表   FuncFParams → FuncFParam { ',' FuncFParam }
        for (FuncFParamNode funcFParamNode :
                funcFParamsNode.funcFParamNodes) {
            visitFuncFParam(funcFParamNode);
        }
    }

    private void visitFuncFParam(FuncFParamNode funcFParamNode) {
        //函数形参    FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]  //   b k
        //          FuncFParam   → BType Ident ['[' ']' { '[' ConstExp ']' }]
        if (funcFParamNode.leftSqareBrack.size() == 0) {
            String name = funcFParamNode.ident.content;
            llvmir.append("i32 %" + regNum + ", ");
            put(name, new ArraySymbol(name, false, funcFParamNode.leftSqareBrack.size(), "%" + regNum, (String) null));
            regNum++;
        }
        else if(funcFParamNode.leftSqareBrack.size() == 1){
            String name = funcFParamNode.ident.content;
            llvmir.append("i32* %" + regNum + ", ");
            put(name, new ArraySymbol(name, false, funcFParamNode.leftSqareBrack.size(), "%" + regNum, (String) null));
            regNum++;
        }
        else if(funcFParamNode.leftSqareBrack.size() == 2){
            isInExpStack.push(true);
            isGlobal=true;
            visitConstExp(funcFParamNode.constExpNodes.get(0));
            isGlobal=false;
            isInExpStack.pop();
            String name = funcFParamNode.ident.content;
            llvmir.append("["+saveValue+" x i32] *%" + regNum + ", ");
            put(name, new ArraySymbol(name, false, funcFParamNode.leftSqareBrack.size(), "%" + regNum, (String) null));
            regNum++;
        }
    }


    private void visitDecl(DeclNode declNode) {
        //Decl         → ConstDecl | VarDecl
        if (declNode.constDeclNode != null) {
            visitConstDecl(declNode.constDeclNode);
        }
        else {
            visitVarDecl(declNode.varDeclNode);
        }
    }

    private void visitVarDecl(VarDeclNode varDeclNode) {
        //VarDecl      → BType VarDef { ',' VarDef } ';'
        for (VarDefNode varDefNode : varDeclNode.varDefNodes) {
            visitVarDef(varDefNode);
        }
    }

    private void visitVarDef(VarDefNode varDefNode) {
//        VarDef → Ident { '[' ConstExp ']' } | Ident { '[' ConstExp ']' } '=' InitVal
        if (varDefNode.constExpNodes.isEmpty()) {
            String ident = varDefNode.identToken.content;
            if (isGlobal) {
                if (varDefNode.initValNode == null) {
                    put(varDefNode.identToken.content,
                            new ArraySymbol(varDefNode.identToken.content,
                                    false, varDefNode.constExpNodes.size(),
                                    "* @" + ident, "0")
                    );

                    llvmir.append("@").append(ident).append(" = dso_local global i32 ").append("0\n");
                }
                else {

                    visitInitVal(varDefNode.initValNode);
                    put(varDefNode.identToken.content,
                            new ArraySymbol(varDefNode.identToken.content,
                                    false, varDefNode.constExpNodes.size(),
                                    "* @" + ident, saveValue.toString())
                    );

                    llvmir.append("@").append(ident).append(" = dso_local global i32 ").append(saveValue).append("\n");
                }
            }
            else {

                if (varDefNode.initValNode == null) {
                    put(varDefNode.identToken.content,
                            new ArraySymbol(varDefNode.identToken.content,
                                    false, varDefNode.constExpNodes.size(),
                                    "* %" + regNum, "0")
                    );

                    llvmir.append("%").append(regNum).append(" = alloca i32\n");
                    llvmir.append(findIndentation());
                    llvmir.append("store i32 ").append(0).append(", i32* ").append("%").append(regNum++).append("\n");
                }
                else {
                    visitInitVal(varDefNode.initValNode);
                    put(varDefNode.identToken.content,
                            new ArraySymbol(varDefNode.identToken.content,
                                    false, varDefNode.constExpNodes.size(),
                                    "* %" + regNum, (String) null)
                    );
                    llvmir.append(findIndentation());
                    llvmir.append("%").append(regNum).append(" = alloca i32\n");
                    llvmir.append(findIndentation());
                    int prevReg = regNum - 1;
                    llvmir.append("store i32 ").append("%" + prevReg).append(", i32* ").append("%").append(regNum++).append("\n");
                }
            }
        }
        else{
            isArray=true;
            String ident = varDefNode.identToken.content;
            if (isGlobal) {
                if (varDefNode.initValNode == null) {
                    List<Integer> shape = new ArrayList<>();

                    llvmir.append("@" + ident + " = dso_local global ");
                    for (int i = 0; i < varDefNode.constExpNodes.size(); i++) {
                        visitConstExp(varDefNode.constExpNodes.get(i));
                        llvmir.append("[" + saveValue + " x ");
                        shape.add(saveValue);
                        saveValue = null;
                    }
                    for (int i = 0; i < varDefNode.constExpNodes.size(); i++) {
                        if (i == 0) {
                            llvmir.append("i32] ");
                        }
                        else {
                            llvmir.append("] ");
                        }
                    }
                    llvmir.append(" zeroinitializer\n");
                    List<Integer> array1=new ArrayList<>();
                    List<List<Integer>> array2=new ArrayList<>();
                    if (varDefNode.constExpNodes.size() == 1) {
                        for (int i = 0; i < shape.get(0); i++) {
                            array1.add(0);
                        }
                        put(varDefNode.identToken.content, new ArraySymbol(
                                varDefNode.identToken.content, false,
                                varDefNode.constExpNodes.size(), "* @" + ident, array1
                        ));
                    }
                    else if (varDefNode.constExpNodes.size() == 2) {
                        for (int i = 0; i < shape.get(0); i++) {
                            array2.add(new ArrayList<>());
                            for (int j = 0; j < shape.get(1); j++) {
                                array2.get(i).add(0);
                            }
                        }
                        put(varDefNode.identToken.content, new ArraySymbol(
                                varDefNode.identToken.content, false,
                                varDefNode.constExpNodes.size(), "* @" + ident, array2
                        ));
                    }



                }
                else {
                    //int c[5][5]={{1,2,3,0,0},{1,2,3,4,5},{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0}};
                    //@c = dso_local global [5 x [5 x i32]] [[5 x i32] [i32 1, i32 2, i32 3, i32 0, i32 0], ...
                    List<Integer> shape = new ArrayList<>();

                    llvmir.append("@" + ident + " = dso_local global ");
                    for (int i = 0; i < varDefNode.constExpNodes.size(); i++) {
                        visitConstExp(varDefNode.constExpNodes.get(i));
                        llvmir.append("[" + saveValue + " x ");
                        shape.add(saveValue);
                        saveValue = null;
                    }
                    for (int i = 0; i < varDefNode.constExpNodes.size(); i++) {
                        if (i == 0) {
                            llvmir.append("i32] ");
                        }
                        else {
                            llvmir.append("] ");
                        }
                    }

                    List<InitValNode> con = varDefNode.initValNode.initValNodes;
                    if (shape.size() == 2) {
                        List<List<Integer>> array = new ArrayList<>();//存数组内容
                        for (InitValNode ccon : con) {
                            List<Integer> rowi = new ArrayList<>();
                            for (InitValNode cccon : ccon.initValNodes) {
                                visitExp(cccon.expNode);
                                rowi.add(saveValue);
                                saveValue = null;
                            }
                            array.add(rowi);
                        }
                        String llvmCode = toLLVM2d(array);
                        llvmir.append(llvmCode);
                        llvmir.append("\n");
                        put(varDefNode.identToken.content, new ArraySymbol(
                                varDefNode.identToken.content, true,
                                varDefNode.constExpNodes.size(), "* @" + ident, array
                        ));
                    }
                    else if (shape.size() == 1) {
                        List<Integer> array = new ArrayList<>();//存数组内容
                        for (InitValNode ccon : con) {
                            visitExp(ccon.expNode);
                            array.add(saveValue);
                            saveValue = null;
                        }
                        String llvmCode = toLLVM1d(array);
                        llvmir.append(llvmCode);
                        llvmir.append("\n");
                        put(varDefNode.identToken.content, new ArraySymbol(
                                varDefNode.identToken.content, true,
                                varDefNode.constExpNodes.size(), "* @" + ident, array
                        ));
                    }
                }
            }
            else {
                //int c[2][3]={{1,2,3},{0,0,0}};
                //int c[2][3]; 只分配空间, 不赋值

                List<Integer> shape = new ArrayList<>();

                StringBuilder array_size = new StringBuilder();//记录形状, [2 x [3 x i32]]这种
                for (int i = 0; i < varDefNode.constExpNodes.size(); i++) {
                    visitConstExp(varDefNode.constExpNodes.get(i));
                    array_size.append("[" + saveValue + " x ");
                    shape.add(saveValue);
                    saveValue = null;
                }
                int arrayRegNum=regNum;
                llvmir.append("%" + regNum++ + " = alloca ");

                for (int i = 0; i < varDefNode.constExpNodes.size(); i++) {
                    if (i == 0) {
                        array_size.append("i32] ");
                    }
                    else {
                        array_size.append("] ");
                    }
                }
                llvmir.append(array_size);
                llvmir.append("\n");
                if (varDefNode.initValNode != null) {
                    List<InitValNode> con = varDefNode.initValNode.initValNodes;
                    if (shape.size() == 2) {
                        List<List<Integer>> array = new ArrayList<>();//存数组内容
                        for (InitValNode ccon : con) {
                            List<Integer> rowi = new ArrayList<>();
                            for (InitValNode cccon : ccon.initValNodes) {
                                visitExp(cccon.expNode);
                                rowi.add(saveValue);
                                saveValue = null;
                            }
                            array.add(rowi);
                        }
                        int i = 0, j = 0;
                        for (List<Integer> arrayRow : array) {
                            j = 0;
                            for (Integer arrayElem : arrayRow) {
                                llvmir.append("%" + regNum + " = getelementptr " + array_size +
                                        ", " + array_size + "*%" + arrayRegNum +
                                        ", i32 0, i32 " + i + ", i32 " + j + "\n");
                                llvmir.append("store i32 " + arrayElem + ", i32* %" + regNum + "\n");
                                regNum++;
                                j++;

                            }
                            i++;
                        }
                        //TODO:改这里!!!!
                        String haha = extractInnerBracketContent(String.valueOf(array_size));
//                        if (isInFunc) {
//                            put(varDefNode.identToken.content, new ArraySymbol(
//                                    varDefNode.identToken.content, false,
//                                    varDefNode.constExpNodes.size(), haha+"* %" + arrayRegNum, array
//                            ));
//                        }
//                        else {
                            put(varDefNode.identToken.content, new ArraySymbol(
                                    varDefNode.identToken.content, false,
                                    varDefNode.constExpNodes.size(), "* %" + arrayRegNum, array
                            ));
//                        }

                    }
                    else if (shape.size() == 1) {
                        List<Integer> array = new ArrayList<>();//存数组内容
                        for (InitValNode ccon : con) {
                            visitExp(ccon.expNode);
                            array.add(saveValue);
                            saveValue = null;
                        }
                        int i = 0;
                        for (Integer integer : array) {
                            llvmir.append("%" + regNum + " = getelementptr " + array_size +
                                    ", " + array_size + "*%" + arrayRegNum +
                                    ", i32 0, i32 " + i + "\n");
                            llvmir.append("store i32 " + integer + ", i32* %" + regNum + "\n");
                            regNum++;
                            i++;
                        }
//
                        llvmir.append("\n");


                            put(varDefNode.identToken.content, new ArraySymbol(
                                    varDefNode.identToken.content, false,
                                    varDefNode.constExpNodes.size(), "* %" + arrayRegNum, array
                            ));

                    }
                }
                else {
                    //未附初值的数组也要进符号表
                    List<Integer> array1=new ArrayList<>();
                    List<List<Integer>> array2=new ArrayList<>();
                    if (varDefNode.constExpNodes.size() == 1) {
                        for (int i = 0; i < shape.get(0); i++) {
                            array1.add(0);
                        }

                            put(varDefNode.identToken.content, new ArraySymbol(
                                    varDefNode.identToken.content, false,
                                    varDefNode.constExpNodes.size(), "* %" + arrayRegNum, array1
                            ));

                    }
                    else if (varDefNode.constExpNodes.size() == 2) {
                        for (int i = 0; i < shape.get(0); i++) {
                            array2.add(new ArrayList<>());
                            for (int j = 0; j < shape.get(1); j++) {
                                array2.get(i).add(0);
                            }
                        }

                        String haha = extractInnerBracketContent(String.valueOf(array_size));
//                        if (isInFunc) {
//                            put(varDefNode.identToken.content, new ArraySymbol(
//                                    varDefNode.identToken.content, false,
//                                    varDefNode.constExpNodes.size(), haha+"* %" + arrayRegNum, array2
//                            ));
//                        }
//                        else {
                            put(varDefNode.identToken.content, new ArraySymbol(
                                    varDefNode.identToken.content, false,
                                    varDefNode.constExpNodes.size(), "* %" + arrayRegNum, array2
                            ));
//                        }
                    }
                }
            }
        }
        isArray=false;

    }
    private  String extractInnerBracketContent(String input) {
        //[2 x [2 x i32]]
        int flag=0;
        // 初始化最后匹配的字符串
        StringBuilder lastMatch = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == '[') {
                flag++;
            }
            if (flag == 2) {
                lastMatch.append(input.charAt(i));
                if (input.charAt(i) == ']') {
                    break;
                }
            }


        }

        return lastMatch.toString();
    }

    private void visitInitVal(InitValNode initValNode) {
        //InitVal      → Exp
        visitExp(initValNode.expNode);
    }

    private void visitConstDecl(ConstDeclNode constDeclNode) {
        //ConstDecl    → 'const' BType ConstDef { ',' ConstDef } ';'
        for (ConstDefNode constDef : constDeclNode.constDefNodes) {
            visitConstDef(constDef);
        }
    }

    public void put(String ident, Symbol symbol) {//加入符号表
        symbolTables.get(symbolTables.size() - 1).first.put(ident, symbol);
    }

    private void visitConstDef(ConstDefNode constDef) {
        //ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
        if (constDef.constExpNodes.size() == 0) {
            //ConstDef     → Ident  '=' ConstInitVal
            String ident = constDef.identToken.content;

            //const int a=5;
            //@a = dso_local constant i32 5
            if (isGlobal) {

                visitConstInitVal(constDef.constInitValNode);
                put(constDef.identToken.content, new ArraySymbol(
                        constDef.identToken.content, true,
                        constDef.constExpNodes.size(), "* @" + ident, saveValue.toString()
                ));


                llvmir.append("@").append(ident).append(" = dso_local constant i32 ").append(saveValue).append("\n");
            }
            else {

                visitConstInitVal(constDef.constInitValNode);

                put(constDef.identToken.content, new ArraySymbol(
                        constDef.identToken.content, true,
                        constDef.constExpNodes.size(), "* %" + regNum, (String) null
                ));


                llvmir.append(findIndentation());
                llvmir.append("%").append(regNum).append(" = alloca i32\n");
                llvmir.append(findIndentation());
                int prevReg = regNum - 1;
                llvmir.append("store i32 ").append("%" + prevReg).append(", i32* ").append("%").append(regNum++).append("\n");
            }
        }
        else{
            isArray=true;
            String ident = constDef.identToken.content;
            if (isGlobal) {
                //int c[5][5]={{1,2,3,0,0},{1,2,3,4,5},{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0}};
                //@c = dso_local global [5 x [5 x i32]] [[5 x i32] [i32 1, i32 2, i32 3, i32 0, i32 0], ...
                List<Integer> shape=new ArrayList<>();

                llvmir.append("@"+ident+" = dso_local constant ");
                for (int i = 0; i < constDef.constExpNodes.size(); i++) {
                    visitConstExp(constDef.constExpNodes.get(i));
                    llvmir.append("[" + saveValue + " x ");
                    shape.add(saveValue);
                    saveValue=null;
                }
                for (int i = 0; i < constDef.constExpNodes.size(); i++){
                    if (i == 0) {
                        llvmir.append("i32] ");
                    }
                    else{
                        llvmir.append("] ");
                    }
                }

                List<ConstInitValNode> con= constDef.constInitValNode.constInitValNodes;
                if (shape.size() == 2) {
                    List<List<Integer>> array=new ArrayList<>();//存数组内容
                    for (ConstInitValNode ccon : con) {
                        List<Integer> rowi = new ArrayList<>();
                        for (ConstInitValNode cccon : ccon.constInitValNodes) {
                            visitConstExp(cccon.constExpNode);
                            rowi.add(saveValue);
                            saveValue=null;
                        }
                        array.add(rowi);
                    }
                    String llvmCode=toLLVM2d(array);
                    llvmir.append(llvmCode);
                    llvmir.append("\n");
                    put(constDef.identToken.content, new ArraySymbol(
                            constDef.identToken.content, true,
                            constDef.constExpNodes.size(), "* @" + ident, array
                    ));
                }
                else if (shape.size() == 1) {
                    List<Integer> array=new ArrayList<>();//存数组内容
                    for (ConstInitValNode ccon : con) {
                        visitConstExp(ccon.constExpNode);
                        array.add(saveValue);
                        saveValue=null;
                    }
                    String llvmCode = toLLVM1d(array);
                    llvmir.append(llvmCode);
                    llvmir.append("\n");
                    put(constDef.identToken.content, new ArraySymbol(
                            constDef.identToken.content, true,
                            constDef.constExpNodes.size(), "* @" + ident, array
                    ));
                }
            }
            else {
                //int c[2][3]={{1,2,3},{0,0,0}};
                //int c[2][3]; 只分配空间, 不赋值
                List<Integer> shape = new ArrayList<>();
                int arrayRegNum=regNum;

                StringBuilder array_size = new StringBuilder();//记录形状, [2 x [3 x i32]]这种
                for (int i = 0; i < constDef.constExpNodes.size(); i++) {
                    visitConstExp(constDef.constExpNodes.get(i));
                    array_size.append("[" + saveValue + " x ");
                    shape.add(saveValue);
                    saveValue = null;
                }
                llvmir.append("%" + regNum++ + " = alloca ");
                for (int i = 0; i < constDef.constExpNodes.size(); i++) {
                    if (i == 0) {
                        array_size.append("i32] ");
                    }
                    else {
                        array_size.append("] ");
                    }
                }
                llvmir.append(array_size);
                llvmir.append("\n");
                if (true) {
                    List<ConstInitValNode> con = constDef.constInitValNode.constInitValNodes;
                    if (shape.size() == 2) {
                        List<List<Integer>> array = new ArrayList<>();//存数组内容
                        for (ConstInitValNode ccon : con) {
                            List<Integer> rowi = new ArrayList<>();
                            for (ConstInitValNode cccon : ccon.constInitValNodes) {
                                visitConstExp(cccon.constExpNode);
                                rowi.add(saveValue);
                                saveValue = null;
                            }
                            array.add(rowi);
                        }
                        int i=0,j=0;
                        for (List<Integer> arrayRow : array) {
                            j=0;
                            for (Integer arrayElem : arrayRow) {
                                llvmir.append("%" + regNum + " = getelementptr " + array_size +
                                        ", " + array_size + "*%" + arrayRegNum +
                                        ", i32 0, i32 " + i + ", i32 " + j + "\n");
                                llvmir.append("store i32 " + arrayElem + ", i32* %" + regNum + "\n");
                                regNum++;
                                j++;

                            }
                            i++;
                        }
                        put(constDef.identToken.content, new ArraySymbol(
                                constDef.identToken.content, true,
                                constDef.constExpNodes.size(), "* %" + arrayRegNum, array
                        ));
                    }
                    else if (shape.size() == 1) {
                        List<Integer> array = new ArrayList<>();//存数组内容
                        for (ConstInitValNode ccon : con) {
                            visitConstExp(ccon.constExpNode);
                            array.add(saveValue);
                            saveValue = null;
                        }
                        int i=0;
                        for (Integer integer : array) {
                            llvmir.append("%" + regNum + " = getelementptr " + array_size +
                                    ", " + array_size + "*%" + arrayRegNum +
                                    ", i32 0, i32 " + i  + "\n");
                            llvmir.append("store i32 " + integer + ", i32* %" + regNum + "\n");
                            regNum++;
                            i++;
                        }
                        llvmir.append("\n");
                        put(constDef.identToken.content, new ArraySymbol(
                                constDef.identToken.content, true,
                                constDef.constExpNodes.size(), "* %" + arrayRegNum, array
                        ));
                    }
                }
            }
        }
        isArray=false;
    }
    private String toLLVM1d(List<Integer> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append("i32 ").append(list.get(i));
            if (i < list.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private String toLLVM2d(List<List<Integer>> array) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        int columLenth = array.get(0).size();
        for (List<Integer> row : array) {
            // 检测是否为全零行
            boolean isZeroRow = true;
            for (int value : row) {
                if (value != 0) {
                    isZeroRow = false;
                    break;
                }
            }

            sb.append("["+columLenth+" x i32] ");
            if (isZeroRow) {
                sb.append("zeroinitializer");
            } else {
                sb.append("[");
                for (int i = 0; i < row.size(); i++) {
                    sb.append("i32 ").append(row.get(i));
                    if (i < row.size() - 1) {
                        sb.append(", ");
                    }
                }
                sb.append("]");
            }
            sb.append(", ");
        }

        // 移除最后一个逗号和空格
        sb.setLength(sb.length() - 2);
        sb.append("]");
        return sb.toString();
    }


    private void visitConstInitVal(ConstInitValNode constInitValNode) {
        //ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
        //ConstInitVal → ConstExp
        if (constInitValNode.constExpNode != null) {
            visitConstExp(constInitValNode.constExpNode);
        }

    }

    private void visitConstExp(ConstExpNode constExpNode) {
        //ConstExp     → AddExp
        isConst = true;
        saveOp = null;
        saveValue = null;
        tmpValue = null;
        visitAdd(constExpNode.addExpNode);
    }

    public void visitMainFuncDef(MainFuncDefNode mainFuncDefNode) {
        //MainFuncDef → 'int' 'main' '(' ')' Block
        llvmir.append("define dso_local i32 @main() ");
        llvmir.append("{\n");
        addSymbolTable(true, FuncSymbol.ReturnType.INT);
        visitBlock(mainFuncDefNode.blockNode);
        removeSymbolTable();
        llvmir.append("\n}\n");
    }


    private void visitBlock(BlockNode blockNode) {
        //Block       → '{' { BlockItem } '}'
        int i = 0;
        for (BlockItemNode blockItemNode : blockNode.blockItemNodes) {
            i++;
            saveValue = null;
            saveOp = null;
            tmpValue = null;
            llvmir.append(findIndentation());

            if ((isContinue&&checkIfInIf())
                    ||(isElseContinue&&checkIfInElse())) {
                return;
            }
            if ((isBreak&&checkIfInIf())
                    ||(isElseBreak&&checkIfInElse())) {
                return;
            }
            visitBlockItem(blockItemNode);
        }
        //如果最后一句没有return 要加上!
//        if (symbolTables.get(symbolTables.size() - 1).second) {
//            if (blockNode.blockItemNodes.size() > 1) {
//                if (blockNode.blockItemNodes.get(i - 1).stmtNode.returnToken == null) {
//                    llvmir.append("ret void\n");
//                }
//            }
//            else if (blockNode.blockItemNodes.size() == 0) {
//                llvmir.append("ret void\n");
//            }
//        }


    }

    private boolean checkIfInElse() {
        for (int i = symbolTables.size() - 1; i >= 0; i--) {
            if (symbolTables.get(i).third == FuncSymbol.ReturnType.ELSE) {
                return true;
            }
        }
        return false;
    }

    private boolean checkIfInIf() {
        for (int i = symbolTables.size() - 1; i >= 0; i--) {
            if (symbolTables.get(i).third == FuncSymbol.ReturnType.IF) {
                return true;
            }
        }
        return false;
    }

    private void visitBlockItem(BlockItemNode blockItemNode) {
        //BlockItem → Decl | Stmt
        isReturn=false;
        if (blockItemNode.declNode != null) {
            visitDecl(blockItemNode.declNode);
        }
        else {
            visitStmt(blockItemNode.stmtNode);
        }

    }

    private void visitStmt(StmtNode stmtNode) {
        //Stmt       → LVal '=' Exp ';'
        //           | [Exp] ';'
        //           | 'return' Exp ';'
        // | '      | Block'
        //LVal '=' 'getint''('')'';'
        //     | 'printf''('FormatString{','Exp}')'';'
        if (stmtNode.stmtType == StmtNode.StmtType.Return) {

            isReturn=true;
            int isVoid = 1;
            if (stmtNode.expNode != null) {
                isVoid = 0;
                visitExp(stmtNode.expNode);
            }
            if (isVoid == 1) {
                llvmir.append("ret void\n");
            }
            else {
                llvmir.append("ret i32 ");
                int curReg = regNum - 1;
                llvmir.append("%" + curReg).append("\n");
                saveValue = null;//使用了要归零
            }
            int flag=0;
            if (checkIfInElse()||
                    checkIfInIf()) {
                putToFor("return", new Symbol());
                llvmir.append(regNum++ + ":\n");
            }
        }
        else if (stmtNode.stmtType == StmtNode.StmtType.LValAssignExp) {
            //TODO: 有问题!!!?

            // int a=1000;
            // int main(){
            //        a=getint(); 此时全局变量也不确定了!!!
            //        int b=a+3;
            //        return 0;
            // Solution:如果是在函数体内,就取消全部变量的地位!!!
            // 可以在{Decl}结束之后取消所有全局变量的地位, 符号表和isGlobal...
            // 不要直接写, 先在这里调试一下, testfile.txt貌似就有问题!
            // 调试一下这个函数
            //}
            if (stmtNode.lValNode.expNodes.size() == 0) {
                visitExp(stmtNode.expNode);
                changeValueFromVar(stmtNode.lValNode.ident.content, null);
                int prevReg = regNum - 1;
                llvmir.append("store i32 ").append("%" + prevReg + ", i32").append(getRegiNameFromVar(stmtNode.lValNode.ident.content)).append("\n");
                saveValue = null;
            }
            else {// 处理数组
                //c[x+y][1]=y;
                if (!isInFunc||!isNotInFuncStack.empty()) {
                    visitExp(stmtNode.expNode);
                    int exp_reg = regNum - 1;
                    visitLval(stmtNode.lValNode);
                    int lval_reg = regNum - 1;
                    llvmir.append("store i32 ").append("%" + exp_reg + ", i32").append("* %" + lval_reg).append("\n");
                    saveValue = null;
                }
                else {
                    ArraySymbol arraySymbol = getArraySymbolFromFunc(stmtNode.lValNode.ident.content);
                    if (arraySymbol == null) {
                        //全局数组
                        visitExp(stmtNode.expNode);
                        int exp_reg = regNum - 1;
                        isNotInFuncStack.push(true);
                        visitLval(stmtNode.lValNode);
                        if (!isNotInFuncStack.empty()) {
                            isNotInFuncStack.pop();
                        }

                        int lval_reg = regNum - 1;
                        llvmir.append("store i32 ").append("%" + exp_reg + ", i32").append("* %" + lval_reg).append("\n");
                        saveValue = null;
                    }
                    else {
                        if (stmtNode.lValNode.expNodes.size() == 1) {
                            visitExp(stmtNode.lValNode.expNodes.get(0));
                            int exp_reg = regNum - 1;
                            llvmir.append("%" + regNum + " = load i32*, i32" + arraySymbol.regiName + "\n");
                            int prev_reg=regNum;
                            regNum++;
                            llvmir.append("%" + regNum +" = getelementptr i32, i32* %"+prev_reg+", i32 %"+exp_reg+"\n");
                            prev_reg=regNum;
                            regNum++;
                            visitExp(stmtNode.expNode);
                            exp_reg=regNum-1;
                            llvmir.append("store i32 %" + exp_reg + ", i32* %" + prev_reg + "\n");
                        }
                        else if (stmtNode.lValNode.expNodes.size() == 2) {
//                        %7 = load [2 x i32]*, [2 x i32]** %5, align 8
//                        %8 = getelementptr inbounds [2 x i32], [2 x i32]* %7, i64 0
//                        %9 = getelementptr inbounds [2 x i32], [2 x i32]* %8, i64 0, i64 0
                            visitExp(stmtNode.lValNode.expNodes.get(0));
                            int exp_reg = regNum - 1;
                            visitExp(stmtNode.lValNode.expNodes.get(1));
                            int exp_reg2 = regNum - 1;
                            String size=doRegex(arraySymbol.regiName);
                            llvmir.append("%" + regNum + " = load " + size + "*, " + arraySymbol.regiName + "\n");
                            int prev_reg=regNum;
                            regNum++;
                            llvmir.append("%" + regNum + " = getelementptr "+size +", "+size+"* %"+prev_reg+", i32 %"+exp_reg+"\n");
                            prev_reg=regNum;
                            regNum++;
                            llvmir.append("%" + regNum + " = getelementptr "+size +", "+size+"* %"+prev_reg+", i32 0, i32 %"+exp_reg2+"\n");
                            prev_reg=regNum;
                            regNum++;
                            visitExp(stmtNode.expNode);
                            exp_reg=regNum-1;
                            llvmir.append("store i32 %" + exp_reg + ", i32* %" + prev_reg + "\n");

                        }
                    }


                }


            }

        }
        else if (stmtNode.stmtType == StmtNode.StmtType.Block) {
            visitBlock(stmtNode.blockNode);
        }
        else if (stmtNode.stmtType == StmtNode.StmtType.LValGetint) {
            llvmir.append("%").append(regNum).append(" = call i32 @getint()\n");
            String regName = getRegiNameFromVar(stmtNode.lValNode.ident.content);
            llvmir.append(findIndentation());
            llvmir.append("store i32 ").append("%" + regNum++).append(", i32").append(regName).append("\n");

        }
        else if (stmtNode.stmtType == StmtNode.StmtType.Printf) {
            //     | 'printf''('FormatString{','Exp}')'';'
            String[] formatStrings = stmtNode.formatString.getContent().replace("\\n", "\n").replace("\"", "").split("%d");
            List<Integer> args = new ArrayList<>();
            List<String> args2 = new ArrayList<>();
            int i = 0, j = 0;
            for (ExpNode expNode : stmtNode.expNodes) {
                saveValue = null;
                tmpValue = null;
                visitExp(expNode);
                args.add(saveValue);
                args2.add(tmpValue);
                saveValue = null;
                tmpValue = null;
                j++;
            }
            for (String formatString : formatStrings) {
                for (char c : formatString.toCharArray()) {
                    llvmir.append("call void @putch(i32 ").append((int) c).append(")\n");
                    llvmir.append(findIndentation());
                }
                if (!args2.isEmpty()) {
                    int num = regNum - j;
                    llvmir.append("call void @putint(i32 ").append(args2.get(0)).append(")\n");
                    llvmir.append(findIndentation());
                    args2.remove(0);
                    j--;
                }
            }
            if (!args2.isEmpty()) {
                int num = regNum - j;
                llvmir.append("call void @putint(i32 ").append(args2.get(0)).append(")\n");
                llvmir.append(findIndentation());
                args2.remove(0);
                j--;
            }
        }
        else if (stmtNode.stmtType == StmtNode.StmtType.Exp) {
            if (stmtNode.expNode != null) {
                visitExp(stmtNode.expNode);
            }
        }
        else if (stmtNode.stmtType == StmtNode.StmtType.If) {
            //Stmt    → 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
            if (stmtNode.elseToken == null) {


                visitCond(stmtNode.condNode);

                int prevReg=regNum-1;
                if (!regHash.containsKey("%" + prevReg)) {
                    llvmir.append("%" + regNum + " = icmp ne i32 " + "%" + prevReg + ", 0\n");
                    regHash.put("%" + regNum, 1);
                    regNum++;
                    prevReg=regNum-1;
                }
                llvmir.append("br i1 ").append("%").append(prevReg + ",  label ")
                        .append("%" + regNum).append(", label ").
                        append("不确定" + "\n\n" + regNum++ + ":\n");

                addSymbolTable(false, FuncSymbol.ReturnType.IF);
                visitStmt(stmtNode.stmtNodes.get(0));

                if (symbolTables.get(symbolTables.size()-1).first.containsKey("return")) {
                    isReturn=false;
//                    llvmir.append("\n\n" + regNum + ":\n");
                }
                else{
                    if (symbolTables.get(symbolTables.size()-1).first.containsKey("break")) {
//                        llvmir.append("\n\n" + regNum + ":\n");
                    }
                    else{
                        if (symbolTables.get(symbolTables.size()-1).first.containsKey("continue")) {
//                            llvmir.append("\n\n" + regNum + ":\n");
                        }
                        else{
                            llvmir.append("br label %" + regNum + "\n\n" + regNum++ + ":\n");
                        }
                    }
                }



                int prev=regNum-1;
                String huitian="%" + prev;
                int index = llvmir.lastIndexOf("不确定");
                if (index != -1) {
                    llvmir.replace(index, index + "不确定".length(), huitian);
                }
                removeSymbolTable();
            }
            else{
                visitCond(stmtNode.condNode);
                int prevReg=regNum-1;
                if (!regHash.containsKey("%" + prevReg)) {
                    llvmir.append("%" + regNum + " = icmp ne i32 " + "%" + prevReg + ", 0\n");
                    regHash.put("%" + regNum, 1);
                    regNum++;
                    prevReg=regNum-1;
                }
                llvmir.append("br i1 ").append("%").append(prevReg + ",  label ")
                        .append("%" + regNum).append(", label ").
                        append("跳else" + "\n\n" + regNum++ + ":\n");
                //上面的不确定是else的位置
                addSymbolTable(false, FuncSymbol.ReturnType.IF);
                visitStmt(stmtNode.stmtNodes.get(0));
                int flag=0;
                // if结束,跳出ifStmt!
                if (symbolTables.get(symbolTables.size()-1).first.containsKey("return")) {
                    isReturn=false;
//                    llvmir.append("\n\n" + regNum + ":\n");
                }
                else{
                    if (symbolTables.get(symbolTables.size()-1).first.containsKey("break")) {
//                        llvmir.append("\n\n" + regNum + ":\n");
                    }
                    else{
                        if (symbolTables.get(symbolTables.size()-1).first.containsKey("continue")) {
//                            llvmir.append("\n\n" + regNum + ":\n");
                        }
                        else{

                            llvmir.append("br label 跳出if" + "\n\n" + regNum++ + ":\n");
                        }
                    }
                }
                removeSymbolTable();

                //开始else, else确定了,开始回填
                int prev=regNum-1;

                String huitian="%" + prev;;
                int index = llvmir.lastIndexOf("跳else");
                if (index != -1) {
                    llvmir.replace(index, index + "跳else".length(), huitian);
                }
                //regNum++;
                addSymbolTable(false, FuncSymbol.ReturnType.ELSE);
                visitStmt(stmtNode.stmtNodes.get(1));

                //else结束!
                if (symbolTables.get(symbolTables.size()-1).first.containsKey("return")) {
                    isReturn=false;
//                    llvmir.append("\n\n" + regNum + ":\n");
                }
                else{
                    if (symbolTables.get(symbolTables.size()-1).first.containsKey("break")) {
//                        llvmir.append("\n\n" + regNum + ":\n");
                    }
                    else{
                        if (symbolTables.get(symbolTables.size()-1).first.containsKey("continue")) {
//                            llvmir.append("\n\n" + regNum + ":\n");
                        }
                        else{
                            llvmir.append("br label %" + regNum + "\n\n" + regNum++ + ":\n");
                        }
                    }
                }


                prev=regNum-1;
                huitian="%" + prev;

                if (!isContinue||isElseContinue) {
                    index = llvmir.lastIndexOf("跳出if");
                    if (index != -1) {
                        llvmir.replace(index, index + "跳出if".length(), huitian);
                    }
                }
                removeSymbolTable();

            }
        }
        else if (stmtNode.stmtType == StmtNode.StmtType.For) {
//            Stmt    → 'for' '(' [ForStmt1] ';' [Cond] ';' [ForStmt2] ')' Stmt
//                    | 'break' ';'
//                    | 'continue' ';'
            if (stmtNode.forStmtNodes.get(0) != null) {
                visitForStmt(stmtNode.forStmtNodes.get(0));
            }
            String condLable="%" + regNum;
            llvmir.append("br label " + "%" + regNum + "\n\n" + regNum + ":\n");
            regNum++;

            if (stmtNode.condNode != null) {
                visitCond(stmtNode.condNode);
                int prevReg=regNum-1;
                String stmtLable="%"+regNum;
                if (!regHash.containsKey("%" + prevReg)) {
                    llvmir.append("%" + regNum + " = icmp ne i32 " + "%" + prevReg + ", 0\n");
                    regHash.put("%" + regNum, 1);
                    regNum++;
                    prevReg=regNum-1;
                }
                llvmir.append("br i1 ").append("%").append(prevReg + ",  label ")
                        .append("%" + regNum).append(", label ").
                        append("不确定" + "\n\n" + regNum++ + ":\n");
            }



            addSymbolTable(false, FuncSymbol.ReturnType.FOR);
            visitStmt(stmtNode.stmtNode);


            int prev=regNum-1;
            String for2Lable="%"+prev;
//            if (isContinue) {
//                isContinue=false;
//                isElseContinue=false;
//                int index = llvmir.lastIndexOf("不确跳for2");
//                if (index!=-1) {
//                    llvmir.replace(index, index + "不确跳for2".length(), for2Lable);
//                }
//            }
            if (symbolTables.get(symbolTables.size()-1).first.containsKey("break")||
                    symbolTables.get(symbolTables.size()-1).first.containsKey("continue")||
                    symbolTables.get(symbolTables.size()-1).first.containsKey("return")) {

            }
            else {
                llvmir.append("br label " + "%" + regNum + "\n\n" + regNum + ":\n");
                regNum++;
            }
            removeSymbolTable();
            if (stmtNode.forStmtNodes.get(1) != null) {
                visitForStmt(stmtNode.forStmtNodes.get(1));
            }
            llvmir.append("br label " + condLable+"\n");
            llvmir.append("\n\n" + regNum + ":\n");
            String nextBlockLable="%"+regNum;
            int index = llvmir.lastIndexOf("不确定");
            if (index != -1) {
                llvmir.replace(index, index + "不确定".length(), nextBlockLable);
            }
//            if (isBreak) {
//                isBreak=false;
//                isElseBreak=false;
//                index = llvmir.lastIndexOf("不确跳出");
//                if (index != -1) {
//                    llvmir.replace(index, index + "不确跳出".length(), nextBlockLable);
//                }
//            }
            regNum++;
            while (true) {
                index = llvmir.lastIndexOf("不确跳for2");
                if (index != -1) {
                    llvmir.replace(index, index + "不确跳for2".length(), for2Lable);
                }
                else {
                    break;
                }
            }
            while (true) {
                index = llvmir.lastIndexOf("不确跳出");
                if (index != -1) {
                    llvmir.replace(index, index + "不确跳出".length(), nextBlockLable);
                }
                else {
                    break;
                }
            }
            isContinue=false;
            isBreak=false;

        }

        else if (stmtNode.stmtType == StmtNode.StmtType.Continue) {
            putToFor("continue", new Symbol());
            llvmir.append("br label 不确跳for2\n\n" + regNum++ + ":\n");
            //isContinue=true;
            if (symbolTables.get(symbolTables.size() - 1).third == FuncSymbol.ReturnType.ELSE) {
                isElseContinue=true;
            }

        }
        else if (stmtNode.stmtType == StmtNode.StmtType.Break) {
            putToFor("break", new Symbol());
            llvmir.append("br label 不确跳出\n\n" + regNum++ + ":\n");
            //isBreak=true;
            if (symbolTables.get(symbolTables.size() - 1).third == FuncSymbol.ReturnType.ELSE) {
                isElseBreak=true;
            }
        }

    }

    private void putToFor(String aBreak, Symbol symbol) {//将break这些加到直接外层的for的符号表里面
        for (int i = symbolTables.size() - 1; i >= 0; i--) {
            if (symbolTables.get(i).third == FuncSymbol.ReturnType.FOR) {
                symbolTables.get(i).first.put(aBreak, symbol);
                return;
            }
        }
    }

    private boolean checkLastLineIsBr() {
        // 将StringBuilder内容转换为字符串，然后按行分割
        String[] lines = llvmir.toString().split("\n");

        // 从后向前遍历查找第一个非空白行
        for (int i = lines.length - 1; i >= 0; i--) {
            String line = lines[i].trim();

            // 检查这一行是否非空
            if (!line.isEmpty()) {
                // 检查这一行是否以"br"开头
                if (line.startsWith("br")) {
                    // 如果以"br"开头，不执行任何操作
                    return true;
                }

            }
        }
        return false;
    }

    private String doRegex(String regiName) {
        String str = regiName;
        int starIndex = str.indexOf('*'); // 找到第一个星号的位置
        if (starIndex != -1) {
            String beforeStar = str.substring(0, starIndex); // 获取星号之前的字符串
            return beforeStar;
        }
        return null;
    }

    private void visitForStmt(ForStmtNode forStmtNode) {
//        ForStmt → LVal '=' Exp
        visitExp(forStmtNode.expNode);
        changeValueFromVar(forStmtNode.lValNode.ident.content, null);
        int prevReg = regNum - 1;
        llvmir.append("store i32 ").append("%" + prevReg + ", i32").append(getRegiNameFromVar(forStmtNode.lValNode.ident.content)).append("\n");
        saveValue = null;
    }

    private void visitCond(CondNode condNode) {
        //Cond    → LOrExp
        saveOp = null;
        saveValue = null;
        tmpValue = null;
        isInExpStack.push(true);
        visitLOrExp(condNode.lOrExpNode);
        isInExpStack.pop();
    }

    private void visitLOrExp(LOrExpNode lOrExpNode) {
        //LOrExp → LAndExp | LAndExp '||'   LOrExp #

        String leftValue = tmpValue;
        LOrExpNode current=lOrExpNode;

        Integer leftAns=tmpAns;//记录是否为1
        String llvmOp = saveOp;
        saveOp = null;
        tmpValue = null;
        if (lOrExpNode != null && lOrExpNode.lAndExpNode != null) {
            LAndExpNode andNode = lOrExpNode.lAndExpNode;
            if (andNode.eqExpNode != null) {
                EqExpNode eqNode = andNode.eqExpNode;
                if (eqNode.relExpNode != null) {
                    RelExpNode relNode = eqNode.relExpNode;
                    if (relNode.addExpNode != null) {
                        AddExpNode addNode = relNode.addExpNode;
                        if (addNode.mulExpNode != null) {
                            MulExpNode mulNode = addNode.mulExpNode;
                            if (mulNode.unaryExpNode != null) {
                                UnaryExpNode unaryNode = mulNode.unaryExpNode;
                                if (unaryNode.primaryExpNode != null) {
                                    PrimaryExpNode primaryNode = unaryNode.primaryExpNode;
                                    if (primaryNode.numberNode != null) {
                                        NumberNode numberNode = primaryNode.numberNode;
                                        if (numberNode.getToken() != null && numberNode.getToken().content != null) {
                                            try {
                                                leftAns = Integer.parseInt(numberNode.getToken().content);
                                            } catch (NumberFormatException e) {
                                                leftAns=null;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        visitLAndExp(lOrExpNode.lAndExpNode);
        String rightValue = tmpValue;
        if (leftAns != null) {
            if (leftAns == 1) {
                //对于连或来说，只要其中一个LOrExp或最后一个LAndExp为1，即可直接跳转Stmt1。
                tmpAns=null;
                return;
            }
        }

        if (leftValue != null) {
            if (leftValue.matches("^-?\\d+$")) {

            }
            else {
                String haha = getValueFromRegiName(leftValue);
                if (haha != null) {
                    leftValue = haha;
                }
                else {
                    // 指针才需要另开变量赋值!
                    if (leftValue.charAt(0) == '*') {
                        llvmir.append("%").append(regNum).append(" = ").append("load i32, i32").append(leftValue).append("\n");
                        leftValue = "%" + regNum;
                        regNum++;
                    }

                }
            }
            if (rightValue.matches("^-?\\d+$")) {

            }
            else {
                String ahah = getValueFromRegiName(rightValue);

                if (ahah != null) {
                    rightValue = ahah;
                }
                else {
                    // 指针才需要另开变量赋值!
                    if (rightValue.charAt(0) == '*') {
                        llvmir.append("%").append(regNum).append(" = ").append("load i32, i32").append(rightValue).append("\n");
                        rightValue = "%" + regNum;
                        regNum++;
                    }
                }
            }
            if (!regHash.containsKey(leftValue)) {
                llvmir.append("%" + regNum + " = icmp ne i32 " + leftValue + ", 0\n");
                regHash.put("%" + regNum, 1);
                leftValue="%"+regNum;
                regNum++;
            }
            if (!regHash.containsKey(rightValue)) {
                llvmir.append("%" + regNum + " = icmp ne i32 "+ rightValue + ", 0\n");
                regHash.put("%" + regNum, 1);
                rightValue="%"+regNum;
                regNum++;
            }
            int tmpVarNum = getNextTmpVarNum();
            llvmir.append("%").append(tmpVarNum).append(" = ")
                    .append(llvmOp).append(" i1 ")
                    .append(leftValue).append(", ").append(rightValue)
                    .append("\n");
            regHash.put("%" + tmpVarNum, 1);
            tmpValue = "%" + tmpVarNum; // 更新tmpValue为新的临时变量

        }
        if (lOrExpNode.lOrExpNode != null) {
            saveOp="or";
            visitLOrExp(lOrExpNode.lOrExpNode);
        }
    }

    private void visitLAndExp(LAndExpNode lAndExpNode) {
        //// LAndExp -> EqExp | EqExp '&&' LAndExp
        String leftValue = tmpValue;
        Integer leftAns=tmpAns;
        String llvmOp = saveOp;
        saveOp = null;
        tmpValue = null;

            LAndExpNode andNode = lAndExpNode;
            if (andNode.eqExpNode != null) {
                EqExpNode eqNode = andNode.eqExpNode;
                if (eqNode.relExpNode != null) {
                    RelExpNode relNode = eqNode.relExpNode;
                    if (relNode.addExpNode != null) {
                        AddExpNode addNode = relNode.addExpNode;
                        if (addNode.mulExpNode != null) {
                            MulExpNode mulNode = addNode.mulExpNode;
                            if (mulNode.unaryExpNode != null) {
                                UnaryExpNode unaryNode = mulNode.unaryExpNode;
                                if (unaryNode.primaryExpNode != null) {
                                    PrimaryExpNode primaryNode = unaryNode.primaryExpNode;
                                    if (primaryNode.numberNode != null) {
                                        NumberNode numberNode = primaryNode.numberNode;
                                        if (numberNode.getToken() != null && numberNode.getToken().content != null) {
                                            try {
                                                leftAns = Integer.parseInt(numberNode.getToken().content);
                                            } catch (NumberFormatException e) {
                                                leftAns=null;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        visitEqExp(lAndExpNode.eqExpNode);
        isAnd=false;
        String rightValue = tmpValue;
        if(leftAns!=null){
            if (leftAns == 0) {
                tmpAns=null;
                return;
            }
        }

        if (leftValue != null) {
            if (leftValue.matches("^-?\\d+$")) {

            }
            else {
                String haha = getValueFromRegiName(leftValue);
                if (haha != null) {
                    leftValue = haha;
                }
                else {
                    // 指针才需要另开变量赋值!
                    if (leftValue.charAt(0) == '*') {
                        llvmir.append("%").append(regNum).append(" = ").append("load i32, i32").append(leftValue).append("\n");
                        leftValue = "%" + regNum;
                        regNum++;
                    }

                }
            }
            if (rightValue.matches("^-?\\d+$")) {

            }
            else {
                String ahah = getValueFromRegiName(rightValue);

                if (ahah != null) {
                    rightValue = ahah;
                }
                else {
                    // 指针才需要另开变量赋值!
                    if (rightValue.charAt(0) == '*') {
                        llvmir.append("%").append(regNum).append(" = ").append("load i32, i32").append(rightValue).append("\n");
                        rightValue = "%" + regNum;
                        regNum++;
                    }
                }
            }
//            int tmpVarNum = getNextTmpVarNum(); // 获取下一个临时变量编号
//            llvmir.append("%").append(tmpVarNum).append(" = ")
//                    .append("icmp ne i32 ").append(leftValue +", 0"+ "\n");
//            leftValue="%"+tmpVarNum;
//            tmpVarNum = getNextTmpVarNum();
//            llvmir.append("%").append(tmpVarNum).append(" = ")
//                    .append("icmp ne i32 ").append(rightValue +", 0"+ "\n");
//            rightValue="%"+tmpVarNum;


            if (!regHash.containsKey(leftValue)) {
                llvmir.append("%" + regNum + " = icmp ne i32 "  + leftValue + ", 0\n");
                regHash.put("%" + regNum, 1);
                leftValue="%"+regNum;
                regNum++;
            }
            if (!regHash.containsKey(rightValue)) {
                llvmir.append("%" + regNum + " = icmp ne i32 "  + rightValue + ", 0\n");
                regHash.put("%" + regNum, 1);
                rightValue="%"+regNum;
                regNum++;
            }
            int tmpVarNum = getNextTmpVarNum();
            llvmir.append("%").append(tmpVarNum).append(" = ")
                    .append(llvmOp).append(" i1 ")
                    .append(leftValue).append(", ").append(rightValue)
                    .append("\n");
            regHash.put("%" + tmpVarNum, 1);
            tmpValue = "%" + tmpVarNum; // 更新tmpValue为新的临时变量

        }


        if (lAndExpNode.lAndExpNode != null) {
            saveOp="and";
            visitLAndExp(lAndExpNode.lAndExpNode);
        }
    }

    private void visitEqExp(EqExpNode eqExpNode) {
        // EqExp -> RelExp | RelExp ('==' | '!=') EqExp
        String leftValue = tmpValue;
        String llvmOp = saveOp;
        saveOp = null;
        tmpValue = null;
        visitRelExp(eqExpNode.relExpNode);
        String rightValue = tmpValue;
        if (leftValue != null){
            if (leftValue.matches("^-?\\d+$")) {

            }
            else {
                String haha = getValueFromRegiName(leftValue);
                if (haha != null) {
                    leftValue = haha;
                }
                else {
                    // 指针才需要另开变量赋值!
                    if (leftValue.charAt(0) == '*') {
                        llvmir.append("%").append(regNum).append(" = ").append("load i32, i32").append(leftValue).append("\n");
                        leftValue = "%" + regNum;
                        regNum++;
                    }

                }
            }
            if (rightValue.matches("^-?\\d+$")) {

            }
            else {
                String ahah = getValueFromRegiName(rightValue);

                if (ahah != null) {
                    rightValue = ahah;
                }
                else {
                    // 指针才需要另开变量赋值!
                    if (rightValue.charAt(0) == '*') {
                        llvmir.append("%").append(regNum).append(" = ").append("load i32, i32").append(rightValue).append("\n");
                        rightValue = "%" + regNum;
                        regNum++;
                    }
                }
            }

            int tmpVarNum ; // 获取下一个临时变量编号
            if (regHash.containsKey(leftValue)) {
                //说明是i1,需要转换成i32
                tmpVarNum = getNextTmpVarNum();
                llvmir.append("%").append(tmpVarNum).append(" = ")
                        .append("zext i1 "+leftValue+" to i32\n");
                leftValue="%"+tmpVarNum;
            }
            if (regHash.containsKey(rightValue)) {
                //说明是i1,需要转换成i32
                tmpVarNum = getNextTmpVarNum();
                llvmir.append("%").append(tmpVarNum).append(" = ")
                        .append("zext i1 "+rightValue+" to i32\n");
                rightValue="%"+tmpVarNum;
            }
            tmpVarNum = getNextTmpVarNum();
            regHash.put("%" + tmpVarNum, 1);
            llvmir.append("%").append(tmpVarNum).append(" = ")
                    .append(llvmOp).append(" i32 ")
                    .append(leftValue).append(", ").append(rightValue)
                    .append("\n");
            tmpValue = "%" + tmpVarNum;
        }
        if (eqExpNode.eqExpNode != null) {
            if (eqExpNode.operator.content == "==") {
                saveOp="icmp eq ";
            }
            else if (eqExpNode.operator.content == "!=") {
                saveOp = "icmp ne ";
            }
            visitEqExp(eqExpNode.eqExpNode);
        }
    }
    private void visitRelExp(RelExpNode relExpNode){
        // RelExp -> AddExp | AddExp ('<' | '>' | '<=' | '>=') RelExp
        // if(3>5)
        String leftValue = tmpValue;
        String llvmOp = saveOp;
        saveOp = null;
        tmpValue = null;
        visitAdd(relExpNode.addExpNode);
        String rightValue = tmpValue;
        if (leftValue != null){
            if (leftValue.matches("^-?\\d+$")) {

            }
            else {
                String haha = getValueFromRegiName(leftValue);
                if (haha != null) {
                    leftValue = haha;
                }
                else {
                    // 指针才需要另开变量赋值!
                    if (leftValue.charAt(0) == '*') {
                        llvmir.append("%").append(regNum).append(" = ").append("load i32, i32").append(leftValue).append("\n");
                        leftValue = "%" + regNum;
                        regNum++;
                    }

                }
            }
            if (rightValue.matches("^-?\\d+$")) {

            }
            else {
                String ahah = getValueFromRegiName(rightValue);

                if (ahah != null) {
                    rightValue = ahah;
                }
                else {
                    // 指针才需要另开变量赋值!
                    if (rightValue.charAt(0) == '*') {
                        llvmir.append("%").append(regNum).append(" = ").append("load i32, i32").append(rightValue).append("\n");
                        rightValue = "%" + regNum;
                        regNum++;
                    }
                }
            }
            int tmpVarNum ; // 获取下一个临时变量编号
            if (regHash.containsKey(leftValue)) {
                //说明是i1,需要转换成i32
                tmpVarNum = getNextTmpVarNum();
                llvmir.append("%").append(tmpVarNum).append(" = ")
                        .append("zext i1 "+leftValue+" to i32\n");
                leftValue="%"+tmpVarNum;
            }
            if (regHash.containsKey(rightValue)) {
                //说明是i1,需要转换成i32
                tmpVarNum = getNextTmpVarNum();
                llvmir.append("%").append(tmpVarNum).append(" = ")
                        .append("zext i1 "+rightValue+" to i32\n");
                rightValue="%"+tmpVarNum;
            }
            tmpVarNum = getNextTmpVarNum();

            regHash.put("%" + tmpVarNum, 1);
            llvmir.append("%").append(tmpVarNum).append(" = ")
                    .append(llvmOp).append(" i32 ")
                    .append(leftValue).append(", ").append(rightValue)
                    .append("\n");

            tmpValue = "%" + tmpVarNum;


        }
        if (relExpNode.relExpNode != null) {
            isRelation=true;
            if (relExpNode.operator.content == "<") {
                saveOp = "icmp slt ";
            }
            else if (relExpNode.operator.content == ">") {
                saveOp = "icmp sgt ";
            }
            else if (relExpNode.operator.content == "<=") {
                saveOp = "icmp sle ";
            }
            else if (relExpNode.operator.content == ">=") {
                saveOp = "icmp sge ";
            }
            visitRelExp(relExpNode.relExpNode);

        }

    }

    private void visitExp(ExpNode expNode) {
        //Exp         → AddExp
        saveOp = null;
        saveValue = null;
        tmpValue = null;
        isInExp = true;
        isInExpStack.push(true);
        visitAdd(expNode.addExpNode);
        isInExpStack.pop();
        isInExp = false;
    }

    private void visitAdd(AddExpNode addExpNode) {
        // AddExp -> MulExp | MulExp ('+' | '−') AddExp

        if (isGlobal||isArray) {
            Integer value = saveValue;
            String op = saveOp;
            saveOp = null;
            saveValue = null;
            this.visitMulExp(addExpNode.mulExpNode);
            if (value != null) {
                saveValue = calculate(op, value, saveValue);
            }
            if (addExpNode.addExpNode != null) {
                if (addExpNode.operation.type == TokenType.PLUS) {
                    saveOp = "add";
                }
                else if (addExpNode.operation.type == TokenType.MINU) {
                    saveOp = "sub";
                }
                this.visitAdd(addExpNode.addExpNode);
//                if (saveValue != null) {
//                    saveOp = addExpNode.operation.content;
//                    saveValue = this.calculate(saveOp, tmpValue, saveValue);
//                }
            }
        }
        else {
            // 处理左侧的乘法表达式
            // AddExp -> MulExp | MulExp ('+' | '−') AddExp
            String leftValue = tmpValue;
            String llvmOp = saveOp;
            saveOp = null;
            tmpValue = null;
            visitMulExp(addExpNode.mulExpNode);
            String rightValue = tmpValue; // 保存左侧表达式的结果
            if (leftValue != null) {
                if (leftValue.matches("^-?\\d+$")) {

                }
                else {
                    String haha = getValueFromRegiName(leftValue);
                    if (haha != null) {
                        leftValue = haha;
                    }
                    else {
                        // 指针才需要另开变量赋值!
                        if (leftValue.charAt(0) == '*') {
                            llvmir.append("%").append(regNum).append(" = ").append("load i32, i32").append(leftValue).append("\n");
                            leftValue = "%" + regNum;
                            regNum++;
                        }

                    }
                }
                if (rightValue.matches("^-?\\d+$")) {

                }
                else {
                    String ahah = getValueFromRegiName(rightValue);

                    if (ahah != null) {
                        rightValue = ahah;
                    }
                    else {
                        // 指针才需要另开变量赋值!
                        if (rightValue.charAt(0) == '*') {
                            llvmir.append("%").append(regNum).append(" = ").append("load i32, i32").append(rightValue).append("\n");
                            rightValue = "%" + regNum;
                            regNum++;
                        }
                    }
                }

                int tmpVarNum = getNextTmpVarNum(); // 获取下一个临时变量编号
                llvmir.append("%").append(tmpVarNum).append(" = ")
                        .append(llvmOp).append(" i32 ")
                        .append(leftValue).append(", ").append(rightValue)
                        .append("\n");
                tmpValue = "%" + tmpVarNum; // 更新tmpValue为新的临时变量

            }
            // 如果有右侧加法表达式，递归处理
            if (addExpNode.addExpNode != null) {
                if (addExpNode.operation.type == TokenType.PLUS) {
                    saveOp = "add";
                }
                else if (addExpNode.operation.type == TokenType.MINU) {
                    saveOp = "sub";
                }
                visitAdd(addExpNode.addExpNode);
//                String rightValue = tmpValue; // 保存右侧表达式的结果
//
//                // 生成LLVM IR代码
//                String llvmOp = addExpNode.operation.getType() == TokenType.PLUS ? "add" : "sub";
                // 如果要运算的是全局变量, 直接取出值, 全局变量肯定知道值
                // 如果直接是数, 就不做处理!

            }
            else {
                //tmpValue = leftValue; // 没有右侧表达式，直接使用左侧结果
//                llvmir.append("%").append(regNum++).append(" = ")
//                        .append("add").append(" i32 ")
//                        .append(leftValue).append(", ").append("0")
//                        .append("\n");
            }
        }

    }

    // 辅助方法：获取下一个临时变量编号
    private int getNextTmpVarNum() {
        // 假设有一个全局变量来跟踪当前的临时变量编号
        return regNum++;
    }


    private void visitMulExp(MulExpNode mulExpNode) {
        //        MulExp     → UnaryExp |UnaryExp ('*' | '/' | '%') MulExp
        if (isGlobal||isArray) {
            Integer value = saveValue;
            String op = saveOp;
            saveOp = null;
            saveValue = null;
            this.visitUnaryExp(mulExpNode.unaryExpNode);
            if (value != null) {
                saveValue = calculate(op, value, saveValue);
            }
            if (mulExpNode.mulExpNode != null) {
//                Integer tmpValue = saveValue;
//                saveValue = null;
                if (mulExpNode.operation.type == TokenType.MULT) {
                    saveOp = "mul";
                }
                else if (mulExpNode.operation.type == TokenType.DIV) {
                    saveOp = "sdiv";
                }
                else if (mulExpNode.operation.type == TokenType.MOD) {
                    saveOp = "srem";
                }

                this.visitMulExp(mulExpNode.mulExpNode);
//                if (saveValue != null) {
//                    saveOp = mulExpNode.operation.content;
//                    saveValue = this.calculate(saveOp, tmpValue, saveValue);
//                    saveOp = null;
//                }
            }
        }
        else {
            // 处理左侧的一元表达式, 注意你的语法树改写过文法, 现在要调整回来!!!
            String leftValue = tmpValue;
            String llvmOp = saveOp;
            saveOp = null;
            tmpValue = null;
            visitUnaryExp(mulExpNode.unaryExpNode);
            String rightValue = tmpValue;
            if (leftValue != null) {
                if (leftValue.matches("^-?\\d+$")) {

                }
                else {
                    String haha = getValueFromRegiName(leftValue);
                    if (haha != null) {
                        leftValue = haha;
                    }
                    else {
                        // 指针才需要另开变量赋值!
                        if (leftValue.charAt(0) == '*') {
                            llvmir.append("%").append(regNum).append(" = ").append("load i32, i32").append(leftValue).append("\n");
                            leftValue = "%" + regNum;
                            regNum++;
                        }

                    }
                }
                if (rightValue.matches("^-?\\d+$")) {

                }
                else {
                    String ahah = getValueFromRegiName(rightValue);

                    if (ahah != null) {
                        rightValue = ahah;
                    }
                    else {
                        // 指针才需要另开变量赋值!
                        if (rightValue.charAt(0) == '*') {
                            llvmir.append("%").append(regNum).append(" = ").append("load i32, i32").append(rightValue).append("\n");
                            rightValue = "%" + regNum;
                            regNum++;
                        }
                    }
                }

                int tmpVarNum = getNextTmpVarNum(); // 获取下一个临时变量编号
                llvmir.append("%").append(tmpVarNum).append(" = ")
                        .append(llvmOp).append(" i32 ")
                        .append(leftValue).append(", ").append(rightValue)
                        .append("\n");
                tmpValue = "%" + tmpVarNum; // 更新tmpValue为新的临时变量
            }
            //String leftValue = tmpValue; // 保存左侧表达式的结果

            // 如果有右侧乘法表达式，递归处理
            if (mulExpNode.mulExpNode != null) {
                if (mulExpNode.operation.type == TokenType.MULT) {
                    saveOp = "mul";
                }
                else if (mulExpNode.operation.type == TokenType.DIV) {
                    saveOp = "sdiv";
                }
                else if (mulExpNode.operation.type == TokenType.MOD) {
                    saveOp = "srem";
                }
                visitMulExp(mulExpNode.mulExpNode);

                //String rightValue = tmpValue; // 保存右侧表达式的结果

                // 生成LLVM IR代码
//                String llvmOp;
//                if (mulExpNode.operation.getType() == TokenType.MULT) {
//                    llvmOp = "mul";
//                } else if (mulExpNode.operation.getType() == TokenType.DIV) {
//                    llvmOp = "sdiv";
//                } else {
//                    llvmOp = "srem";
//                }
                // 如果直接是数, 就不做处理!

            }
            else {
                //tmpValue = leftValue; // 没有右侧表达式，直接使用左侧结果
            }
        }
    }


    private void visitUnaryExp(UnaryExpNode unaryExpNode) {
        // UnaryExp -> PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
        if (isGlobal||isArray) {
            if (unaryExpNode.primaryExpNode != null) {
                this.visitPrimaryExp(unaryExpNode.primaryExpNode);
            }
            else if (unaryExpNode.unaryExpNode != null) {
                this.visitUnaryExp(unaryExpNode.unaryExpNode);
                if (Objects.equals(unaryExpNode.unaryOpNode.token.content, "-")) {
                    saveValue = -saveValue;
                }
            }
        }
        else {
            if (unaryExpNode.primaryExpNode != null) {
                // 处理主表达式
                visitPrimaryExp(unaryExpNode.primaryExpNode);
            }
            else if (unaryExpNode.unaryOpNode != null) {
                // 处理一元操作符
                visitUnaryExp(unaryExpNode.unaryExpNode);
                String value = tmpValue;

                // 生成LLVM IR代码
                String llvmOp;
                if (unaryExpNode.unaryOpNode.token.getType() == TokenType.MINU) {
                    llvmOp = "sub";
                    // 如果直接是数, 就不做处理!
                    if (value.matches("^-?\\d+$")) {

                    }
                    else {
                        String haha = getValueFromRegiName(value);
                        if (haha != null) {
                            value = haha;
                        }
                        else {
                            if (value.charAt(0) == '*') {
                                llvmir.append("%").append(regNum).append(" = ").append("load i32, i32").append(value).append("\n");
                                value = "%" + regNum;
                                regNum++;
                            }

                        }
                    }


                    int tmpVarNum = getNextTmpVarNum();
                    llvmir.append("%").append(tmpVarNum).append(" = ")
                            .append(llvmOp).append(" i32 0, ").append(value)
                            .append("\n");
                    tmpValue = "%" + tmpVarNum;
                }
                else {
                    // 其他一元操作，如 NOT
                    // 根据需求添加相应的代码
                }
            }
            else {
                //Ident '(' [FuncRParams] ')'
                //先判断参数个数
                FuncSymbol funcSymbol = (FuncSymbol) symbolTables.get(0).first.get
                        (unaryExpNode.ident.content);
//                List<String> args = new ArrayList<String>();
                // 参数数量
                int length = 0;
                if (unaryExpNode.funcRParamsNode != null && unaryExpNode.funcRParamsNode.expNodes != null) {
                    length = unaryExpNode.funcRParamsNode.expNodes.size();
                }
                if (funcSymbol.funcParams.size() != 0) {
                    if (unaryExpNode.funcRParamsNode != null) {
                        visitfuncRParams(unaryExpNode.funcRParamsNode);
                    }

                }

                if (funcSymbol.type == FuncSymbol.ReturnType.VOID) {
                    llvmir.append("call void " + funcSymbol.name + "(");

                }
                else if (funcSymbol.type == FuncSymbol.ReturnType.INT) {
                    llvmir.append("%" + regNum + " =  call i32 " + funcSymbol.name + "(");
                    tmpValue = "%" + regNum;
                    regNum++;
                }

                if (length > 0) {
                    int i;
                    for (i = 0; i < length ; i++) {
                        if (funcSymbol.funcParams.get(i).dimension == 0) {
                            llvmir.append("i32 " + "%" + funcRParamsArrayStack.peek().get(i) + ", ");
                        }
                        else if (funcSymbol.funcParams.get(i).dimension == 1) {
                            llvmir.append("i32* " + "%" + funcRParamsArrayStack.peek().get(i) + ", ");
                        }
                        else if (funcSymbol.funcParams.get(i).dimension == 2) {
                            //[3 x i32]* %21

                            llvmir.append("["+funcSymbol.funcParams.get(i).size+" x i32]* " + "%" + funcRParamsArrayStack.peek().get(i) + ", ");
                        }
                    }
                    // 后退两个字符
                    int ll_length = llvmir.length();
                    if (ll_length >= 2) {
                        llvmir.delete(ll_length - 2, ll_length);
                    }
                    //funcRParamsArray.clear();
                    if (funcSymbol.funcParams.size() != 0) {
                        if (unaryExpNode.funcRParamsNode != null) {
                            funcRParamsArrayStack.pop();
                        }
                        }

                }
                llvmir.append(")\n");

            }
        }
    }

    private void visitfuncRParams(FuncRParamsNode funcRParamsNode) {
//        FuncRParams → Exp { ',' Exp }
        funcRParamsArrayStack.push(new ArrayList<>());
        for (ExpNode expNode : funcRParamsNode.expNodes) {

            isFuncParamStack.push(true);
            visitExp(expNode);
            int prev_reg=regNum-1;// 这就是实参数的寄存器
            addToFuncRParamsArrayStack(prev_reg);
            isFuncParamStack.pop();
        }
    }

    private void addToFuncRParamsArrayStack(int prevReg) {
        List<Integer> list = funcRParamsArrayStack.peek();
        funcRParamsArrayStack.pop();
        list.add(prevReg);
        funcRParamsArrayStack.push(list);
    }


    private void visitFunctionOrVariable(IdentNode ident, FuncRParamsNode funcRParamsNode) {
        // 根据程序的具体实现，这里可能需要处理函数调用或变量访问
        // 生成相应的LLVM IR代码
    }


    private void visitPrimaryExp(PrimaryExpNode primaryExpNode) {
        //PrimaryExp → '(' Exp ')' | LVal | Number
        if (isGlobal||isArray) {
            if (primaryExpNode.numberNode != null) {
                saveValue = Integer.valueOf(primaryExpNode.numberNode.getToken().content);
            }
            else if (primaryExpNode.lValNode != null) {
                visitLval(primaryExpNode.lValNode);
            }
            else if (primaryExpNode.expNode != null) {
                this.visitExp(primaryExpNode.expNode);
            }
        }
        else {
            if (primaryExpNode.expNode != null) {
                // 处理括号内的表达式
                visitExp(primaryExpNode.expNode);
            }
            else if (primaryExpNode.lValNode != null) {
                // 处理变量
                visitLval(primaryExpNode.lValNode);
            }
            else if (primaryExpNode.numberNode != null) {
                // 处理数字
                visitNumber(primaryExpNode.numberNode);
            }
        }
    }

    private void visitNumber(NumberNode numberNode) {
        tmpValue = numberNode.getToken().content;
        saveValue = Integer.valueOf(tmpValue);
        if (!isGlobal) {
            llvmir.append("%").append(regNum).append(" = add i32 0, ").append(tmpValue).append("\n");
            regNum++;
        }
    }

    private void visitLval(LValNode lValNode) {
        //主要处理函数实参, 看看函数中呢,一样吗
        // LVal '=' Exp ';'这个文法并没有调用该函数.
        // 该函数主要处理表达式里的Lval
        //LVal → Ident {'[' Exp ']'} '
        if (isInFunc) {
            //ArraySymbol arraySymbol = (ArraySymbol) symbolTables.get(symbolTables.size() - 1).first.get(lValNode.ident.content);
            ArraySymbol arraySymbol = getArraySymbolFromFunc(lValNode.ident.content);
            if (arraySymbol == null) {
                isNotInFuncStack.push(true);
            }
        }

        ArraySymbol arraySymboll = getArraySymbol(lValNode.ident.content);

        if (lValNode.expNodes.isEmpty()&&arraySymboll.dimension==0) {//0维度
            if (isGlobal||isArray) {
                saveValue = Integer.valueOf(this.getValueFromVar(lValNode.ident.content));
            }
            else {
                tmpValue = getRegiNameFromVar(lValNode.ident.content);
                if (!isInExpStack.empty()) {
                    llvmir.append("%").append(regNum).append(" = load i32, i32 ").append(tmpValue).append("\n");
                    tmpValue = "%" + String.valueOf(regNum);
                    regNum++;
                }
            }

        }
        else {
            if (!isInFunc||!isNotInFuncStack.empty()) {
                if (arraySymboll.dimension == 1 && lValNode.expNodes.size() == 1 ||
                        arraySymboll.dimension == 2 && lValNode.expNodes.size() == 2) {//0维度
                    if (isGlobal) {
                        Integer x = null, y = null;
                        if (lValNode.expNodes.size() >= 1) {
                            visitExp(lValNode.expNodes.get(0));
                            x = saveValue;
                        }
                        if (lValNode.expNodes.size() >= 2) {
                            visitExp(lValNode.expNodes.get(1));
                            y = saveValue;
                        }
                        saveValue = Integer.valueOf(getValueFromVar(lValNode.ident.content, x, y));
                    }
                    else {
                        String x = null, y = null;
//                    int x=1,y=1;
//                    int p=c[x][y];
                        //  %9 = getelementptr inbounds ...* %2, i32 0, i32 %8, i32 %9; like this, 其实也好搞
                        if (lValNode.expNodes.size() >= 1) {
                            visitExp(lValNode.expNodes.get(0));
                            int prev_reg = regNum - 1;
                            x = "%" + prev_reg;
                        }
                        if (lValNode.expNodes.size() >= 2) {
                            visitExp(lValNode.expNodes.get(1));
                            int prev_reg = regNum - 1;
                            y = "%" + prev_reg;
                        }
                        if (y == null) {
                            //1d
                            List<Integer> array = new ArrayList<>();
                            ArraySymbol arraySymbol = null;
                            for (int i = symbolTables.size() - 1; i >= 0; i--) {
                                if (symbolTables.get(i).first.containsKey(lValNode.ident.content)) {
                                    Symbol symbol = symbolTables.get(i).first.get(lValNode.ident.content);
                                    if (symbol instanceof ArraySymbol) {
                                        if (((ArraySymbol) symbol).dimension == 1) {
                                            array = ((ArraySymbol) symbol).value1d;
                                            arraySymbol = (ArraySymbol) symbol;
                                            break;
                                        }
                                    }
                                }
                            }
                            String array_size = "[" + array.size() + " x i32]";
                            llvmir.append("%" + regNum + " = getelementptr "
                                    + array_size + ", " + array_size + arraySymbol.regiName + ", i32 0, i32 " + x + "\n");
                            int prev_reg = regNum;
                            regNum++;
                            if (!isInExpStack.empty()) {
                                llvmir.append("%").append(regNum).append(" = load i32, i32* %").append(prev_reg).append("\n");
                                tmpValue = "%" + String.valueOf(regNum);
                                regNum++;
                            }
                        }
                        else {//2d//%3 = getelementptr [5 x [7 x i32]], [5 x [7 x i32]]* @a, i32 0, i32 3, i32 4
                            List<List<Integer>> array = new ArrayList<>();
                            ArraySymbol arraySymbol = null;
                            for (int i = symbolTables.size() - 1; i >= 0; i--) {
                                if (symbolTables.get(i).first.containsKey(lValNode.ident.content)) {
                                    Symbol symbol = symbolTables.get(i).first.get(lValNode.ident.content);
                                    if (symbol instanceof ArraySymbol) {
                                        if (((ArraySymbol) symbol).dimension == 2) {
                                            array = ((ArraySymbol) symbol).value2d;
                                            arraySymbol = (ArraySymbol) symbol;
                                        }
                                    }
                                }
                            }
                            String array_size = "[" + array.size() + " x [" + array.get(0).size() + " x i32]]";
                            llvmir.append("%" + regNum + " = getelementptr "
                                    + array_size + ", " + array_size + arraySymbol.regiName + ", i32 0, i32 " + x + ", i32 " + y + "\n");
                            int prev_reg = regNum;
                            regNum++;
                            if (!isInExpStack.empty()) {
                                llvmir.append("%").append(regNum).append(" = load i32, i32* %").append(prev_reg).append("\n");
                                tmpValue = "%" + String.valueOf(regNum);
                                regNum++;
                            }
                        }
                    }
                }
                else { // 这里面的都不可能是复杂表达式, 一定只有一个字母!
                    if (arraySymboll.dimension == 1) {//一维参数// int x[3];// func(x)
                        ArraySymbol arraySymbol = getArraySymbol(lValNode.ident.content);
                        List<Integer> array = arraySymbol.value1d;
                        String array_size = "[" + array.size() + " x i32]";
                        llvmir.append("%" + regNum + " = getelementptr "
                                + array_size + ", " + array_size + arraySymbol.regiName + ", i32 0, i32 0" + "\n");
                        regNum++;

                    }
                    else if (arraySymboll.dimension == 2) {
                        ArraySymbol arraySymbol = getArraySymbol(lValNode.ident.content);
                        List<List<Integer>> array = arraySymbol.value2d;
                        String array_size1d = "[" + array.get(0).size() + " x i32]";
                        String array_size2d = "[" + array.size() + " x [" + array.get(0).size() + " x i32]]";
                        if (lValNode.expNodes.size() == 1) {//一维参数
                            String x = null;
                            visitExp(lValNode.expNodes.get(0));
                            int pre_reg = regNum - 1;
                            llvmir.append("%" + regNum + " = mul i32 " + array.get(0).size() + ", %" + pre_reg + "\n");
                            regNum++;
                            int prev_reg = regNum - 1;
                            x = "%" + prev_reg;
                            llvmir.append("%" + regNum + " = getelementptr "
                                    + array_size2d + ", " + array_size2d + arraySymbol.regiName + ", i32 0, i32 0" + "\n");
                            int pa = regNum;
                            regNum++;
                            llvmir.append("%" + regNum + " = getelementptr "
                                    + array_size1d + ", " + array_size1d + "* %" + pa + ", i32 0, i32 " + x + "\n");
                            regNum++;

                        }
                        else if (lValNode.expNodes.size() == 0) {//二维参数
                            llvmir.append("%" + regNum + " = getelementptr "
                                    + array_size2d + ", " + array_size2d + arraySymbol.regiName + ", i32 0, i32 0" + "\n");
                            regNum++;

                        }
                    }
                }
            }
            else {
               // 处理函数中的数组情况
                if (arraySymboll.dimension == 1 && lValNode.expNodes.size() == 1 ||
                        arraySymboll.dimension == 2 && lValNode.expNodes.size() == 2) {
                    if (arraySymboll.dimension == 1 && lValNode.expNodes.size() == 1) {
                        String x = null;


                        visitExp(lValNode.expNodes.get(0));
                        int prev_reg = regNum - 1;
                        x = "%" + prev_reg;
                        llvmir.append("%" + regNum + " = load i32*, i32" + arraySymboll.regiName + "\n");
                        int arr_reg = regNum;
                        regNum++;
                        llvmir.append("%" + regNum + " = getelementptr i32, i32* %" + arr_reg + ", i32 " + x + "\n");
                        int this_prev_reg = regNum;
                        regNum++;
                        llvmir.append("%" + regNum + " = load i32, i32* %" + this_prev_reg + "\n");// 这样就获取到值了
                        regNum++;

                        if (!isInExpStack.empty()) {
                            llvmir.append("%").append(regNum).append(" = load i32, i32* %").append(this_prev_reg).append("\n");
                            tmpValue = "%" + String.valueOf(regNum);
                            regNum++;
                        }
                    }
                    else {
                        //TODO: 如果是在函数里定义的数组, 不能执行下面的代码!!!!
                        String x=null,y=null;
                        visitExp(lValNode.expNodes.get(0));
                        int exp_reg = regNum - 1;
                        x = "%" + exp_reg;
                        visitExp(lValNode.expNodes.get(1));
                        int exp_reg2 = regNum - 1;
                        y = "%" + exp_reg2;

                        String size=doRegex(arraySymboll.regiName);
                        llvmir.append("%" + regNum + " = load " + size + "*, " + arraySymboll.regiName + "\n");
                        int prev_reg=regNum;
                        regNum++;
                        llvmir.append("%" + regNum + " = getelementptr "+size +", "+size+"* %"+prev_reg+", i32 %"+exp_reg+"\n");
                        prev_reg=regNum;
                        regNum++;
                        llvmir.append("%" + regNum + " = getelementptr "+size +", "+size+"* %"+prev_reg+", i32 0, i32 %"+exp_reg2+"\n");
                        prev_reg=regNum;
                        regNum++;
//                        llvmir.append("%" + regNum + " = load i32, i32 *%" + prev_reg + "\n");
//                        tmpValue="%"+regNum;
//                        regNum++;
                        if (!isInExpStack.empty()) {
                            llvmir.append("%").append(regNum).append(" = load i32, i32* %").append(prev_reg).append("\n");
                            tmpValue = "%" + String.valueOf(regNum);
                            regNum++;
                        }
                    }
                }
            }

        }
        if (!isNotInFuncStack.empty()) {
            isNotInFuncStack.pop();
        }

    }

    //找非全局的数组
    private ArraySymbol getArraySymbolFromFunc(String content) {
        boolean notReachFunc=true;
        for (int i = symbolTables.size() - 1; i >= 0; i--) {
            if (notReachFunc) {
                if (symbolTables.get(i).first.containsKey(content)) {
                    Symbol symbol=symbolTables.get(i).first.get(content);
                    if (symbol instanceof ArraySymbol) {
                        return (ArraySymbol) symbol;
                    }
                }
            }
            if (symbolTables.get(i).second) {
                notReachFunc=false;
                if (symbolTables.get(i).first.containsKey(content)) {
                    Symbol symbol=symbolTables.get(i).first.get(content);
                    if (symbol instanceof ArraySymbol) {
                        return (ArraySymbol) symbol;
                    }
                }
                return null;
            }
        }
        return null;
    }

    private ArraySymbol getArraySymbol(String content) {
        for (int i = symbolTables.size() - 1; i >= 0; i--) {
            if (symbolTables.get(i).first.containsKey(content)) {
                Symbol symbol = symbolTables.get(i).first.get(content);
                if (symbol instanceof ArraySymbol) {
                    return (ArraySymbol) symbol;
                }
            }
        }
        return null;
    }


    public String toString() {

        return llvmir.toString();
    }

}








