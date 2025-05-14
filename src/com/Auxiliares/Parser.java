package com.Auxiliares;

import java.io.IOException;
import com.Erro.LexerException;

public class Parser {

  // Atributos
  private AnalisadorLexico analisadorLexico;
  private Token token;
  private String mensagemErro = "";

  public Parser(AnalisadorLexico analisadorLexico) {
    this.analisadorLexico = analisadorLexico;
  }

  public boolean parse() throws IOException, LexerException {
    boolean resultado;

    token = analisadorLexico.pegarProximoToken();
    resultado = SE();

    if (resultado && token.getTipo() != Token.EOF) {
      System.err.println("\n Fim de arquivo esperado, token = " + token);
      resultado = false;
    }

    if (!resultado) {
      mensagemErro = "Token nao esperado: " + token;
    }

    return resultado;
  }

  public String errorMessage() {
    return mensagemErro;
  }

  private boolean verificarTiposAritmeticos(Token token1, Token token2) throws LexerException {
    boolean valido = (ehOperandoNumerico(token1) && ehOperandoNumerico(token2));

    if (!valido) {
      throw new LexerException("Operacao aritmetica invalida entre " +
              token1.toString() + " e " + token2.toString());
    }
    return true;
  }

  private boolean ehOperandoNumerico(Token token) {
    if (token.getTipo() == Token.LITERALNUMERICO || token.getTipo() == Token.ID || token.getTipo() == Token.FLOAT) {
      Object valor = token.getValor();
      return !("true".equalsIgnoreCase(valor.toString()) || "false".equalsIgnoreCase(valor.toString()));
    }
    return false;
  }

  private boolean SE() throws IOException, LexerException {
    boolean resultado;

    System.out.print("SE[ if ");

    if (match(Token.IF)) {
      if (match(Token.PONTUACAO, Token.AP)) {
        resultado = LE() && match(Token.PONTUACAO, Token.FP);

        if (match(Token.PONTUACAO, Token.AC)) {
          resultado = comandos() && match(Token.PONTUACAO, Token.FC);
        } else {
          resultado = false;
        }
      } else {
        resultado = false;
      }
    } else {
      resultado = false;
    }

    System.out.print(" ]se");
    return resultado;
  }

  private boolean CHAVES() throws IOException, LexerException {
    boolean resultado;

    System.out.print("Chaves[ { ");

    if (match(Token.PONTUACAO, Token.AC)) {
      resultado = comandos() && match(Token.PONTUACAO, Token.FC);
    } else if (comando()) {
      resultado = true;
    } else {
      resultado = false;
    }

    System.out.print(" ]CHAVES");
    return resultado;
  }

  private boolean comando() throws IOException, LexerException {
    boolean resultado;

    System.out.print("comando[ { ");

    resultado = match(Token.ID) && match(Token.AT) && LE();

    System.out.print(" ]CHAVES");
    return resultado;
  }

  private boolean comandos() throws IOException, LexerException {
    return true;
  }

  private boolean LE() throws IOException, LexerException {
    boolean resultado;

    System.out.print("LE[ ");

    if (!RE()) {
      return false;
    }

    if (match(Token.LOG, Token.AND) || match(Token.LOG, Token.OR)) {
      resultado = LE();
    } else {
      System.out.print(" ?");
      resultado = true;
    }

    System.out.print(" ]le");
    return resultado;
  }

  private boolean RE() throws IOException, LexerException {
    boolean resultado;

    System.out.print("RE[ ");

    if (!AE()) {
      return false;
    }

    if (match(Token.RELOP, Token.GT) || match(Token.RELOP, Token.GE) ||
            match(Token.RELOP, Token.LT) || match(Token.RELOP, Token.LE) ||
            match(Token.RELOP, Token.EQ)) {
      if (!AE()) return false;
    }

    System.out.print(" ]re");
    return true;
  }

