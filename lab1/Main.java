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
      Ast.Source.Prog progSource = Ast.Source.readProgram(bxFile);
      for (Ast.Source.Stmt stmt : progSource.statements) {
        generateInstructions(stmt);
      }
      Ast.Target.Prog progTarget = new Ast.Target.Prog(mInstrs);
      String stem = bxFile.substring(0, bxFile.length() - 3);
      String cFile = stem + ".c";
      PrintStream out = new PrintStream(cFile);
      out.println("#include \"bx0.h\"");
      out.println("int main(){");
      out.print("\tint64_t");
      for (int i = 0; i < mVarCounter; i++) {
        if (i == mVarCounter - 1) {
          out.print(String.format(" x%d;\n\n", i));
        } else {
          out.print(String.format(" x%d,", i));
        }
      }
      for (Ast.Target.Instr instr : progTarget.instructions) {
        out.println("\t" + instr.toC());
      }
      out.println("\n\treturn 0;");
      out.println("}");
      out.close();
      String gccCmd = String.format("gcc -o %s.exe %s", stem, cFile);
      Process gccProc = Runtime.getRuntime().exec(gccCmd);
      gccProc.waitFor();
    }
  }

  private static void generateInstructions(Ast.Source.Stmt stmt) {
    if (stmt instanceof Ast.Source.Stmt.Move) {
      Ast.Source.Stmt.Move move = (Ast.Source.Stmt.Move) stmt;
      mVars.put(move.dest.var, genInstrsFromExpr(move.source));
    } else if (stmt instanceof Ast.Source.Stmt.Print) {
      Ast.Source.Stmt.Print print = (Ast.Source.Stmt.Print) stmt;
      mInstrs.add(new Ast.Target.Instr.Print(genInstrsFromExpr(print.arg)));
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
