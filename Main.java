import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Main {
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

  public static List<Ast.Target.Instr> getInstructions(List<Ast.Source.Stmt> stmts) {
    // if (stmts.isEmpty()) {
    //   return new ArrayList<>;
    // }
    // if (stmts.get(0) instanceOf Ast.Source.Stmt.Move) {
    //   Ast.Source.Stmt.Move move = stmts.get(0);
    //
    // } else {
    //
    // }
    return new ArrayList<Ast.Target.Instr>();
  }

  public static String getLine(Ast.Target.Instr instruction) {
    if (instruction instanceof Ast.Target.Instr.MoveImm) {
      Ast.Target.Instr.MoveImm instr = (Ast.Target.Instr.MoveImm) instruction;
      return String.format("%s = %d;", instr.dest.toString(), instr.imm);
    }
    return "";
      // case Ast.Target.Instr.MoveCp:
      //   Ast.Target.Instr.MoveImm moveCp = instr;
      //   return String.format("%s = %s;", moveCp.dest.toString(), moveCp.source.toString());
      // case Ast.Target.Instr.MoveBinop:
      //   Ast.Target.Instr.MoveBinop moveBinop = instr;
      //   return String.format("%s = %s %s %s;", moveBinop.dest.toString(),
      //     moveBinop.leftArg.toString(), moveBinop.rightArg.toString(), moveBinop.op.toString());
      // case Ast.Target.Instr.MoveUnop:
      //   Ast.Target.Instr.MoveUnop moveUnop = instr;
      //   return String.format("%s = %s %s;", moveUnop.dest.toString(), moveUnop.op.toString(), moveUnop.arg.toString());
      // case Ast.Target.Instr.Print:
      //   Ast.Target.Instr.Print print = instr;
      //   return String.format("print %s;", print.dest.toString());
      // case Ast.Target.Instr.Comment:
      //   Ast.Target.Instr.Comment comment = instr;
      //   return String.format("// %s", comment.comment);
  }
}
