# Signed 64-bit multiplication: %rax * source → %rdx:%rax
# Source operand is on the stack at 16(%rbp).
# Converts to unsigned, multiplies, then adjusts sign.
# Clobbers: only %rax and %rdx (the defined outputs).
__z64_imul:
    pushq %rbp
    movq %rsp, %rbp
    pushq %rbx
    pushq %rcx
    pushq %rsi
    pushq %rdi
    pushq %r8
    # %rbx = |multiplier|, %rcx = |multiplicand|
    # %r8 = sign flag (0 = positive result, 1 = negative result)
    xorq %r8, %r8
    movq %rax, %rbx
    movq 16(%rbp), %rcx
    # Negate multiplier if negative
    cmpq $0, %rbx
    jns .__imul_pos_a
    negq %rbx
    addq $1, %r8
.__imul_pos_a:
    # Negate multiplicand if negative
    cmpq $0, %rcx
    jns .__imul_pos_b
    negq %rcx
    addq $1, %r8
.__imul_pos_b:
    # Now do unsigned multiplication of %rbx * %rcx
    xorq %rsi, %rsi
    xorq %rax, %rax
    xorq %rdx, %rdx
    cmpq $0, %rcx
    jz .__imul_adjust
    cmpq $0, %rbx
    jz .__imul_adjust
.__imul_loop:
    shrq $1, %rbx
    jnc .__imul_no_add
    addq %rcx, %rax
    adcq %rsi, %rdx
.__imul_no_add:
    addq %rcx, %rcx
    adcq %rsi, %rsi
    cmpq $0, %rbx
    jnz .__imul_loop
.__imul_adjust:
    # If r8 is odd (one operand was negative), negate the 128-bit result
    andq $1, %r8
    jz .__imul_done
    notq %rax
    notq %rdx
    addq $1, %rax
    adcq $0, %rdx
.__imul_done:
    popq %r8
    popq %rdi
    popq %rsi
    popq %rcx
    popq %rbx
    popq %rbp
    retq
