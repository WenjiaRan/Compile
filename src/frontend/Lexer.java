package frontend;

import token.Token;
import token.TokenType;
import utils.IOUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer {
    private static final Lexer instance = new Lexer();

    public static Lexer getInstance() {
        return instance;
    }

    private List<Token> tokens = new ArrayList<>();

    public List<Token> getTokens() {
        return tokens;
    }
    //private String source; //输入编译器的源程序字符串
    private char p; //指向下一个待解析的单词的起始字符
    private String token;
    private TokenType type;
    private int line;
    private int number;
    // 关键字
    private Map<String, TokenType> keywords;

    public Lexer() {
        keywords = new HashMap<>();
        keywords.put("main", TokenType.MAINTK);
        keywords.put("const", TokenType.CONSTTK);
        keywords.put("int", TokenType.INTTK);
        keywords.put("break", TokenType.BREAKTK);
        keywords.put("continue", TokenType.CONTINUETK);
        keywords.put("if", TokenType.IFTK);
        keywords.put("else", TokenType.ELSETK);
        keywords.put("for", TokenType.FORTK);
        keywords.put("getint", TokenType.GETINTTK);
        keywords.put("printf", TokenType.PRINTFTK);
        keywords.put("return", TokenType.RETURNTK);
        keywords.put("void", TokenType.VOIDTK);
    }
    public void analyze(String source) {
        line = 1; // 当前所在行数
        int contentLength = source.length(); // 源代码长度
        for (int i = 0; i < contentLength; i++) {
            p = source.charAt(i);
            char next = i + 1 < contentLength ? source.charAt(i + 1) : '\0';
            if (p == '\n') line++;
            else if (p == '_' || Character.isLetter(p)) {//保留字or标识符
                String s = "";
                for (int j = i; j < contentLength; j++) {
                    char d = source.charAt(j);
                    if (d == '_' || Character.isLetter(d) || Character.isDigit(d)) s += d;
                    else {
                        i = j - 1;
                        break;
                    }
                }
                tokens.add(new Token(keywords.getOrDefault(s, TokenType.IDENFR), line, s));
            } else if (Character.isDigit(p)) { //数字
                String s = "";
                for (int j = i; j < contentLength; j++) {
                    char d = source.charAt(j);
                    if (Character.isDigit(d)) s += d;
                    else {
                        i = j - 1;
                        break;
                    }
                }
                tokens.add(new Token(TokenType.INTCON, line, s));
            } else if (p == '\"') { // 字符串
                String s = "\"";
                for (int j = i+1; j < contentLength; j++) {
                    char d = source.charAt(j);
                    if (d != '\"') {
                        s += d;
                        if (d == 32 || d == 33 || d >= 40 && d <= 126) { //Normal Char
                            if (d == 92 && (j + 1 == contentLength || source.charAt(j + 1) != 'n')) {
                                // 错误
                            }
                        } else if (d == 37) { //Format Char
                            if (j + 1 == contentLength || source.charAt(j + 1) != 'd') {
                                // 错误
                            }
                        } else {
                            // 错误
                        }
                    } else {
                        i = j; // i 指到 " 的位置
                        s += "\"";
                        break;
                    }
                }
                tokens.add(new Token(TokenType.STRCON, line, s));
            } else if (p == '!') { // ! or !=
                if (next != '=') tokens.add(new Token(TokenType.NOT, line, "!"));
                else {
                    tokens.add(new Token(TokenType.NEQ, line, "!="));
                    i++;
                }
            } else if (p == '&') { // &&
                if (next == '&') {
                    tokens.add(new Token(TokenType.AND, line, "&&"));
                    i++;
                }
            } else if (p == '|') { // ||
                if (next == '|') {
                    tokens.add (new Token(TokenType.OR, line, "||"));
                    i++;
                }
            } else if (p == '+') {
                tokens.add(new Token(TokenType.PLUS, line, "+"));
            } else if (p == '-') {
                tokens.add(new Token(TokenType.MINU, line, "-"));
            } else if (p == '*') {
                tokens.add(new Token(TokenType.MULT, line, "*"));
            } else if (p == '/') {// 除法 OR 单行注释// OR 多行注释 /*
                if (next == '/') {
                    // 查找从i + 2位置开始的第一个换行符\n的索引。表示单行注释的结束位置。
                    int j = source.indexOf('\n', i + 2);
                    // 如果没有找到换行符，注释直到文件的末尾
                    if (j == -1) j = contentLength;
                    i = j - 1;// 指向注释结束后的字符
                } else if (next == '*') {
                    for (int j=i+2; j< contentLength;j++) {
                        char e = source.charAt(j);
                        if (e=='\n') line++;
                        else if (e=='*' && source.charAt(j+1)=='/') { // j+1有可能超出contentLength, 需要错误处理!!!
                            i = j+1;
                            break;
                        }
                    }
                } else tokens.add(new Token(TokenType.DIV, line, "/"));

            } else if (p == '%') {
                tokens.add (new Token(TokenType.MOD, line, "%"));
            } else if (p == '<') { // < OR <=
                if (next == '=') {
                    tokens.add (new Token(TokenType.LEQ, line , "<="));
                    i++;
                } else {
                    tokens.add(new Token(TokenType.LSS, line, "<"));
                }
            } else if (p == '>') { // < OR <=
                if (next == '=') {
                    tokens.add (new Token(TokenType.GEQ, line , ">="));
                    i++;
                } else {
                    tokens.add(new Token(TokenType.GRE, line, ">"));
                }
            } else if (p ==  '=') {
                if (next == '=') {
                    tokens.add(new Token( TokenType.EQL, line, "=="));
                    i++;
                } else {
                    tokens.add(new Token( TokenType.ASSIGN, line, "="));
                }
            } else if (p == ';') tokens.add(new Token(TokenType.SEMICN, line, ";"));
            else if (p == ',') tokens.add(new Token(TokenType.COMMA, line, ","));
            else if (p == '(') tokens.add(new Token(TokenType.LPARENT, line, "("));
            else if (p == ')') tokens.add(new Token(TokenType.RPARENT, line, ")"));
            else if (p == '[') tokens.add(new Token(TokenType.LBRACK, line, "["));
            else if (p == ']') tokens.add(new Token(TokenType.RBRACK, line, "]"));
            else if (p == '{') tokens.add(new Token(TokenType.LBRACE, line, "{"));
            else if (p == '}') tokens.add(new Token(TokenType.RBRACE, line, "}"));

        }
    }

    public void printLexAns() {
        for (Token token : tokens) {
            IOUtils.write(token.toString());

        }
    }
}