  private boolean AE() throws IOException, LexerException {
    boolean resultado;
    System.out.print("AE[ ");

    Token operando1 = token;
    if (!ME()) return false;

    while (token.getTipo() == Token.OP && ((Integer) token.getValor() == Token.AD || (Integer) token.getValor() == Token.SUB)) {
      int operador = (Integer) token.getValor();
      match(Token.OP, operador);

      Token operando2 = token;
      if (!ME()) return false;

      if (!verificarTiposAritmeticos(operando1, operando2)) return false;

      operando1 = operando2;
    }

    System.out.print(" ]ae");
    return true;
  }

  private boolean ME() throws LexerException, IOException {
    Token operando1 = token;
    if (!UE()) return false;

    while (token.getTipo() == Token.OP &&
            ((Integer) token.getValor() == Token.MUL ||
                    (Integer) token.getValor() == Token.DIV)){

      int operador = (Integer) token.getValor();
      match(Token.OP, operador);

      Token operando2 = token;
      if (!UE()) return false;

      if (!verificarTiposAritmeticos(operando1, operando2)) return false;

      operando1 = operando2;
    }

    return true;
  }

  private boolean UE() throws IOException, LexerException {
    boolean resultado;
    System.out.print("UE[ ");

    if (match(Token.PONTUACAO, Token.AP)) {
      resultado = LE() && match(Token.PONTUACAO, Token.FP);
    } else if (match(Token.ID) || match(Token.LITERALNUMERICO) || match(Token.TRUE) || match(Token.FALSE) || match(Token.LITERALSTRING) || match(Token.FLOAT)) {
      resultado = true;
    } else if (match(Token.OP, Token.AD) || match(Token.OP, Token.SUB) || match(Token.LOG, Token.NOT)) {
      resultado = UE();
    } else {
      resultado = false;
    }

    System.out.print(" ]ue ");
    return resultado;
  }

  private boolean match(int tipoToken) throws IOException, LexerException {
    boolean resultado;

    if (token.getTipo() == tipoToken) {
      imprimirToken(token);
      token = analisadorLexico.pegarProximoToken();
      resultado = true;
    } else {
      resultado = false;
    }

    return resultado;
  }

  private boolean match(int tipoToken, int valorToken) throws IOException, LexerException {
    boolean resultado;

    if (token.getTipo() == tipoToken && (Integer) token.getValor() == valorToken) {
      imprimirToken(token);
      token = analisadorLexico.pegarProximoToken();
      resultado = true;
    } else {
      resultado = false;
    }

    return resultado;
  }

  private void imprimirToken(Token token) {
    switch (token.getTipo()) {
      case Token.ID -> System.out.print("Id");
      case Token.LITERALNUMERICO -> System.out.print(token.getValor());
      case Token.TRUE -> System.out.print("true");
      case Token.FALSE -> System.out.print("false");
      case Token.OP -> {
        switch ((Integer) token.getValor()) {
          case Token.AD -> System.out.print("+");
          case Token.SUB -> System.out.print("-");
          case Token.MUL -> System.out.print("*");
          case Token.DIV -> System.out.print("/");
        }
      }
      case Token.LOG -> {
        switch ((Integer) token.getValor()) {
          case Token.AND -> System.out.print("&&");
          case Token.OR -> System.out.print("||");
          case Token.NOT -> System.out.print("!");
        }
      }
      case Token.RELOP -> {
        switch ((Integer) token.getValor()) {
          case Token.GT -> System.out.print(">");
          case Token.GE -> System.out.print(">=");
          case Token.LT -> System.out.print("<");
          case Token.LE -> System.out.print("<=");
          case Token.EQ -> System.out.print("==");
        }
      }
      case Token.PONTUACAO -> {
        switch ((Integer) token.getValor()) {
          case Token.AP -> System.out.print("(");
          case Token.FP -> System.out.print(")");
          case Token.AC -> System.out.print("{");
          case Token.FC -> System.out.print("}");
        }
      }
    }
  }
}