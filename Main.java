import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
  private static final Map<Ast.Source.Unop, String> unopMap = Map.of(
    Ast.Source.Unop.Negate, "-",
    Ast.Source.Unop.BitNot, "~"
  );

  private static final Map<Ast.Source.Binop, String> binopMap = Map.of(
    Ast.Source.Binop.Add, "+",
    Ast.Source.Binop.Subtract, "-",
    Ast.Source.Binop.Multiply, "*",
    Ast.Source.Binop.Divide, "/",
    Ast.Source.Binop.Modulus, "%",
    Ast.Source.Binop.BitAnd, "&",
    Ast.Source.Binop.BitOr, "|",
    Ast.Source.Binop.BitXor, "^",
    Ast.Source.Binop.Lshift, "<<",
    Ast.Source.Binop.Rshift, ">>"
  );

  private static int varCounter = 0;
  private static HashMap<String, Ast.Target.Dest> dests = new HashMap<>();
  private static List<Ast.Target.Instr> instrs = new ArrayList<>();

  public static void main(String[] args) throws Exception {
    for (String bxFile : args) {
      if (! bxFile.endsWith(".bx"))
        throw new RuntimeException(String.format("%s does not end in .bx", bxFile));
      Ast.Source.Prog progSource = Ast.Source.readProgram(bxFile);
      for (Ast.Source.Stmt stmt : progSource.statements) {
        generateInstructions(stmt);
      }
      Ast.Target.Prog progTarget = new Ast.Target.Prog(instrs);
      String stem = bxFile.substring(0, bxFile.length() - 3);
      String cFile = stem + ".c";
      PrintStream out = new PrintStream(cFile);
      out.println("#include \"bx0.h\"");
      out.println("int main(){");
      out.print("\tint64_t");
      for (int i = 0; i < varCounter; i++) {
        if (i == varCounter - 1) {
          out.print(String.format(" x%d;\n\n", i));
        } else {
          out.print(String.format(" x%d,", i));
        }
      }
      for (Ast.Target.Instr instr : progTarget.instructions) {
        out.println("\t" + instrToString(instr));
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
      dests.put(move.dest.var, genInstrsFromExpr(move.source));
    } else if (stmt instanceof Ast.Source.Stmt.Print) {
      Ast.Source.Stmt.Print print = (Ast.Source.Stmt.Print) stmt;
      instrs.add(new Ast.Target.Instr.Print(genInstrsFromExpr(print.arg)));
    }
  }

  // helper function for generateInstructions
  private static Ast.Target.Dest genInstrsFromExpr(Ast.Source.Expr expr) {
    Ast.Target.Dest dest = null;
    if (expr instanceof Ast.Source.Expr.Immediate) {
      Ast.Source.Expr.Immediate imm = (Ast.Source.Expr.Immediate) expr;
      dest = new Ast.Target.Dest(varCounter++);
      instrs.add(new Ast.Target.Instr.MoveImm(dest, imm.value));
    } else if (expr instanceof Ast.Source.Expr.Read) {
      Ast.Source.Expr.Read read = (Ast.Source.Expr.Read) expr;
      dest = dests.get(read.dest.var);
    } else if (expr instanceof Ast.Source.Expr.UnopApp) {
      Ast.Source.Expr.UnopApp unopApp = (Ast.Source.Expr.UnopApp) expr;
      Ast.Target.Dest argDest = genInstrsFromExpr(unopApp.arg);
      dest = new Ast.Target.Dest(varCounter++);
      instrs.add(new Ast.Target.Instr.MoveUnop(dest, unopApp.op, argDest));
    } else if (expr instanceof Ast.Source.Expr.BinopApp) {
      Ast.Source.Expr.BinopApp binopApp = (Ast.Source.Expr.BinopApp) expr;
      Ast.Target.Dest leftDest = genInstrsFromExpr(binopApp.leftArg);
      Ast.Target.Dest rightDest = genInstrsFromExpr(binopApp.rightArg);
      dest = new Ast.Target.Dest(varCounter++);
      instrs.add(new Ast.Target.Instr.MoveBinop(
        dest, leftDest, binopApp.op, rightDest));
    }
    return dest;
  }

  private static String instrToString(Ast.Target.Instr instruction) {
    if (instruction instanceof Ast.Target.Instr.MoveImm) {
      Ast.Target.Instr.MoveImm instr = (Ast.Target.Instr.MoveImm) instruction;
      return String.format("x%d = %d;", instr.dest.loc, instr.imm);
    } else if (instruction instanceof Ast.Target.Instr.MoveCp) {
      Ast.Target.Instr.MoveCp instr = (Ast.Target.Instr.MoveCp) instruction;
      return String.format("x%d = x%d;", instr.dest.loc, instr.source.loc);
    } else if (instruction instanceof Ast.Target.Instr.MoveBinop) {
      Ast.Target.Instr.MoveBinop instr = (Ast.Target.Instr.MoveBinop) instruction;
      return String.format("x%d = x%d %s x%d;", instr.dest.loc,
        instr.leftArg.loc, binopMap.get(instr.op), instr.rightArg.loc);
    } else if (instruction instanceof Ast.Target.Instr.MoveUnop) {
      Ast.Target.Instr.MoveUnop instr = (Ast.Target.Instr.MoveUnop) instruction;
      return String.format("x%d = %s x%d;", instr.dest.loc,
        unopMap.get(instr.op), instr.arg.loc);
    } else if (instruction instanceof Ast.Target.Instr.Print) {
      Ast.Target.Instr.Print instr = (Ast.Target.Instr.Print) instruction;
      return String.format("PRINT(x%d);", instr.dest.loc);
    } else if (instruction instanceof Ast.Target.Instr.Comment) {
      Ast.Target.Instr.Comment instr = (Ast.Target.Instr.Comment) instruction;
      return String.format("// %s", instr.comment);
    }
    return null;
  }
}
