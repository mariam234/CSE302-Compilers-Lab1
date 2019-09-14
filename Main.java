import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class Main {
  private static int counter = 0;
  private static SortedSet variables = new TreeSet();

  public static void main(String[] args) throws Exception {
    for (String bxFile : args) {
      if (! bxFile.endsWith(".bx"))
        throw new RuntimeException(String.format("%s does not end in .bx", bxFile));
      Ast.Source.Prog progSource = Ast.Source.readProgram(bxFile);
      System.out.print(progSource.toString());
      Ast.Target.Prog progTarget =
        new Ast.Target.Prog(getInstructions(progSource.statements));
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

// !!! TODO - Can print expressions or just vars
  public static List<Ast.Target.Instr> getInstructions(List<Ast.Source.Stmt> stmts) {
    if (stmts.isEmpty()) {
      return new ArrayList<>;
    }
    Ast.Source.Stmt stmt = stmts.get(0);
    if (stmt instanceof Ast.Source.Stmt.Move) {
      Ast.Source.Stmt.Move move = (Ast.Source.Stmt.Move) stmt;

    } else if (stmt instanceof Ast.Source.Stmt.Print) {
      Ast.Source.Stmt.Print print = (Ast.Source.Stmt.Print) stmt;
    }

  }

// !!!! TODO - allowed to change ast??
  public static String getLine(Ast.Target.Instr instruction) {
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
