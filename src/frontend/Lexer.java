package frontend;

import token.Token;
import token.TokenType;
import utils.IOUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer {
    public static final Lexer instance = new Lexer();

    public static Lexer getInstance() {
        return instance;
    }

    public List<Token> tokens = new ArrayList<>();

    public List<Token> getTokens() {
        return tokens;
    }
    //public String source; //输入编译器的源程序字符串
    public char p; //指向下一个待解析的单词的起始字符
    public String token;
    public TokenType type;
    public int line;
    public int number;
    // 关键字
    public Map<String, TokenType> keywords;

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
        line = 1;
        int i = 0;
        int contentLength = source.length();

        while (i < contentLength) {
            char currentChar = source.charAt(i);
            char nextChar = '\0';
            if (i + 1 < contentLength) {
                nextChar = source.charAt(i + 1);
            }

            switch (currentChar) {
                case '\n':
                    line++;
                    break;
                case '_':
                case 'a': case 'b': case 'c': case 'd': case 'e':
                case 'f': case 'g': case 'h': case 'i': case 'j':
                case 'k': case 'l': case 'm': case 'n': case 'o':
                case 'p': case 'q': case 'r': case 's': case 't':
                case 'u': case 'v': case 'w': case 'x': case 'y':
                case 'z':
                case 'A': case 'B': case 'C': case 'D': case 'E':
                case 'F': case 'G': case 'H': case 'I': case 'J':
                case 'K': case 'L': case 'M': case 'N': case 'O':
                case 'P': case 'Q': case 'R': case 'S': case 'T':
                case 'U': case 'V': case 'W': case 'X': case 'Y':
                case 'Z':
                    i = handleIdentifierOrKeyword(source, i);
                    break;
                case '0': case '1': case '2': case '3': case '4':
                case '5': case '6': case '7': case '8': case '9':
                    i = handleNumber(source, i);
                    break;
                case '\"':
                    i = handleString(source, i);
                    break;
                case '/':
                    i = handleCommentOrDivide(source, i, nextChar);
                    break;
                default:
                    i = handleSymbolsAndOperators(source, i, nextChar);
                    break;
            }

            i++;
        }
    }

    public int handleIdentifierOrKeyword(String source, int startIndex) {
        int i = startIndex;
        StringBuilder identifier = new StringBuilder();

        while (i < source.length() && (Character.isLetterOrDigit(source.charAt(i)) || source.charAt(i) == '_')) {
            identifier.append(source.charAt(i));
            i++;
        }

        tokens.add(new Token(keywords.getOrDefault(identifier.toString(), TokenType.IDENFR), line, identifier.toString()));

        return i - 1;
    }

    public int handleNumber(String source, int startIndex) {
        int i = startIndex;
        StringBuilder number = new StringBuilder();

        while (i < source.length() && Character.isDigit(source.charAt(i))) {
            number.append(source.charAt(i));
            i++;
        }

        tokens.add(new Token(TokenType.INTCON, line, number.toString()));

        return i - 1;
    }

    public int handleString(String source, int startIndex) {
        int i = startIndex + 1; // Start right after the opening "
        StringBuilder s = new StringBuilder("\"");

        while (i < source.length()) {
            char currentChar = source.charAt(i);

            if (currentChar == '\"') { // String ending quote
                s.append(currentChar);
                break;
            }

            if (currentChar == '\n' || currentChar == 37) {
                // Handle invalid characters and format chars as in the original code
                // TODO: Implement any error handling or logging you may need
            }

            // Handle escape sequences (assuming only "\n" is valid as in the original)
            if (currentChar == 92 && (i + 1 == source.length() || source.charAt(i + 1) != 'n')) {
                // TODO: Handle the error as in the original code
            }

            s.append(currentChar);
            i++;
        }

        tokens.add(new Token(TokenType.STRCON, line, s.toString()));
        return i;
    }

    public int handleCommentOrDivide(String source, int startIndex, char nextChar) {
        int i = startIndex;

        if (nextChar == '/') {
            i = source.indexOf('\n', i + 2);
            if (i == -1) {
                i = source.length() - 1;
            }
        } else if (nextChar == '*') {
            for (i = i + 2; i < source.length(); i++) {
                if (source.charAt(i) == '*' && (i + 1 < source.length() && source.charAt(i + 1) == '/')) {
                    i++;
                    break;
                }
                if (source.charAt(i) == '\n') {
                    line++;
                }
            }
        } else {
            tokens.add(new Token(TokenType.DIV, line, "/"));
        }

        return i;
    }

    public int handleSymbolsAndOperators(String source, int startIndex, char nextChar) {
        int i = startIndex;
        char currentChar = source.charAt(i);

        switch (currentChar) {
            case '!':
                if (nextChar == '=') {
                    tokens.add(new Token(TokenType.NEQ, line, "!="));
                    i++;
                } else {
                    tokens.add(new Token(TokenType.NOT, line, "!"));
                }
                break;
            case '&':
                if (nextChar == '&') {
                    tokens.add(new Token(TokenType.AND, line, "&&"));
                    i++;
                }
                break;
            case '|':
                if (nextChar == '|') {
                    tokens.add(new Token(TokenType.OR, line, "||"));
                    i++;
                }
                break;
            case '+':
                tokens.add(new Token(TokenType.PLUS, line, "+"));
                break;
            case '-':
                tokens.add(new Token(TokenType.MINU, line, "-"));
                break;
            case '*':
                tokens.add(new Token(TokenType.MULT, line, "*"));
                break;
            case '%':
                tokens.add(new Token(TokenType.MOD, line, "%"));
                break;
            case '<':
                if (nextChar == '=') {
                    tokens.add(new Token(TokenType.LEQ, line, "<="));
                    i++;
                } else {
                    tokens.add(new Token(TokenType.LSS, line, "<"));
                }
                break;
            case '>':
                if (nextChar == '=') {
                    tokens.add(new Token(TokenType.GEQ, line, ">="));
                    i++;
                } else {
                    tokens.add(new Token(TokenType.GRE, line, ">"));
                }
                break;
            case '=':
                if (nextChar == '=') {
                    tokens.add(new Token(TokenType.EQL, line, "=="));
                    i++;
                } else {
                    tokens.add(new Token(TokenType.ASSIGN, line, "="));
                }
                break;
            case ';':
                tokens.add(new Token(TokenType.SEMICN, line, ";"));
                break;
            case ',':
                tokens.add(new Token(TokenType.COMMA, line, ","));
                break;
            case '(':
                tokens.add(new Token(TokenType.LPARENT, line, "("));
                break;
            case ')':
                tokens.add(new Token(TokenType.RPARENT, line, ")"));
                break;
            case '[':
                tokens.add(new Token(TokenType.LBRACK, line, "["));
                break;
            case ']':
                tokens.add(new Token(TokenType.RBRACK, line, "]"));
                break;
            case '{':
                tokens.add(new Token(TokenType.LBRACE, line, "{"));
                break;
            case '}':
                tokens.add(new Token(TokenType.RBRACE, line, "}"));
                break;
            default:
                // TODO: Handle unexpected characters if necessary
                break;
        }

        return i;
    }



    public void printLexAns() {
        for (Token token : tokens) {
            IOUtils.write(token.toString());

        }
    }



}











