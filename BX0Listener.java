// Generated from BX0.g4 by ANTLR 4.7.2
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link BX0Parser}.
 */
public interface BX0Listener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link BX0Parser#program}.
	 * @param ctx the parse tree
	 */
	void enterProgram(BX0Parser.ProgramContext ctx);
	/**
	 * Exit a parse tree produced by {@link BX0Parser#program}.
	 * @param ctx the parse tree
	 */
	void exitProgram(BX0Parser.ProgramContext ctx);
	/**
	 * Enter a parse tree produced by {@link BX0Parser#vardecl}.
	 * @param ctx the parse tree
	 */
	void enterVardecl(BX0Parser.VardeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link BX0Parser#vardecl}.
	 * @param ctx the parse tree
	 */
	void exitVardecl(BX0Parser.VardeclContext ctx);
	/**
	 * Enter a parse tree produced by the {@code move}
	 * labeled alternative in {@link BX0Parser#statement}.
	 * @param ctx the parse tree
	 */
	void enterMove(BX0Parser.MoveContext ctx);
	/**
	 * Exit a parse tree produced by the {@code move}
	 * labeled alternative in {@link BX0Parser#statement}.
	 * @param ctx the parse tree
	 */
	void exitMove(BX0Parser.MoveContext ctx);
	/**
	 * Enter a parse tree produced by the {@code print}
	 * labeled alternative in {@link BX0Parser#statement}.
	 * @param ctx the parse tree
	 */
	void enterPrint(BX0Parser.PrintContext ctx);
	/**
	 * Exit a parse tree produced by the {@code print}
	 * labeled alternative in {@link BX0Parser#statement}.
	 * @param ctx the parse tree
	 */
	void exitPrint(BX0Parser.PrintContext ctx);
	/**
	 * Enter a parse tree produced by the {@code blockstmt}
	 * labeled alternative in {@link BX0Parser#statement}.
	 * @param ctx the parse tree
	 */
	void enterBlockstmt(BX0Parser.BlockstmtContext ctx);
	/**
	 * Exit a parse tree produced by the {@code blockstmt}
	 * labeled alternative in {@link BX0Parser#statement}.
	 * @param ctx the parse tree
	 */
	void exitBlockstmt(BX0Parser.BlockstmtContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ifelsestmt}
	 * labeled alternative in {@link BX0Parser#statement}.
	 * @param ctx the parse tree
	 */
	void enterIfelsestmt(BX0Parser.IfelsestmtContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ifelsestmt}
	 * labeled alternative in {@link BX0Parser#statement}.
	 * @param ctx the parse tree
	 */
	void exitIfelsestmt(BX0Parser.IfelsestmtContext ctx);
	/**
	 * Enter a parse tree produced by the {@code whilestmt}
	 * labeled alternative in {@link BX0Parser#statement}.
	 * @param ctx the parse tree
	 */
	void enterWhilestmt(BX0Parser.WhilestmtContext ctx);
	/**
	 * Exit a parse tree produced by the {@code whilestmt}
	 * labeled alternative in {@link BX0Parser#statement}.
	 * @param ctx the parse tree
	 */
	void exitWhilestmt(BX0Parser.WhilestmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link BX0Parser#block}.
	 * @param ctx the parse tree
	 */
	void enterBlock(BX0Parser.BlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link BX0Parser#block}.
	 * @param ctx the parse tree
	 */
	void exitBlock(BX0Parser.BlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link BX0Parser#ifelse}.
	 * @param ctx the parse tree
	 */
	void enterIfelse(BX0Parser.IfelseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BX0Parser#ifelse}.
	 * @param ctx the parse tree
	 */
	void exitIfelse(BX0Parser.IfelseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BX0Parser#whileloop}.
	 * @param ctx the parse tree
	 */
	void enterWhileloop(BX0Parser.WhileloopContext ctx);
	/**
	 * Exit a parse tree produced by {@link BX0Parser#whileloop}.
	 * @param ctx the parse tree
	 */
	void exitWhileloop(BX0Parser.WhileloopContext ctx);
	/**
	 * Enter a parse tree produced by {@link BX0Parser#type}.
	 * @param ctx the parse tree
	 */
	void enterType(BX0Parser.TypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link BX0Parser#type}.
	 * @param ctx the parse tree
	 */
	void exitType(BX0Parser.TypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code add}
	 * labeled alternative in {@link BX0Parser#expr}.
	 * @param ctx the parse tree
	 */
	void enterAdd(BX0Parser.AddContext ctx);
	/**
	 * Exit a parse tree produced by the {@code add}
	 * labeled alternative in {@link BX0Parser#expr}.
	 * @param ctx the parse tree
	 */
	void exitAdd(BX0Parser.AddContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ineq}
	 * labeled alternative in {@link BX0Parser#expr}.
	 * @param ctx the parse tree
	 */
	void enterIneq(BX0Parser.IneqContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ineq}
	 * labeled alternative in {@link BX0Parser#expr}.
	 * @param ctx the parse tree
	 */
	void exitIneq(BX0Parser.IneqContext ctx);
	/**
	 * Enter a parse tree produced by the {@code parens}
	 * labeled alternative in {@link BX0Parser#expr}.
	 * @param ctx the parse tree
	 */
	void enterParens(BX0Parser.ParensContext ctx);
	/**
	 * Exit a parse tree produced by the {@code parens}
	 * labeled alternative in {@link BX0Parser#expr}.
	 * @param ctx the parse tree
	 */
	void exitParens(BX0Parser.ParensContext ctx);
	/**
	 * Enter a parse tree produced by the {@code or}
	 * labeled alternative in {@link BX0Parser#expr}.
	 * @param ctx the parse tree
	 */
	void enterOr(BX0Parser.OrContext ctx);
	/**
	 * Exit a parse tree produced by the {@code or}
	 * labeled alternative in {@link BX0Parser#expr}.
	 * @param ctx the parse tree
	 */
	void exitOr(BX0Parser.OrContext ctx);
	/**
	 * Enter a parse tree produced by the {@code mul}
	 * labeled alternative in {@link BX0Parser#expr}.
	 * @param ctx the parse tree
	 */
	void enterMul(BX0Parser.MulContext ctx);
	/**
	 * Exit a parse tree produced by the {@code mul}
	 * labeled alternative in {@link BX0Parser#expr}.
	 * @param ctx the parse tree
	 */
	void exitMul(BX0Parser.MulContext ctx);
	/**
	 * Enter a parse tree produced by the {@code boolor}
	 * labeled alternative in {@link BX0Parser#expr}.
	 * @param ctx the parse tree
	 */
	void enterBoolor(BX0Parser.BoolorContext ctx);
	/**
	 * Exit a parse tree produced by the {@code boolor}
	 * labeled alternative in {@link BX0Parser#expr}.
	 * @param ctx the parse tree
	 */
	void exitBoolor(BX0Parser.BoolorContext ctx);
	/**
	 * Enter a parse tree produced by the {@code shift}
	 * labeled alternative in {@link BX0Parser#expr}.
	 * @param ctx the parse tree
	 */
	void enterShift(BX0Parser.ShiftContext ctx);
	/**
	 * Exit a parse tree produced by the {@code shift}
	 * labeled alternative in {@link BX0Parser#expr}.
	 * @param ctx the parse tree
	 */
	void exitShift(BX0Parser.ShiftContext ctx);
	/**
	 * Enter a parse tree produced by the {@code unop}
	 * labeled alternative in {@link BX0Parser#expr}.
	 * @param ctx the parse tree
	 */
	void enterUnop(BX0Parser.UnopContext ctx);
	/**
	 * Exit a parse tree produced by the {@code unop}
	 * labeled alternative in {@link BX0Parser#expr}.
	 * @param ctx the parse tree
	 */
	void exitUnop(BX0Parser.UnopContext ctx);
	/**
	 * Enter a parse tree produced by the {@code booladd}
	 * labeled alternative in {@link BX0Parser#expr}.
	 * @param ctx the parse tree
	 */
	void enterBooladd(BX0Parser.BooladdContext ctx);
	/**
	 * Exit a parse tree produced by the {@code booladd}
	 * labeled alternative in {@link BX0Parser#expr}.
	 * @param ctx the parse tree
	 */
	void exitBooladd(BX0Parser.BooladdContext ctx);
	/**
	 * Enter a parse tree produced by the {@code eq}
	 * labeled alternative in {@link BX0Parser#expr}.
	 * @param ctx the parse tree
	 */
	void enterEq(BX0Parser.EqContext ctx);
	/**
	 * Exit a parse tree produced by the {@code eq}
	 * labeled alternative in {@link BX0Parser#expr}.
	 * @param ctx the parse tree
	 */
	void exitEq(BX0Parser.EqContext ctx);
	/**
	 * Enter a parse tree produced by the {@code number}
	 * labeled alternative in {@link BX0Parser#expr}.
	 * @param ctx the parse tree
	 */
	void enterNumber(BX0Parser.NumberContext ctx);
	/**
	 * Exit a parse tree produced by the {@code number}
	 * labeled alternative in {@link BX0Parser#expr}.
	 * @param ctx the parse tree
	 */
	void exitNumber(BX0Parser.NumberContext ctx);
	/**
	 * Enter a parse tree produced by the {@code boolean}
	 * labeled alternative in {@link BX0Parser#expr}.
	 * @param ctx the parse tree
	 */
	void enterBoolean(BX0Parser.BooleanContext ctx);
	/**
	 * Exit a parse tree produced by the {@code boolean}
	 * labeled alternative in {@link BX0Parser#expr}.
	 * @param ctx the parse tree
	 */
	void exitBoolean(BX0Parser.BooleanContext ctx);
	/**
	 * Enter a parse tree produced by the {@code and}
	 * labeled alternative in {@link BX0Parser#expr}.
	 * @param ctx the parse tree
	 */
	void enterAnd(BX0Parser.AndContext ctx);
	/**
	 * Exit a parse tree produced by the {@code and}
	 * labeled alternative in {@link BX0Parser#expr}.
	 * @param ctx the parse tree
	 */
	void exitAnd(BX0Parser.AndContext ctx);
	/**
	 * Enter a parse tree produced by the {@code variable}
	 * labeled alternative in {@link BX0Parser#expr}.
	 * @param ctx the parse tree
	 */
	void enterVariable(BX0Parser.VariableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code variable}
	 * labeled alternative in {@link BX0Parser#expr}.
	 * @param ctx the parse tree
	 */
	void exitVariable(BX0Parser.VariableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code xor}
	 * labeled alternative in {@link BX0Parser#expr}.
	 * @param ctx the parse tree
	 */
	void enterXor(BX0Parser.XorContext ctx);
	/**
	 * Exit a parse tree produced by the {@code xor}
	 * labeled alternative in {@link BX0Parser#expr}.
	 * @param ctx the parse tree
	 */
	void exitXor(BX0Parser.XorContext ctx);
}