	.file "test/fib.bx"
	.section .text
	.globl main
main:
	pushq %rbp:
	pushq %rsp, %rbp
	subq $96, %rsp

	int64_t	x0 = 0;
	x1 = 1;
	PRINT(x0);
	x2 = x1 + x0;
	PRINT(x1);
	x3 = x2 + x1;
	PRINT(x2);
	x4 = x3 + x2;
	PRINT(x3);
	x5 = x4 + x3;
	PRINT(x4);
	x6 = x5 + x4;
	PRINT(x5);
	x7 = x6 + x5;
	PRINT(x6);
	x8 = x7 + x6;
	PRINT(x7);
	x9 = x8 + x7;
	PRINT(x8);
	x10 = x9 + x8;
	PRINT(x9);
	x11 = x10 + x9;
	PRINT(x10);

	movq %rbp, %rsp
	popq %rbp
	movq $0, %rax
	retq
