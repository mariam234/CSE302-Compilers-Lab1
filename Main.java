import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main {
  private static int mVarCounter = 0;
  private static HashMap<String, Ast.Target.Dest> mVars = new HashMap<>();
  private static List<Ast.Target.Instr> mInstrs = new ArrayList<>();

  public static void main(String[] args) throws Exception {
    for (String bxFile : args) {
      if (! bxFile.endsWith(".bx"))
        throw new RuntimeException(String.format("%s does not end in .bx", bxFile));
      System.out.println(Ast.Source.readProgram(bxFile).toString());
      generateInstructions(Ast.Source.readProgram(bxFile));
      Ast.Target.Prog progTarget = new Ast.Target.Prog(mInstrs);
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
      for (Ast.Target.Instr instr : progTarget.instructions) {
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

  private static void generateInstructions(Ast.Source.Prog progSource) {
    for (Ast.Source.Stmt stmt : progSource.statements) {
      if (stmt instanceof Ast.Source.Stmt.Move) {
        Ast.Source.Stmt.Move move = (Ast.Source.Stmt.Move) stmt;
        mVars.put(move.dest.var, genInstrsFromExpr(move.source));
      } else if (stmt instanceof Ast.Source.Stmt.Print) {
        Ast.Source.Stmt.Print print = (Ast.Source.Stmt.Print) stmt;
        mInstrs.add(new Ast.Target.Instr.Print(genInstrsFromExpr(print.arg)));
      }
    }
  }

  // helper function for generateInstructions
  private static Ast.Target.Dest genInstrsFromExpr(Ast.Source.Expr expr) {
    Ast.Target.Dest dest = null;
    if (expr instanceof Ast.Source.Expr.Immediate) {
      Ast.Source.Expr.Immediate imm = (Ast.Source.Expr.Immediate) expr;
      dest = new Ast.Target.Dest(mVarCounter++);
      mInstrs.add(new Ast.Target.Instr.MoveImm(dest, imm.value));
    } else if (expr instanceof Ast.Source.Expr.Read) {
      Ast.Source.Expr.Read read = (Ast.Source.Expr.Read) expr;
      dest = mVars.get(read.dest.var);
    } else if (expr instanceof Ast.Source.Expr.UnopApp) {
      Ast.Source.Expr.UnopApp unopApp = (Ast.Source.Expr.UnopApp) expr;
      Ast.Target.Dest argDest = genInstrsFromExpr(unopApp.arg);
      dest = new Ast.Target.Dest(mVarCounter++);
      mInstrs.add(new Ast.Target.Instr.MoveUnop(dest, unopApp.op, argDest));
    } else if (expr instanceof Ast.Source.Expr.BinopApp) {
      Ast.Source.Expr.BinopApp binopApp = (Ast.Source.Expr.BinopApp) expr;
      Ast.Target.Dest leftDest = genInstrsFromExpr(binopApp.leftArg);
      Ast.Target.Dest rightDest = genInstrsFromExpr(binopApp.rightArg);
      dest = new Ast.Target.Dest(mVarCounter++);
      mInstrs.add(new Ast.Target.Instr.MoveBinop(
        dest, leftDest, binopApp.op, rightDest));
    }
    return dest;
  }
}
