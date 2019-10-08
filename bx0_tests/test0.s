	.file "bx0_tests/test0.bx"
	.section .text
	.globl main
main:
	pushq %rbp
	movq %rsp, %rbp
	subq $432, %rsp

	movq $3, (%rsp)
	movq $2, 8(%rsp)
	movq (%rsp), %r11
	addq 8(%rsp), %r11
	movq %r11, 16(%rsp)
	movq $2, 24(%rsp)
	movq (%rsp), %r11
	subq 24(%rsp), %r11
	movq %r11, 32(%rsp)
	movq 16(%rsp), %rax
	imulq 32(%rsp)
	movq %rax, 40(%rsp)
	movq 40(%rsp), %r11
	negq %r11
	movq %r11, 48(%rsp)
	movq 48(%rsp), %rdi
	callq bx0_print

	movq $5, 56(%rsp)
	movq 56(%rsp), %rax
	imulq 56(%rsp)
	movq %rax, 64(%rsp)
	movq $1, 72(%rsp)
	movq $2, 80(%rsp)
	movq $4, 88(%rsp)
	movq 88(%rsp), %rax
	imulq 72(%rsp)
	movq %rax, 96(%rsp)
	movq 96(%rsp), %rax
	imulq 80(%rsp)
	movq %rax, 104(%rsp)
	movq 64(%rsp), %r11
	subq 104(%rsp), %r11
	movq %r11, 112(%rsp)
	movq 112(%rsp), %rdi
	callq bx0_print

	movq $1, 120(%rsp)
	movq $5, 128(%rsp)
	movq $3, 136(%rsp)
	movq 120(%rsp), %r11
	addq 128(%rsp), %r11
	movq %r11, 144(%rsp)
	movq 144(%rsp), %rdi
	callq bx0_print

	movq $2, 152(%rsp)
	movq 152(%rsp), %r11
	negq %r11
	movq %r11, 160(%rsp)
	movq $6, 168(%rsp)
	movq 168(%rsp), %rax
	cqto
	idivq 160(%rsp)
	movq %rax, 176(%rsp)
	movq 176(%rsp), %rdi
	callq bx0_print

	movq $6, 184(%rsp)
	movq 160(%rsp), %rax
	cqto
	idivq 184(%rsp)
	movq %rax, 192(%rsp)
	movq 192(%rsp), %rdi
	callq bx0_print

	movq $10, 200(%rsp)
	movq $3, 208(%rsp)
	movq 200(%rsp), %rax
	cqto
	idivq 208(%rsp)
	movq %rdx, 216(%rsp)
	movq 216(%rsp), %rdi
	callq bx0_print

	movq $3, 224(%rsp)
	movq 224(%rsp), %rax
	cqto
	idivq 200(%rsp)
	movq %rdx, 232(%rsp)
	movq 232(%rsp), %rdi
	callq bx0_print

	movq $15, 240(%rsp)
	movq 240(%rsp), %r11
	negq %r11
	movq %r11, 248(%rsp)
	movq 248(%rsp), %rax
	cqto
	idivq 200(%rsp)
	movq %rdx, 256(%rsp)
	movq 256(%rsp), %rdi
	callq bx0_print

	movq $120, 264(%rsp)
	movq $2, 272(%rsp)
	movb 272(%rsp), %cl
	movq 264(%rsp), %r11
	salq %cl, %r11
	movq %r11, 280(%rsp)
	movq 280(%rsp), %rdi
	callq bx0_print

	movq $34, 288(%rsp)
	movq $453543, 296(%rsp)
	movb 296(%rsp), %cl
	movq 288(%rsp), %r11
	salq %cl, %r11
	movq %r11, 304(%rsp)
	movq 304(%rsp), %rdi
	callq bx0_print

	movq $15435, 312(%rsp)
	movq 312(%rsp), %r11
	negq %r11
	movq %r11, 320(%rsp)
	movq $333, 328(%rsp)
	movb 328(%rsp), %cl
	movq 320(%rsp), %r11
	sarq %cl, %r11
	movq %r11, 336(%rsp)
	movq 336(%rsp), %rdi
	callq bx0_print

	movq $34, 344(%rsp)
	movq $4, 352(%rsp)
	movq 352(%rsp), %r11
	negq %r11
	movq %r11, 360(%rsp)
	movb 360(%rsp), %cl
	movq 344(%rsp), %r11
	sarq %cl, %r11
	movq %r11, 368(%rsp)
	movq 368(%rsp), %rdi
	callq bx0_print

	movq $91203128, 376(%rsp)
	movq 376(%rsp), %r11
	notq %r11
	movq %r11, 384(%rsp)
	movq 384(%rsp), %rdi
	callq bx0_print

	movq $20, 392(%rsp)
	movq 376(%rsp), %r11
	andq 392(%rsp), %r11
	movq %r11, 400(%rsp)
	movq 400(%rsp), %rdi
	callq bx0_print

	movq $4356, 408(%rsp)
	movq 408(%rsp), %r11
	negq %r11
	movq %r11, 416(%rsp)
	movq 376(%rsp), %r11
	orq 416(%rsp), %r11
	movq %r11, 424(%rsp)
	movq 424(%rsp), %rdi
	callq bx0_print

	movq %rbp, %rsp
	popq %rbp
	movq $0, %rax
	retq
