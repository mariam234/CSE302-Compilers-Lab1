import java.io.*;

public class Main {
  public static void main(String[] args) throws Exception {
    for (String bxFile : args) {
      if (! bxFile.endsWith(".bx"))
        throw new RuntimeException(String.format("%s does not end in .bx", bxFile));
      Ast.Source.Prog progSource = Ast.Source.readProgram(bxFile);
      // System.out.print(prog.toString());
      Ast.Target.Prog progTarget =
        Ast.Target.Prog(getInstructions(progSource.statements));
      String stem = bxFile.substring(0, bxFile.length() - 3);
      String cFile = stem + ".c";
      PrintStream out = new PrintStream(cFile);
      out.println("#include \"bx0.h\"");
      out.println("int main(){");
      for (Ast.Target.Instr instr : progTarget.instructions) {
        out.println(getLine(instr));
      }
      generateCompiledCode(progTarget, out);
      out.println("}");
      out.close();
      String gccCmd = String.format("gcc -o %s.exe %s", stem, cFile);
      Process gccProc = Runtime.getRuntime().exec(gccCmd);
      gccProc.waitFor();
    }
  }

  public static List<Ast.Target.Instr> getInstructions(List<Ast.Source.Stmt> stmts) {
    if (stmts.isEmpty()) {
      return new ArrayList<>;
    }
    if (stmts.get(0) instanceOf Ast.Source.Stmt.Move) {
      Ast.Source.Stmt.Move move = stmts.get(0);

    } else {

    }
  }

  public static String getLine(Ast.Target.Instr instr) {
    switch instr.getClass() {
      case MoveImm:
        return String.format("%s = %i;", instr.dest.toString(), instr.imm);
      case MoveCp:
        return String.format("%s = %i;", instr.dest.toString(), instr.source.toString());
      case MoveBinop:
        return String.format("%s = %s %s %s;", instr.dest.toString(),
          instr.leftArg.toString(), instr.rightArg.toString(), instr.op.toString());
      case MoveUnop:
        return String.format("%s = %s %s;", instr.dest.toString(), instr.op.toString(), instr.arg.toString());
      case Print:
        return String.format("print %s;", instr.dest.toString());
      case Comment:
        return String.format("// %s", instr.comment);
    }
  }
}
