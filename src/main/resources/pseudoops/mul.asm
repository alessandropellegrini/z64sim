# Unsigned 64-bit multiplication: %rax * source → %rdx:%rax
# Source operand is on the stack at 16(%rbp).
# Shift-and-add algorithm with 128-bit accumulator.
# Clobbers: only %rax and %rdx (the defined outputs).
__z64_mul:
    pushq %rbp
    movq %rsp, %rbp
    pushq %rbx
    pushq %rcx
    pushq %rsi
    # %rbx = multiplier (from %rax), %rcx = multiplicand (from stack)
    # %rdx:%rax = 128-bit accumulator, %rsi = high carry word for multiplicand
    movq %rax, %rbx
    movq 16(%rbp), %rcx
    xorq %rsi, %rsi
    xorq %rax, %rax
    xorq %rdx, %rdx
    # Quick check: if either operand is zero, result is zero
    cmpq $0, %rcx
    jz .__mul_done
    cmpq $0, %rbx
    jz .__mul_done
.__mul_loop:
    # Test LSB of multiplier
    shrq $1, %rbx
    jnc .__mul_no_add
    addq %rcx, %rax
    adcq %rsi, %rdx
.__mul_no_add:
    # Shift multiplicand left (double it) using add for correct CF
    addq %rcx, %rcx
    adcq %rsi, %rsi
    # If multiplier is not zero, keep going
    cmpq $0, %rbx
    jnz .__mul_loop
.__mul_done:
    popq %rsi
    popq %rcx
    popq %rbx
    popq %rbp
    retq
