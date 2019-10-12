// Abstract syntax trees
import java.util.*;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public abstract class Ast {
  public static abstract class Source {
    public static enum Error {
      UndefinedTypeException, UndeclaredVarException, InvalidTypeException,
      UninitializedVarException;
    }
    public static void raise(Error error) {
      System.out.println(error.toString());
      System.exit(1);
    }

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

      public String getInstr() {
        switch(this) {
          case Add: return "addq";
          case Subtract: return "subq";
          case Multiply: return "imulq";
          case Divide:
          case Modulus: return "idivq";
          case BitAnd: return "andq";
          case BitOr: return "orq";
          case BitXor: return  "xorq";
          case Lshift: return "salq";
          case Rshift: return "sarq";
          default: throw new IllegalArgumentException();
        }
      }
    }

    public static enum Unop {
      Negate, BitNot, BoolNot;

      public String getInstr() {
        switch(this) {
          case Negate: return "negq";
          case BoolNot:
          case BitNot: return "notq";
          default: throw new IllegalArgumentException();
        }
      }
    }

    public static enum BoolOp {
      And, Or;
    }

    public static enum CompOp {
      Eq, Neq, Lt, Leq, Gt, Geq;
    }

    public static abstract class Expr {
      protected Type type = null;

      public Type getType() {
        if (this.type == null) {
          raise(Error.UndefinedTypeException);
        }
        return this.type;
      }

      public void setType(Type type) {
        if (this.type != null && !this.type.equals(type)) {
          raise(Error.InvalidTypeException);
        }
        this.type = type;
      }

      public abstract Type typeCheck(Map<String,VarDecl> vars);

      public static final class IntImm extends Expr {
        public final int value;
        public IntImm(int value) {
          this.value = value;
          this.type = Types.int64;
        }
        @Override
        public Type typeCheck(Map<String,VarDecl> vars) {
          return this.type;
        }
        @Override
        public String toString() {
          return Integer.toString(this.value);
        }
      }
      public static final class BoolImm extends Expr {
        public final boolean isTrue;
        public BoolImm(boolean isTrue) {
          this.isTrue = isTrue;
          this.type = Types.bool;
        }
        @Override
        public Type typeCheck(Map<String,VarDecl> vars) {
          return this.type;
        }
        @Override
        public String toString() {
          return Boolean.toString(this.isTrue);
        }
      }
      public static final class Read extends Expr {
        public final Dest dest;
        public Read(Dest dest) {
          this.dest = dest;
        }
        @Override
        public Type typeCheck(Map<String,VarDecl> vars) {
          if (!vars.containsKey(this.dest)) {
            raise(Error.UndeclaredVarException);
          }
          VarDecl varDecl = vars.get(this.dest);
          this.type = varDecl.type;
          return this.type;
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
          this.type = op == Unop.BoolNot ? Types.bool : Types.int64;
        }
        @Override
        public Type typeCheck(Map<String,VarDecl> vars) {
          Type argType = arg.typeCheck(vars);
          if (!argType.equals(this.type)) {
            raise(Error.InvalidTypeException);
          }
          return this.type;
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
          this.type = Types.int64;
        }
        @Override
        public Type typeCheck(Map<String,VarDecl> vars) {
          Type leftType = leftArg.typeCheck(vars);
          Type rightType = rightArg.typeCheck(vars);
          if (!leftType.equals(Types.int64)
              || !rightType.equals(Types.int64)) {
            raise(Error.InvalidTypeException);
          }
          return this.type;
        }
        @Override
        public String toString() {
          return String.format("(%s, %s, %s)",
                               this.leftArg.toString(),
                               this.op.toString(),
                               this.rightArg.toString());
        }
      }
      public static final class BoolOpApp extends Expr {
        public final BoolOp op;
        public final Expr leftArg, rightArg;
        public BoolOpApp(Expr leftArg, BoolOp op, Expr rightArg) {
          this.op = op;
          this.leftArg = leftArg;
          this.rightArg = rightArg;
          this.type = Types.bool;
        }
        @Override
        public Type typeCheck(Map<String,VarDecl> vars) {
          Type leftType = leftArg.typeCheck(vars);
          Type rightType = rightArg.typeCheck(vars);
          if (!leftType.equals(Types.bool)
              || !rightType.equals(Types.bool)) {
            raise(Error.InvalidTypeException);
          }
          return this.type;
        }
        @Override
        public String toString() {
          return String.format("(%s, %s, %s)", this.op.toString(),
            this.leftArg.toString(), this.rightArg.toString());
        }
      }
      public static final class Comp extends Expr {
        public final CompOp op;
        public final Expr leftArg, rightArg;
        public Comp(Expr leftArg, CompOp op, Expr rightArg) {
          this.op = op;
          this.leftArg = leftArg;
          this.rightArg = rightArg;
          this.type = Types.bool;
        }
        @Override
        public Type typeCheck(Map<String,VarDecl> vars) {
          Type leftType = leftArg.typeCheck(vars);
          Type rightType = rightArg.typeCheck(vars);
          if (!leftType.equals(rightType)
              || ((op != CompOp.Eq || op != CompOp.Neq) && !leftType.equals(Types.int64))) {
            raise(Error.InvalidTypeException);
          }
          return this.type;
        }
        @Override
        public String toString() {
          return String.format("(%s, %s, %s)", this.op.toString(),
            this.leftArg.toString(), this.rightArg.toString());
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
      public static final class IfElse extends Stmt {
        public final Expr condition;
        public final List<Stmt> thenBranch;
        public final List<Stmt> elseBranch;
        public IfElse(Expr condition, List<Stmt> thenBranch,
          List<Stmt> elseBranch) {
          this.condition = condition;
          this.thenBranch = thenBranch;
          this.elseBranch = elseBranch;
        }
        @Override
        public String toString() {
          String ifBlock = String.format("if (%s) then (%s)",
          this.condition.toString(), this.thenBranch.toString());
          String elseBlock = this.elseBranch == null
            ? "" : String.format(" else (%s)", this.elseBranch.toString());
          return ifBlock + elseBlock;
        }
      }
      public static final class While extends Stmt {
        public final Expr condition;
        public final List<Stmt> body;
        public While(Expr condition, List<Stmt> body) {
          this.condition = condition;
          this.body = body;
        }
        @Override
        public String toString() {
          return String.format("while (%s) do (%s)",
          this.condition, this.body.toString());
        }
      }
      public static final class Block extends Stmt {
        public final List<Stmt> stmts;
        public Block() {
          this.stmts = new ArrayList<Stmt>();
        }
        public void addStmt(Stmt stmt) {
          stmts.add(stmt);
        }
      }
    } // Stmt

    public static class VarDecl {
      public final Type type;
      public Expr initialValue;
      public final int order;
      public VarDecl(Type type, Expr initialValue, int order) {
        this.type = type;
        this.initialValue = initialValue;
        this.order = order;
      }
      @Override public String toString() {
        return String.format("(%s, %s, %d)",
          type.toString(), initialValue == null ? "NO_INIT" : initialValue.toString(),
          order);
      }
    }

    public static abstract class Type {
      public abstract int getStorageSize();
    }

    public static class BasicType extends Type {
      public final String label;
      public final int size;
      public BasicType(String label, int size) {
        this.label = label;
        this.size = size;
      }
      @Override public int getStorageSize() {
        return Math.max(this.size, 8);
      }
      @Override public String toString() {
        return label;
      }
    }

    public static class Types {
      public static final BasicType int64 = new BasicType("int64", 64);
      public static final BasicType bool = new BasicType("bool", 1);
    }

    public static class Prog {
      public final Map<String,VarDecl> vars;
      public final List<Stmt> statements;
      public Prog(List<Stmt> statements, Map<String, VarDecl> vars) {
        this.statements = statements;
        this.vars = vars;
      }
      @Override
      public String toString() {
        String str = "";
        for (Map.Entry<String,VarDecl> varEntry : vars.entrySet())   {
          str += varEntry.getKey() + " -> " + varEntry.getValue().toString() + "\n";
        }
        for (Stmt stmt : this.statements)
          str += stmt.toString() + ";\n";
        return str;
      }
    }

    // Source code parser
    public static class SourceCreator extends BX0BaseListener {
      private Stack<Ast.Source.Stmt> stmts = new Stack<>();
      private Map<String, VarDecl> vars = new HashMap<>();
      private Stack<Expr> exprs = new Stack<>();
      private Prog prog = null;
      private int varCounter = 0;

      @Override
      public void exitProgram(BX0Parser.ProgramContext ctx) {
        this.prog = new Prog(stmts, vars);
      }

      @Override
      public void exitVarinit(BX0Parser.VarinitContext ctx) {
        String var = ctx.getChild(0).getText();
        Type type = ((BX0Parser.VardeclContext) ctx.getParent()).type()
          .getText().equals("int64") ? Types.int64 : Types.bool;
        Expr initialValue = null;
        if (ctx.expr() != null) {
          initialValue = this.exprs.pop();
        }
        vars.put(var, new VarDecl(type, initialValue, varCounter++));
      }

      @Override
      public void exitMove(BX0Parser.MoveContext ctx) {
        Dest dest = new Dest(ctx.getChild(0).getText());
        Expr source = this.exprs.pop();
        stmts.push(new Stmt.Move(dest, source));
      }

      @Override
      public void exitBlock(BX0Parser.BlockContext ctx) {
        Stmt.Block block = new Stmt.Block();
        int blockSize = ctx.statement().size();
        while (blockSize != 0) {
          block.addStmt(stmts.pop());
          blockSize -= 1;
        }
        stmts.push(block);
      }

      @Override
      public void exitIfelse(BX0Parser.IfelseContext ctx) {
        List<Stmt> elseBranch = null;
        if (ctx.ifelse() != null || ctx.block(1) != null) {
          elseBranch = ((Stmt.Block) stmts.pop()).stmts;
        }
        List<Stmt> thenBranch = ((Stmt.Block) stmts.pop()).stmts;
        Expr condition = exprs.pop();
        stmts.push(new Stmt.IfElse(condition, thenBranch, elseBranch));
      }

      @Override
      public void exitWhileloop(BX0Parser.WhileloopContext ctx) {
        Expr condition = exprs.pop();
        List<Stmt> body = ((Stmt.Block) stmts.pop()).stmts;
        stmts.push(new Stmt.While(condition, body));
      }

      @Override
      public void exitPrint(BX0Parser.PrintContext ctx) {
        Expr arg = this.exprs.pop();
        stmts.push(new Stmt.Print(arg));
      }

      @Override
      public void exitUnop(BX0Parser.UnopContext ctx) {
        Unop op = ctx.op.getText().equals("-") ?
          Unop.Negate :  ctx.op.getText().equals("~") ? Unop.BitNot : Unop.BoolNot;
        Expr arg = this.exprs.pop();
        Expr expr = new Expr.UnopApp(op, arg);
        this.exprs.push(expr);
      }

      private void processBinop(Binop op) {
        Expr right = this.exprs.pop();
        Expr left = this.exprs.pop();
        Expr expr = new Expr.BinopApp(left, op, right);
        this.exprs.push(expr);
      }

      private void processBoolOp(BoolOp op) {
        Expr right = this.exprs.pop();
        Expr left = this.exprs.pop();
        Expr expr = new Expr.BoolOpApp(left, op, right);
        this.exprs.push(expr);
      }

      private void processComp(CompOp op) {
        Expr right = this.exprs.pop();
        Expr left = this.exprs.pop();
        Expr expr = new Expr.Comp(left, op, right);
        this.exprs.push(expr);
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
      public void exitBoolop(BX0Parser.BoolopContext ctx) {
        String opText = ctx.op.getText();
        BoolOp op = opText.equals("&&")
          ? BoolOp.And : BoolOp.Or;
        this.processBoolOp(op);
      }

      @Override
      public void exitEq(BX0Parser.EqContext ctx) {
        String opText = ctx.op.getText();
        CompOp op = opText.equals("==")
          ? CompOp.Eq : CompOp.Neq;
        this.processComp(op);
      }

      @Override
      public void exitLess(BX0Parser.LessContext ctx) {
        String opText = ctx.op.getText();
        CompOp op = opText.equals("<")
          ? CompOp.Lt : CompOp.Leq;
        this.processComp(op);
      }

      @Override
      public void exitGreater(BX0Parser.GreaterContext ctx) {
        String opText = ctx.op.getText();
        CompOp op = opText.equals(">")
          ? CompOp.Gt : CompOp.Geq;
        this.processComp(op);
      }

      @Override
      public void exitVariable(BX0Parser.VariableContext ctx) {
        this.exprs.push(new Expr.Read(new Dest(ctx.getText())));
      }

      @Override
      public void exitNumber(BX0Parser.NumberContext ctx) {
        int num = Integer.parseInt(ctx.getText());
        this.exprs.push(new Expr.IntImm(num));
      }

      @Override
      public void exitBoolean(BX0Parser.BooleanContext ctx) {
        boolean bool = Boolean.parseBoolean(ctx.getText());
        this.exprs.push(new Expr.BoolImm(bool));
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
      public abstract String toAmd64();
      public abstract String toRtl();
      public int inLabel = 0;

      public static class MoveImm extends Instr {
        public final Dest dest;
        public final int imm;
        public final int outLabel;
        public MoveImm(int inLabel, Dest dest, int imm, int outLabel) {
          this.dest = dest;
          this.imm = imm;
          this.inLabel = inLabel;
          this.outLabel = outLabel;
        }
        @Override
        public String toRtl() {
          return String.format("L%d: move %d #%dq --> L%d",
            this.inLabel, this.imm, this.dest.loc, this.outLabel);
        }
        @Override
        public String toAmd64() {
          return String.format("movq $%d, %s", this.imm, getStackSlot(dest));
        }
      }

      public static class MoveCp extends Instr {
        public final Dest dest, source;
        public final int outLabel;
        public MoveCp(int inLabel, Dest dest, Dest source, int outLabel) {
          this.dest = dest;
          this.source = source;
          this.inLabel = inLabel;
          this.outLabel = outLabel;
        }
        @Override
        public String toRtl() {
          return String.format("L%d: copy #%dq #%dq --> L%d",
            this.inLabel, this.source.loc, this.dest.loc, this.outLabel);
        }
        @Override
        public String toAmd64() {
          return String.format("movq %s, %%r11\n\tmovq %%r11, %s",
            getStackSlot(source), getStackSlot(dest));
        }
      }

      public static class MoveBinop extends Instr {
        public final Dest dest, leftArg, rightArg;
        public final Ast.Source.Binop op;
        public final int outLabel;
        public MoveBinop(int inLabel, Dest dest, Dest leftArg, Ast.Source.Binop op,
          Dest rightArg, int outLabel) {
          this.dest = dest;
          this.leftArg = leftArg;
          this.rightArg = rightArg;
          this.op = op;
          // in and out labels should be 3 apart
          this.inLabel = inLabel;
          this.outLabel = outLabel;
        }
        @Override
        public String toRtl() {
          return String.format("L%d: copy #%dq ## --> L%d\nL%d: binop %s #%dq ## --> L%d\nL%d: copy ## #%dq --> L%d",
            this.inLabel, this.leftArg.loc, this.inLabel + 1, this.inLabel + 1,
            this.op.toString(), this.rightArg.loc, this.inLabel + 2, this.inLabel + 2,
            this.dest.loc, this.outLabel);
        }
        @Override
        public String toAmd64() {
          switch (op) {
            case Add:
            case Subtract:
            case BitAnd:
            case BitOr:
            case BitXor:
              return String.format("movq %s, %%r11\n\t%s %s, %%r11\n\tmovq %%r11, %s",
                getStackSlot(leftArg), op.getInstr(), getStackSlot(rightArg),
                getStackSlot(dest));
            case Multiply:
              return String.format("movq %s, %%rax\n\t%s %s\n\tmovq %%rax, %s",
                getStackSlot(leftArg), op.getInstr(), getStackSlot(rightArg),
                getStackSlot(dest));
            case Divide:
            case Modulus:
              return String.format("movq %s, %%rax\n\tcqto\n\t%s %s\n\tmovq %s, %s",
                getStackSlot(leftArg), op.getInstr(), getStackSlot(rightArg),
                op == Ast.Source.Binop.Divide ? "%rax" : "%rdx", getStackSlot(dest));
            case Lshift:
            case Rshift:
              return String.format("movb %s, %%cl\n\tmovq %s, %%r11\n\t%s %%cl, %%r11\n\tmovq %%r11, %s",
                getStackSlot(rightArg), getStackSlot(leftArg), op.getInstr(),
                getStackSlot(dest));
            default: throw new IllegalArgumentException();
          }
        }
      }

      public static class MoveUnop extends Instr {
        public final Dest dest, arg;
        public final Ast.Source.Unop op;
        public final int outLabel;
        public MoveUnop(int inLabel, Dest dest, Ast.Source.Unop op, Dest arg,
          int outLabel) {
          this.dest = dest;
          this.arg = arg;
          this.op = op;
          // in and out labels should be 3 apart
          this.inLabel = inLabel;
          this.outLabel = outLabel;
        }
        @Override
        public String toRtl() {
          return String.format("L%d: copy #%dq ## --> L%d\nL%d: unop %s ## --> L%d\nL%d: copy ## #%dq --> L%d",
            this.inLabel, this.arg.loc, this.inLabel + 1, this.inLabel + 1,
            this.op.toString(), this.inLabel + 2, this.inLabel + 2,
            this.dest.loc, this.outLabel);
        }
        @Override
        public String toAmd64() {
          return String.format("movq %s, %%r11\n\t%s %%r11\n\tmovq %%r11, %s",
            getStackSlot(arg), op.getInstr(), getStackSlot(dest));
        }
      }

      public static class UBranch extends Instr {
        public final Dest arg;
        public final Ast.Source.CompOp op;
        public final int trueOutLabel, falseOutLabel;
        public UBranch(int inLabel, Ast.Source.CompOp op, Dest arg,
          int trueOutLabel, int falseOutLabel) {
          this.op = op;
          this.arg = arg;
          this.trueOutLabel, this.falseOutLabel = trueOutLabel, falseOutLabel;
        }
        @Override
        public String toRtl() {
          return String.format("L%d: ubranch %s #%dq --> L%d, L%d",
            this.inLabel, op.toString(), arg.loc, this.trueOutLabel,
            this.falseOutLabel);
        }
        @Override
        public String toAmd64() {
          return String.format("AMD UBRANCH");
        }
      }

      public static class BBranch extends Instr {
        public final Dest leftArg, rightArg;
        public final Ast.Source.CompOp op;
        public final int trueOutLabel, falseOutLabel;
        public BBranch(int inLabel, Dest leftArg, Ast.Source.CompOp op,
          Dest rightArg, int trueOutLabel, int falseOutLabel) {
          this.leftArg = leftArg;
          this.rightArg = rightArg;
          this.op = op;
          this.trueOutLabel, this.falseOutLabel = trueOutLabel, falseOutLabel;
        }
        @Override
        public String toRtl() {
          return String.format("L%d: bbranch %s #%dq #%dq --> L%d, L%d",
            this.inLabel, op.toString(), leftArg.loc, rightArg.loc, this.trueOutLabel,
            this.falseOutLabel));
        }
        @Override
        public String toAmd64() {
          return String.format("AMD BBRANCH");
        }
      }

      public static class Goto extends Instr {
        public final int outLabel;
        public Goto(int inLabel, int outLabel) {
          this.inLabel = inLabel;
          this.outLabel = outLabel;
        }
        @Override
        public String toRtl() {
          return String.format("L%d: goto --> L%d", this.inLabel, this.outLabel);
        }
        @Override
        public String toAmd64() {
          return String.format("AMD GOTO");
        }
      }

      public static class Print extends Instr {
        public final Dest dest;
        public final int outLabel;
        public Print(int inLabel, Dest dest, int outLabel) {
          this.dest = dest;
          this.inLabel = inLabel;
          this.outLabel = outLabel;
        }
        @Override
        public String toRtl() {
          return String.format("L%d: call bx1_print(#%dq), ## --> L%d",
            this.inLabel, this.dest.loc, this.outLabel);
        }
        @Override
        public String toAmd64() {
          return String.format("movq %s, %%rdi\n\tcallq bx0_print\n",
            getStackSlot(dest));
        }
      }

      /** This class can be used to embed comments in the generated
          assembly. This is useful for debugging. */
      public static class Comment extends Instr {
        public final String comment;
        public Comment(String comment) {
          this.comment = comment;
        }
        @Override
        public String toRtl() {
          return String.format("; %s", this.comment);
        }
        @Override
        public String toAmd64() {
          return String.format("# %s", this.comment);
        }
      }
    } // Instr

    public static class Prog {
      public final List<Instr> instructions;
      public Prog(List<Instr> instructions) {
        this.instructions = instructions;
      }
    }

    private static String getStackSlot(Dest dest) {
      return dest.loc == 0 ? "(%rsp)" : (dest.loc * 8) + "(%rsp)";
    }
  }
}
