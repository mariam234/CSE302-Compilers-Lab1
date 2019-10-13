import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main {
  private static int mVarCounter = 0;
  private static int mLabelCounter = 0;
  private static HashMap<String, Ast.Target.Dest> mVars = new HashMap<>();
  private static List<Ast.Target.Instr> mInstrs = new ArrayList<>();

  public static void main(String[] args) throws Exception {
    for (String bxFile : args) {
      if (! bxFile.endsWith(".bx"))
        throw new RuntimeException(String.format("%s does not end in .bx", bxFile));
      Ast.Source.Prog sourceProg = Ast.Source.readProgram(bxFile);
      System.out.println(sourceProg.toString());
      RTLstmts(sourceProg.stmts, 0);
      System.out.println(String.format("enter L0\nexit L%d\n----", mLabelCounter + 1));
      for (Ast.Target.Instr instr : mInstrs) {
        System.out.println(instr.toRtl());
      }
      System.out.println(String.format("L%d: move 0, #0q --> L%d\nL%d: return #0q",
        mLabelCounter, mLabelCounter + 1, mLabelCounter + 1));
      Ast.Target.Prog targetProg = new Ast.Target.Prog(mInstrs);
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
    public int inLabel;
    public DestLabelPair(Ast.Target.Dest dest, int inLabel) {
      this.dest = dest;
      this.inLabel = inLabel;
    }
  }

  private static int RTLstmts(List<Ast.Source.Stmt> stmts, int Lo) {
    int Li = Lo;
    int L1 = mLabelCounter;
    for (Ast.Source.Stmt stmt : stmts) {
      L1 = RTLs(stmt, L1);
      if (stmt == stmts.get(0)) {
        Li = L1;
      }
    }
   return Li;
  }

  // returns in label
  private static int RTLs(Ast.Source.Stmt stmt, int Lo) {
    if (stmt instanceof Ast.Source.Stmt.Move) {
      Ast.Source.Stmt.Move move = (Ast.Source.Stmt.Move) stmt;
      if (move.source.getType() == Ast.Source.Types.int64) {
        DestLabelPair res = RTLi(move.source, Lo);
        mVars.put(move.dest.var, res.dest);
        // System.out.println(String.format("added (%s, %d) to mVars", move.dest.var, res.dest.loc));
        return res.inLabel;
      } else {
        int Lt = mLabelCounter++;
        int Lf = mLabelCounter++;
        // create new dest or use old one from looking up if exists?
        Ast.Target.Dest dest = new Ast.Target.Dest(mVarCounter++);
        mInstrs.add(new Ast.Target.Instr.MoveImm(Lt, dest, 0, Lo));
        mInstrs.add(new Ast.Target.Instr.MoveImm(Lf, dest, 1, Lo));
        int Li = RTLb(move.source, Lt, Lf);
        mVars.put(move.dest.var, dest);
        // System.out.println(String.format("added (%s, %d) to mVars", move.dest.var, dest.loc));
        return Li;
      }
    }
    else if (stmt instanceof Ast.Source.Stmt.IfElse) {
      Ast.Source.Stmt.IfElse ifElse = (Ast.Source.Stmt.IfElse) stmt;
      int Lt = RTLstmts(ifElse.thenBranch, Lo);
      int Lf = RTLstmts(ifElse.elseBranch, Lo);
      return RTLb(ifElse.condition, Lt, Lf) ;
    }
    else if (stmt instanceof Ast.Source.Stmt.While) {
      Ast.Source.Stmt.While whileStmt = (Ast.Source.Stmt.While) stmt;
      int Lt = RTLstmts(whileStmt.body, Lo);
      int Lend = mLabelCounter++;
      int Li = RTLb(whileStmt.condition, Lend, Lo);
      mInstrs.add(new Ast.Target.Instr.Goto(Lend, Li));
      return Li;
    }
    else if (stmt instanceof Ast.Source.Stmt.Block) {
      Ast.Source.Stmt.Block block = (Ast.Source.Stmt.Block) stmt;
      return RTLstmts(block.stmts, Lo);
    }
    else if (stmt instanceof Ast.Source.Stmt.Print) {
      Ast.Source.Stmt.Print print = (Ast.Source.Stmt.Print) stmt;
      if (print.arg.getType() == Ast.Source.Types.int64) {
        DestLabelPair res = RTLi(print.arg, Lo);
        mInstrs.add(new Ast.Target.Instr.Print(res.inLabel, res.dest, Lo));
        return res.inLabel;
      } else {
        int Lt = mLabelCounter++;
        int Lf = mLabelCounter++;
        Ast.Target.Dest dest = new Ast.Target.Dest(mVarCounter++);
        mInstrs.add(new Ast.Target.Instr.MoveImm(Lt, dest, 0, Lo));
        mInstrs.add(new Ast.Target.Instr.MoveImm(Lf, dest, 1, Lo));
        int Li = RTLb(print.arg, Lt, Lf);
        return Li;
      }
    }
    return -1;
  }

  // takes in expr and outlabel; returns inlabel and result dest (bottom-up)
  private static DestLabelPair RTLi(Ast.Source.Expr expr, int Lo) {
    if (expr instanceof Ast.Source.Expr.IntImm) {
      Ast.Source.Expr.IntImm intImm = (Ast.Source.Expr.IntImm) expr;
      Ast.Target.Dest dest = new Ast.Target.Dest(mVarCounter++);
      mInstrs.add(new Ast.Target.Instr.MoveImm(mLabelCounter++, dest, intImm.value, Lo));
      return new DestLabelPair(dest, mLabelCounter);
    }
    else if (expr instanceof Ast.Source.Expr.Read) {
      Ast.Source.Expr.Read read = (Ast.Source.Expr.Read) expr;
      Ast.Target.Dest dest = new Ast.Target.Dest(mVarCounter++);
      mInstrs.add(new Ast.Target.Instr.MoveCp(mLabelCounter++, dest, lookup(read.dest.var), Lo));
      return new DestLabelPair(dest, mLabelCounter);
    }
    else if (expr instanceof Ast.Source.Expr.UnopApp) {
      Ast.Source.Expr.UnopApp unopApp = (Ast.Source.Expr.UnopApp) expr;
      Ast.Target.Dest dest = new Ast.Target.Dest(mVarCounter++);
      int L1 = mLabelCounter++;
      DestLabelPair argRes = RTLi(unopApp.arg, L1);
      mInstrs.add(new Ast.Target.Instr.MoveUnop(L1, dest, unopApp.op, argRes.dest, Lo));
      return new DestLabelPair(dest, argRes.inLabel);
    }
    else if (expr instanceof Ast.Source.Expr.BinopApp) {
      Ast.Source.Expr.BinopApp binopApp = (Ast.Source.Expr.BinopApp) expr;
      Ast.Target.Dest dest = new Ast.Target.Dest(mVarCounter++);
      int L1 = mLabelCounter++;
      DestLabelPair rightRes = RTLi(binopApp.rightArg, L1);
      DestLabelPair leftRes = RTLi(binopApp.leftArg, rightRes.inLabel);
      mInstrs.add(new Ast.Target.Instr.MoveBinop(
        L1, dest, leftRes.dest, binopApp.op, rightRes.dest, Lo));
      return new DestLabelPair(dest, leftRes.inLabel);
    }
    return null;
  }

  // takes in expr, true outlabel, false outlabel; returns inlabel (bottom-up)
  private static int RTLb(Ast.Source.Expr expr, int Lt, int Lf) {
    if (expr instanceof Ast.Source.Expr.BoolImm) {
      Ast.Source.Expr.BoolImm boolImm = (Ast.Source.Expr.BoolImm) expr;
      return boolImm.isTrue ?  Lt : Lf;
    }
    else if (expr instanceof Ast.Source.Expr.UnopApp) {
      Ast.Source.Expr.UnopApp unopApp = (Ast.Source.Expr.UnopApp) expr;
      return RTLb(unopApp.arg, Lf, Lt);
    }
    else if (expr instanceof Ast.Source.Expr.BoolOpApp) {
      Ast.Source.Expr.BoolOpApp boolOpApp = (Ast.Source.Expr.BoolOpApp) expr;
      int L1 = RTLb(boolOpApp.rightArg, Lt, Lf);
      return boolOpApp.op == Ast.Source.BoolOp.And
          ? RTLb(boolOpApp.leftArg, L1, Lf) : RTLb(boolOpApp.leftArg, Lt, L1);
    }
    else if (expr instanceof Ast.Source.Expr.Comp) {
      Ast.Source.Expr.Comp comp = (Ast.Source.Expr.Comp) expr;
      if (comp.leftArg.getType() == Ast.Source.Types.int64) {
        int L1 = mLabelCounter++;
        DestLabelPair rightRes = RTLi(comp.rightArg, L1);
        DestLabelPair leftRes = RTLi(comp.leftArg, rightRes.inLabel);
        mInstrs.add(new Ast.Target.Instr.BBranch(L1, leftRes.dest, comp.op,
          rightRes.dest, Lt, Lf));
        return leftRes.inLabel;
      } else {
        // using equivalencies for boolean eq/neq
        if (comp.op == Ast.Source.CompOp.Eq) {
          return -2;
        } else {
          return -3;
        }
      }
    }
    return -1;
  }

  private static Ast.Target.Dest lookup(String var) {
    Ast.Target.Dest dest = mVars.get(var);
    if (dest == null) {
      Ast.Source.raise(Ast.Source.Error.UninitializedVarException, var);
    }
    return dest;
  }
}
