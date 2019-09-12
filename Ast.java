// Abstract syntax trees

import java.util.*;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public abstract class Ast {
  public static abstract class Source {
    public static class Dest {
      public final String var;
      public Dest(String var) {
        this.var = var;
      }
      @Override
      public String toString() {
        return this.var;
      }
    }

    public static enum Binop {
      Add, Subtract, Multiply, Divide, Modulus,
      BitAnd, BitOr, BitXor, Lshift, Rshift;
    }

    public static enum Unop {
      Negate, BitNot;
    }

    public static abstract class Expr {
      public static final class Immediate extends Expr {
        public final int value;
        public Immediate(int value) {
          this.value = value;
        }
        @Override
        public String toString() {
          return Integer.toString(this.value);
        }
      }
      public static final class Read extends Expr {
        public final Dest dest;
        public Read(Dest dest) {
          this.dest = dest;
        }
        @Override
        public String toString() {
          return this.dest.toString();
        }
      }
      public static final class UnopApp extends Expr {
        public final Unop op;
        public final Expr arg;
        public UnopApp(Unop op, Expr arg) {
          this.op = op;
          this.arg = arg;
        }
        @Override
        public String toString() {
          return String.format("(%s, %s)", this.op.toString(), this.arg.toString());
        }
      }
      public static final class BinopApp extends Expr {
        public final Binop op;
        public final Expr leftArg, rightArg;
        public BinopApp(Expr leftArg, Binop op, Expr rightArg) {
          this.op = op;
          this.leftArg = leftArg;
          this.rightArg = rightArg;
        }
        @Override
        public String toString() {
          return String.format("(%s, %s, %s)",
                               this.leftArg.toString(),
                               this.op.toString(),
                               this.rightArg.toString());
        }
      }
    } // Expr

    public static abstract class Stmt {
      public static final class Move extends Stmt {
        public final Dest dest;
        public final Expr source;
        public Move(Dest dest, Expr source) {
          this.dest = dest;
          this.source = source;
        }
        @Override
        public String toString() {
          return String.format("%s = %s", dest.toString(), source.toString());
        }
      }
      public static final class Print extends Stmt {
        public final Expr arg;
        public Print(Expr arg) {
          this.arg = arg;
        }
        @Override
        public String toString() {
          return String.format("print %s", arg.toString());
        }
      }
    } // Stmt

    public static class Prog {
      public final List<Stmt> statements;
      public Prog(List<Stmt> statements) {
        this.statements = statements;
      }
      @Override
      public String toString() {
        String str = "";
        for (Stmt stmt : this.statements)
          str += stmt.toString() + ";\n";
        return str;
      }
    }

    private static class SourceCreator extends BX0BaseListener {
      private List<Stmt> stmts = new ArrayList<>();
      private Stack<Expr> exprStack = new Stack<>();
      private Prog prog = null;

      @Override
      public void exitProgram(BX0Parser.ProgramContext ctx) {
        this.prog = new Prog(this.stmts);
      }

      @Override
      public void exitMove(BX0Parser.MoveContext ctx) {
        Dest dest = new Dest(ctx.getChild(0).getText());
        Expr source = this.exprStack.pop();
        this.stmts.add(new Stmt.Move(dest, source));
      }

      @Override
      public void exitPrint(BX0Parser.PrintContext ctx) {
        Expr arg = this.exprStack.pop();
        this.stmts.add(new Stmt.Print(arg));
      }

      @Override
      public void exitUnop(BX0Parser.UnopContext ctx) {
        Unop op = ctx.op.getText().equals("-") ? Unop.Negate : Unop.BitNot;
        Expr arg = this.exprStack.pop();
        Expr expr = new Expr.UnopApp(op, arg);
        this.exprStack.push(expr);
      }

      private void processBinop(Binop op) {
        Expr right = this.exprStack.pop();
        Expr left = this.exprStack.pop();
        Expr expr = new Expr.BinopApp(left, op, right);
        this.exprStack.push(expr);
      }

      @Override
      public void exitAdd(BX0Parser.AddContext ctx) {
        this.processBinop(ctx.op.getText().equals("+") ? Binop.Add : Binop.Subtract);
      }

      @Override
      public void exitMul(BX0Parser.MulContext ctx) {
        String opText = ctx.op.getText();
        Binop op = opText.equals("*") ? Binop.Multiply :
          opText.equals("/") ? Binop.Divide : Binop.Modulus;
        this.processBinop(op);
      }

      @Override
      public void exitShift(BX0Parser.ShiftContext ctx) {
        String opText = ctx.op.getText();
        Binop op = opText.equals("<<") ? Binop.Lshift :
          Binop.Rshift;
        this.processBinop(op);
      }

      @Override
      public void exitAnd(BX0Parser.AndContext ctx) {
        this.processBinop(Binop.BitAnd);
      }

      @Override
      public void exitOr(BX0Parser.OrContext ctx) {
        this.processBinop(Binop.BitOr);
      }

      @Override
      public void exitXor(BX0Parser.XorContext ctx) {
        this.processBinop(Binop.BitXor);
      }

      @Override
      public void exitVariable(BX0Parser.VariableContext ctx) {
        this.exprStack.push(new Expr.Read(new Dest(ctx.getText())));
      }

      @Override
      public void exitNumber(BX0Parser.NumberContext ctx) {
        int num = Integer.parseInt(ctx.getText());
        this.exprStack.push(new Expr.Immediate(num));
      }
    }

    /** Parse and return an AST for a BX0 program */
    public static Prog readProgram(String file) throws Exception {
      CharStream input = CharStreams.fromFileName(file);
      BX0Lexer lexer = new BX0Lexer(input);
      CommonTokenStream tokens = new CommonTokenStream(lexer);
      BX0Parser parser = new BX0Parser(tokens);
      BX0Parser.ProgramContext pcx = parser.program();
      ParseTreeWalker walker = new ParseTreeWalker();
      SourceCreator creator = new SourceCreator();
      walker.walk(creator, pcx);
      return creator.prog;
    }
  }

  public static class Target {

    public static class Dest {
      public final int loc;
      public Dest(int loc) {
        this.loc = loc;
      }
    }

    public static abstract class Instr {
      public static class MoveImm extends Instr {
        public final Dest dest;
        public final int imm;
        public MoveImm(Dest dest, int imm) {
          this.dest = dest;
          this.imm = imm;
        }
      }

      public static class MoveCp extends Instr {
        public final Dest dest, source;
        public MoveCp(Dest dest, Dest source) {
          this.dest = dest;
          this.source = source;
        }
      }

      public static class MoveBinop extends Instr {
        public final Dest dest;
        public final Dest leftArg, rightArg;
        public final Ast.Source.Binop op;
        public MoveBinop(Dest dest, Dest leftArg, Ast.Source.Binop op, Dest rightArg) {
          this.dest = dest;
          this.leftArg = leftArg;
          this.rightArg = rightArg;
          this.op = op;
        }
      }

      public static class MoveUnop extends Instr {
        public final Dest dest;
        public final Dest arg;
        public final Ast.Source.Unop op;
        public MoveUnop(Dest dest, Ast.Source.Unop op, Dest arg) {
          this.dest = dest;
          this.arg = arg;
          this.op = op;
        }
      }

      public static class Print extends Instr {
        public final Dest dest;
        public Print(Dest dest) {
          this.dest = dest;
        }
      }

      /** This class can be used to embed comments in the generated
          assembly. This is useful for debugging. */
      public static class Comment extends Instr {
        public final String comment;
        public Comment(String comment) {
          this.comment = comment;
        }
      }
    } // Instr

    public static class Prog {
      public final List<Instr> instructions;
      public Prog(List<Instr> instructions) {
        this.instructions = instructions;
      }
    }
  }
}
