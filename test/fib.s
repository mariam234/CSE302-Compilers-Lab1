	.file "test/fib.bx"
	.section .text
	.globl main
main:
	pushq %rbp:
	pushq %rsp, %rbp
	subq $104, %rsp

	movq $0, (%rsp)
	movq $1, 8(%rsp)
	movq (%rsp), %rdi
	callq bx0_print

	movq 8(%rsp), %r11
	addq (%rsp), %r11
	movq %r11, 16(%rsp)
	movq 8(%rsp), %rdi
	callq bx0_print

	movq 16(%rsp), %r11
	addq 8(%rsp), %r11
	movq %r11, 24(%rsp)
	movq 16(%rsp), %rdi
	callq bx0_print

	movq 24(%rsp), %r11
	addq 16(%rsp), %r11
	movq %r11, 32(%rsp)
	movq 24(%rsp), %rdi
	callq bx0_print

	movq 32(%rsp), %r11
	addq 24(%rsp), %r11
	movq %r11, 40(%rsp)
	movq 32(%rsp), %rdi
	callq bx0_print

	movq 40(%rsp), %r11
	addq 32(%rsp), %r11
	movq %r11, 48(%rsp)
	movq 40(%rsp), %rdi
	callq bx0_print

	movq 48(%rsp), %r11
	addq 40(%rsp), %r11
	movq %r11, 56(%rsp)
	movq 48(%rsp), %rdi
	callq bx0_print

	movq 56(%rsp), %r11
	addq 48(%rsp), %r11
	movq %r11, 64(%rsp)
	movq 56(%rsp), %rdi
	callq bx0_print

	movq 64(%rsp), %r11
	addq 56(%rsp), %r11
	movq %r11, 72(%rsp)
	movq 64(%rsp), %rdi
	callq bx0_print

	movq 72(%rsp), %r11
	addq 64(%rsp), %r11
	movq %r11, 80(%rsp)
	movq 72(%rsp), %rdi
	callq bx0_print

	movq 80(%rsp), %r11
	addq 72(%rsp), %r11
	movq %r11, 88(%rsp)
	movq 80(%rsp), %rdi
	callq bx0_print

	movq %rbp, %rsp
	popq %rbp
	movq $0, %rax
	retq
