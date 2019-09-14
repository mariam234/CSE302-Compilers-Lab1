import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Set;
import java.util.SortedSet;

public class Main {
  private static int idCounter = 0;
  private static HashMap<Ast.Source.Dest, Ast.Target.Dest> dests = new HashMap<>();
  private static List<Ast.Target.Instr> instrs = new ArrayList<>();

  public static void main(String[] args) throws Exception {
    for (String bxFile : args) {
      if (! bxFile.endsWith(".bx"))
        throw new RuntimeException(String.format("%s does not end in .bx", bxFile));
      Ast.Source.Prog progSource = Ast.Source.readProgram(bxFile);
      System.out.print(progSource.toString());
      for (Ast.Source.Stmt stmt : progSource.statements) {
        generateInstructions(stmt);
      }
      Ast.Target.Prog progTarget = new Ast.Target.Prog(instrs);
      String stem = bxFile.substring(0, bxFile.length() - 3);
      String cFile = stem + ".c";
      PrintStream out = new PrintStream(cFile);
      out.println("#include \"bx0.h\"");
      out.println("int main(){");
      for (Ast.Target.Instr instr : progTarget.instructions) {
        out.println("\t" + getLine(instr));
      }
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
      dests.put(move.dest, genInstrsFromExpr(move.source));
    } else if (stmt instanceof Ast.Source.Stmt.Print) {
      Ast.Source.Stmt.Print print = (Ast.Source.Stmt.Print) stmt;
      // TODO - check for null ?
      instrs.add(new Ast.Target.Instr.Print(genInstrsFromExpr(print.arg)));
    }
  }

  // helper function for generateInstructions
  private static Ast.Target.Dest genInstrsFromExpr(Ast.Source.Expr expr) {
    Ast.Target.Dest dest = null;
    if (expr instanceof Ast.Source.Expr.Immediate) {
      Ast.Source.Expr.Immediate imm = (Ast.Source.Expr.Immediate) expr;
      dest = new Ast.Target.Dest(idCounter++);
      instrs.add(new Ast.Target.Instr.MoveImm(dest, imm.value));
    } else if (expr instanceof Ast.Source.Expr.Read) {
      Ast.Source.Expr.Read read = (Ast.Source.Expr.Read) expr;
      dest = dests.get(read.dest);
    } else if (expr instanceof Ast.Source.Expr.UnopApp) {
      Ast.Source.Expr.UnopApp unopApp = (Ast.Source.Expr.UnopApp) expr;
      Ast.Target.Dest argDest = genInstrsFromExpr(unopApp.arg);
      dest = new Ast.Target.Dest(idCounter++);
      instrs.add(new Ast.Target.Instr.MoveUnop(dest, unopApp.op, argDest));
    } else if (expr instanceof Ast.Source.Expr.BinopApp) {
      Ast.Source.Expr.BinopApp binopApp = (Ast.Source.Expr.BinopApp) expr;
      Ast.Target.Dest leftDest = genInstrsFromExpr(binopApp.leftArg);
      Ast.Target.Dest rightDest = genInstrsFromExpr(binopApp.rightArg);
      dest = new Ast.Target.Dest(idCounter++);
      instrs.add(new Ast.Target.Instr.MoveBinop(
        dest, leftDest, binopApp.op, rightDest));
    }
    return dest;
  }

// !!!! TODO - allowed to change ast??
  private static String getLine(Ast.Target.Instr instruction) {
    if (instruction instanceof Ast.Target.Instr.MoveImm) {
      Ast.Target.Instr.MoveImm instr = (Ast.Target.Instr.MoveImm) instruction;
      return String.format("%s = %d;", instr.dest.toString(), instr.imm);
    } else if (instruction instanceof Ast.Target.Instr.MoveCp) {
      Ast.Target.Instr.MoveCp instr = (Ast.Target.Instr.MoveCp) instruction;
      return String.format("%s = %s;", instr.dest.toString(), instr.source.toString());
    } else if (instruction instanceof Ast.Target.Instr.MoveBinop) {
      Ast.Target.Instr.MoveBinop instr = (Ast.Target.Instr.MoveBinop) instruction;
      return String.format("%s = %s %s %s;", instr.dest.toString(),
        instr.leftArg.toString(), instr.rightArg.toString(), instr.op.toString());
    } else if (instruction instanceof Ast.Target.Instr.MoveUnop) {
      Ast.Target.Instr.MoveUnop instr = (Ast.Target.Instr.MoveUnop) instruction;
      return String.format("%s = %s %s;", instr.dest.toString(),
        instr.op.toString(), instr.arg.toString());
    } else if (instruction instanceof Ast.Target.Instr.Print) {
      Ast.Target.Instr.Print instr = (Ast.Target.Instr.Print) instruction;
      return String.format("print %s;", instr.dest.toString());
    } else if (instruction instanceof Ast.Target.Instr.Comment) {
      Ast.Target.Instr.Comment instr = (Ast.Target.Instr.Comment) instruction;
      return String.format("// %s", instr.comment);
    }
    return "";
  }
}
