import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main {
  private static int mVarCounter = 0;
  private static int mLabelCounter = 0;
  private static HashMap<String, Ast.Target.Dest> mVars = new HashMap<>();
  private static HashMap<Integer, Integer> mLabelChanges = new HashMap<>();
  private static List<Ast.Target.Instr> mInstrs = new ArrayList<>();

  public static void main(String[] args) throws Exception {
    for (String bxFile : args) {
      if (! bxFile.endsWith(".bx"))
        throw new RuntimeException(String.format("%s does not end in .bx", bxFile));
      Ast.Source.Prog sourceProg = Ast.Source.readProgram(bxFile);
      System.out.println(sourceProg.toString());
      int Lend = RTLstmts(sourceProg.stmts, 0);
      mInstrs.add(new Ast.Target.Instr.Return(Lend));
      Ast.Target.Prog targetProg = new Ast.Target.Prog(mInstrs);
      targetProg.replaceLabels(mLabelChanges);
      System.out.println(String.format("enter L0\nexit L%d\n----", Lend + 1));
      for (Ast.Target.Instr instr : targetProg.instructions) {
        System.out.println(instr.toRtl());
      }
      String stem = bxFile.substring(0, bxFile.length() - 3);
      String amd64File = stem + ".s";
      PrintStream out = new PrintStream(amd64File);
      out.println(String.format("\t.file \"%s\"", bxFile));
      out.println("\t.section .text");
      out.println("\t.globl main");
      out.println("main:");
      out.println("\tpushq %rbp");
      out.println("\tmovq %rsp, %rbp");
      out.println(String.format("\tsubq $%d, %%rsp\n", mVarCounter * 8));
      for (Ast.Target.Instr instr : targetProg.instructions) {
        out.println("\t" + instr.toAmd64());
      }
      out.println("\tmovq %rbp, %rsp");
      out.println("\tpopq %rbp");
      out.println("\tmovq $0, %rax");
      out.println("\tretq");
      out.close();
      String gccCmd = String.format("gcc -no-pie -o %s.exe %s bx0rt.c", stem, amd64File);
      Process gccProc = Runtime.getRuntime().exec(gccCmd);
      gccProc.waitFor();
    }
  }

  // private static void generateInstructions(Ast.Source.Prog progSource) {
  //   for (Ast.Source.Stmt stmt : progSource.stmts) {
  //     if (stmt instanceof Ast.Source.Stmt.Move) {
  //       Ast.Source.Stmt.Move move = (Ast.Source.Stmt.Move) stmt;
  //       mVars.put(move.dest.var, genInstrsFromExpr(move.source));
  //     } else if (stmt instanceof Ast.Source.Stmt.Print) {
  //       Ast.Source.Stmt.Print print = (Ast.Source.Stmt.Print) stmt;
  //       mInstrs.add(new Ast.Target.Instr.Print(genInstrsFromExpr(print.arg)));
  //     }
  //   }
  // }
  //
  // // helper function for generateInstructions
  // private static Ast.Target.Dest genInstrsFromExpr(Ast.Source.Expr expr) {
  //   Ast.Target.Dest dest = null;
  //   if (expr instanceof Ast.Source.Expr.IntImm) {
  //     Ast.Source.Expr.IntImm imm = (Ast.Source.Expr.IntImm) expr;
  //     dest = new Ast.Target.Dest(mVarCounter++);
  //     mInstrs.add(new Ast.Target.Instr.MoveImm(dest, imm.value));
  //   } else if (expr instanceof Ast.Source.Expr.Read) {
  //     Ast.Source.Expr.Read read = (Ast.Source.Expr.Read) expr;
  //     dest = mVars.get(read.dest.var);
  //   } else if (expr instanceof Ast.Source.Expr.UnopApp) {
  //     Ast.Source.Expr.UnopApp unopApp = (Ast.Source.Expr.UnopApp) expr;
  //     Ast.Target.Dest argDest = genInstrsFromExpr(unopApp.arg);
  //     dest = new Ast.Target.Dest(mVarCounter++);
  //     mInstrs.add(new Ast.Target.Instr.MoveUnop(dest, unopApp.op, argDest));
  //   } else if (expr instanceof Ast.Source.Expr.BinopApp) {
  //     Ast.Source.Expr.BinopApp binopApp = (Ast.Source.Expr.BinopApp) expr;
  //     Ast.Target.Dest leftDest = genInstrsFromExpr(binopApp.leftArg);
  //     Ast.Target.Dest rightDest = genInstrsFromExpr(binopApp.rightArg);
  //     dest = new Ast.Target.Dest(mVarCounter++);
  //     mInstrs.add(new Ast.Target.Instr.MoveBinop(
  //       dest, leftDest, binopApp.op, rightDest));
  //   }
  //   return dest;
  // }

  private static class DestLabelPair {
    public Ast.Target.Dest dest;
    public int outLabel;
    public DestLabelPair(Ast.Target.Dest dest, int outLabel) {
      this.dest = dest;
      this.outLabel = outLabel;
    }
  }

  private static class TrueFalseLabels {
    public int trueLabel;
    public int falseLabel;
    public TrueFalseLabels(int trueLabel, int falseLabel) {
      this.trueLabel = trueLabel;
      this.falseLabel = falseLabel;
    }
    public TrueFalseLabels reverse() {
      int temp = this.trueLabel;
      this.trueLabel = this.falseLabel;
      this.falseLabel = temp;
      return this;
    }
  }

  private static int RTLstmts(List<Ast.Source.Stmt> stmts, int Li) {
    int Lo = Li;
    for (Ast.Source.Stmt stmt : stmts) {
      Lo = RTLs(stmt, Lo);
    }
   return Lo;
  }

  // returns out label
  private static int RTLs(Ast.Source.Stmt stmt, int Li) {
    if (stmt instanceof Ast.Source.Stmt.Move) {
      Ast.Source.Stmt.Move move = (Ast.Source.Stmt.Move) stmt;
      Ast.Target.Dest sourceDest;
      int Lo;
      if (move.source.getType() == Ast.Source.Types.int64) {
        DestLabelPair res = RTLi(move.source, Li);
        Lo = res.outLabel;
        sourceDest = res.dest;
      } else {
        TrueFalseLabels res = RTLb(move.source, Li);
        Lo = ++mLabelCounter;
        sourceDest = new Ast.Target.Dest(mVarCounter++);
        mInstrs.add(new Ast.Target.Instr.MoveImm(res.trueLabel, sourceDest, 1, Lo));
        mInstrs.add(new Ast.Target.Instr.MoveImm(res.falseLabel, sourceDest, 0, Lo));
      }
      Ast.Target.Dest targetDest = mVars.get(move.dest.var);
      if (targetDest == null && mVars.containsValue(sourceDest)) {
        Ast.Target.Dest freshDest = new Ast.Target.Dest(mVarCounter++);
        mVars.put(move.dest.var, freshDest);
        Lo = ++mLabelCounter;
        mInstrs.add(new Ast.Target.Instr.MoveCp(Lo - 1, freshDest, sourceDest, Lo));
      } else if (targetDest == null) {
        mVars.put(move.dest.var, sourceDest);
      } else {
        Lo = ++mLabelCounter;
        mInstrs.add(new Ast.Target.Instr.MoveCp(Lo - 1, targetDest, sourceDest, Lo));
      }
      return Lo;
    }
    else if (stmt instanceof Ast.Source.Stmt.IfElse) {
      Ast.Source.Stmt.IfElse ifElse = (Ast.Source.Stmt.IfElse) stmt;
      TrueFalseLabels res = RTLb(ifElse.condition, Li);
      int Lo = RTLstmts(ifElse.thenBranch, res.trueLabel);
      // change last instruction to have correct outLabel
      if (ifElse.elseBranch != null && !ifElse.elseBranch.isEmpty()) {
        int L1 = RTLstmts(ifElse.elseBranch, res.falseLabel);
        mLabelChanges.put(L1, Lo);
      } else if (!ifElse.thenBranch.isEmpty()) {
        mLabelChanges.put(Lo, res.falseLabel);
        Lo = res.falseLabel;
      }
      return Lo;
    }
    else if (stmt instanceof Ast.Source.Stmt.While) {
      Ast.Source.Stmt.While whileStmt = (Ast.Source.Stmt.While) stmt;
      TrueFalseLabels res = RTLb(whileStmt.condition, Li);
      int Lo = RTLstmts(whileStmt.body, res.trueLabel);
      mInstrs.add(new Ast.Target.Instr.Goto(Lo, Li));
      return res.falseLabel;
    }
    else if (stmt instanceof Ast.Source.Stmt.Block) {
      Ast.Source.Stmt.Block block = (Ast.Source.Stmt.Block) stmt;
      return RTLstmts(block.stmts, Li);
    }
    else if (stmt instanceof Ast.Source.Stmt.Print) {
      Ast.Source.Stmt.Print print = (Ast.Source.Stmt.Print) stmt;
      if (print.arg.getType() == Ast.Source.Types.int64) {
        DestLabelPair res = RTLi(print.arg, Li);
        int L1 = ++mLabelCounter;
        mInstrs.add(new Ast.Target.Instr.Print(res.outLabel, res.dest, L1));
        return L1;
      } else {
        TrueFalseLabels res = RTLb(print.arg, Li);
        Ast.Target.Dest dest = new Ast.Target.Dest(mVarCounter++);
        int L1 = ++mLabelCounter;
        int L2 = ++mLabelCounter;
        mInstrs.add(new Ast.Target.Instr.MoveImm(res.trueLabel, dest, 1, L1));
        mInstrs.add(new Ast.Target.Instr.MoveImm(res.falseLabel, dest, 0, L1));
        mInstrs.add(new Ast.Target.Instr.Print(L1, dest, L2));
        return L2;
      }
    }
    return -1;
  }

  // takes in expr and inlabel; returns result dest and outLabel (bottom-up)
  private static DestLabelPair RTLi(Ast.Source.Expr expr, int Li) {
    if (expr instanceof Ast.Source.Expr.IntImm) {
      Ast.Source.Expr.IntImm intImm = (Ast.Source.Expr.IntImm) expr;
      Ast.Target.Dest dest = new Ast.Target.Dest(mVarCounter++);
      int Lo = ++mLabelCounter;
      mInstrs.add(new Ast.Target.Instr.MoveImm(Li, dest, intImm.value, Lo));
      return new DestLabelPair(dest, Lo);
    }
    else if (expr instanceof Ast.Source.Expr.Read) {
      Ast.Source.Expr.Read read = (Ast.Source.Expr.Read) expr;
      return new DestLabelPair(lookup(read.dest.var), Li);
    }
    else if (expr instanceof Ast.Source.Expr.UnopApp) {
      Ast.Source.Expr.UnopApp unopApp = (Ast.Source.Expr.UnopApp) expr;
      Ast.Target.Dest dest = new Ast.Target.Dest(mVarCounter++);
      DestLabelPair argRes = RTLi(unopApp.arg, Li);
      int Lo = ++mLabelCounter;
      mInstrs.add(new Ast.Target.Instr.MoveUnop(
        argRes.outLabel, dest, unopApp.op, argRes.dest, Lo));
      return new DestLabelPair(dest, Lo);
    }
    else if (expr instanceof Ast.Source.Expr.BinopApp) {
      Ast.Source.Expr.BinopApp binopApp = (Ast.Source.Expr.BinopApp) expr;
      Ast.Target.Dest dest = new Ast.Target.Dest(mVarCounter++);
      DestLabelPair leftRes = RTLi(binopApp.leftArg, Li);
      DestLabelPair rightRes = RTLi(binopApp.rightArg, leftRes.outLabel);
      int Lo = ++mLabelCounter;
      mInstrs.add(new Ast.Target.Instr.MoveBinop(
        rightRes.outLabel, dest, leftRes.dest, binopApp.op, rightRes.dest, Lo));
      return new DestLabelPair(dest, Lo);
    }
    return null;
  }

  // takes in expr and inlabel; returns true and false outLabels (bottom-up)
  private static TrueFalseLabels RTLb(Ast.Source.Expr expr, int Li) {
    if (expr instanceof Ast.Source.Expr.BoolImm) {
      Ast.Source.Expr.BoolImm boolImm = (Ast.Source.Expr.BoolImm) expr;
      return boolImm.isTrue ? new TrueFalseLabels(Li, ++mLabelCounter)
        : new TrueFalseLabels(++mLabelCounter, Li);
    }
    else if (expr instanceof Ast.Source.Expr.Read) {
      Ast.Source.Expr.Read read = (Ast.Source.Expr.Read) expr;
      TrueFalseLabels ret = new TrueFalseLabels(++mLabelCounter, ++mLabelCounter);
      // jump to true label if not zero, otherwise jump to false label
      mInstrs.add(new Ast.Target.Instr.UBranch(Li, Ast.Source.CompOp.Neq,
        lookup(read.dest.var), ret.trueLabel, ret.falseLabel));
      return ret;
    }
    else if (expr instanceof Ast.Source.Expr.UnopApp) {
      Ast.Source.Expr.UnopApp unopApp = (Ast.Source.Expr.UnopApp) expr;
      return RTLb(unopApp.arg, Li).reverse();
    }
    else if (expr instanceof Ast.Source.Expr.BoolOpApp) {
      Ast.Source.Expr.BoolOpApp boolOpApp = (Ast.Source.Expr.BoolOpApp) expr;
      TrueFalseLabels leftRes = RTLb(boolOpApp.leftArg, Li);
      if (boolOpApp.op == Ast.Source.BoolOp.And) {
        TrueFalseLabels rightRes = RTLb(boolOpApp.rightArg, leftRes.trueLabel);
        // make sure equivalent cases end up at same place
        mLabelChanges.put(rightRes.falseLabel, leftRes.falseLabel);
        return new TrueFalseLabels(rightRes.trueLabel, leftRes.falseLabel);
      } else {
        TrueFalseLabels rightRes = RTLb(boolOpApp.rightArg, leftRes.falseLabel);
        mLabelChanges.put(rightRes.trueLabel, leftRes.trueLabel);
        return new TrueFalseLabels(leftRes.trueLabel, rightRes.falseLabel);
      }
    }
    else if (expr instanceof Ast.Source.Expr.Comp) {
      Ast.Source.Expr.Comp comp = (Ast.Source.Expr.Comp) expr;
      if (comp.leftArg.getType() == Ast.Source.Types.int64) {
        DestLabelPair leftRes = RTLi(comp.leftArg, Li);
        DestLabelPair rightRes = RTLi(comp.rightArg, leftRes.outLabel);
        TrueFalseLabels ret = new TrueFalseLabels(++mLabelCounter, ++mLabelCounter);
        mInstrs.add(new Ast.Target.Instr.BBranch(rightRes.outLabel, leftRes.dest,
          comp.op, rightRes.dest, ret.trueLabel, ret.falseLabel));
        return ret;
      } else {
        if (comp.op == Ast.Source.CompOp.Eq) {
          // (e1 == e2) ≡ (e1 && e2) || ! (e1 || e2)
          Ast.Source.Expr e1 = new Ast.Source.Expr.BoolOpApp(
            comp.leftArg, Ast.Source.BoolOp.And, comp.rightArg);
          Ast.Source.Expr e2 = new Ast.Source.Expr.BoolOpApp(
            comp.leftArg, Ast.Source.BoolOp.Or, comp.rightArg);
          Ast.Source.Expr e3 = new Ast.Source.Expr.UnopApp(Ast.Source.Unop.BoolNot, e2);
          Ast.Source.Expr e4 = new Ast.Source.Expr.BoolOpApp(e1, Ast.Source.BoolOp.Or, e3);
          return RTLb(e4, Li);
        }
        else {
          // (e1 != e2) ≡ ! (e1 && e2) && (e1 || e2)
          Ast.Source.Expr e1 = new Ast.Source.Expr.BoolOpApp(
            comp.leftArg, Ast.Source.BoolOp.And, comp.rightArg);
          Ast.Source.Expr e2 = new Ast.Source.Expr.BoolOpApp(
            comp.leftArg, Ast.Source.BoolOp.Or, comp.rightArg);
          Ast.Source.Expr e3 = new Ast.Source.Expr.UnopApp(Ast.Source.Unop.BoolNot, e1);
          Ast.Source.Expr e4 = new Ast.Source.Expr.BoolOpApp(e1, Ast.Source.BoolOp.And, e3);
          return RTLb(e4, Li);
        }
      }
    }
    return null;
  }

  private static Ast.Target.Dest lookup(String var) {
    Ast.Target.Dest dest = mVars.get(var);
    if (dest == null) {
      Ast.Source.raise(Ast.Source.Error.UninitializedVarException, var);
    }
    return dest;
  }
}
