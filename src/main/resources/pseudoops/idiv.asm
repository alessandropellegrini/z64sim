# Signed 64-bit division: %rdx:%rax / source
# Output: %rax = quotient, %rdx = remainder
# Source operand is on the stack at 16(%rbp).
# Converts to unsigned, divides, then adjusts signs.
# Remainder sign follows the dividend sign (x86 semantics).
# Division by zero returns %rax = -1 (sentinel).
# Clobbers: only %rax and %rdx (the defined outputs).
__z64_idiv:
    pushq %rbp
    movq %rsp, %rbp
    pushq %rbx
    pushq %rcx
    pushq %rsi
    pushq %rdi
    pushq %r8
    pushq %r9
    pushq %r10
    movq 16(%rbp), %rcx
    # Check for division by zero
    cmpq $0, %rcx
    jz .__idiv_error
    # Save signs: %r9 = dividend sign, %r10 = quotient sign adjustment
    # If dividend (rdx:rax) is negative, negate to get absolute value
    xorq %r9, %r9
    xorq %r10, %r10
    cmpq $0, %rdx
    jns .__idiv_pos_dividend
    # Negate 128-bit dividend: NOT rdx, NOT rax, add 1 to rax, adc 0 to rdx
    notq %rax
    notq %rdx
    addq $1, %rax
    adcq $0, %rdx
    movq $1, %r9
    movq $1, %r10
.__idiv_pos_dividend:
    # If divisor is negative, negate it
    cmpq $0, %rcx
    jns .__idiv_pos_divisor
    negq %rcx
    xorq $1, %r10
.__idiv_pos_divisor:
    # Now do unsigned division of %rdx:%rax / %rcx
    movq %rdx, %rdi
    movq %rax, %r8
    xorq %rsi, %rsi
    movq $64, %rbx
.__idiv_loop_hi:
    cmpq $0, %rbx
    jz .__idiv_phase2
    subq $1, %rbx
    addq %rsi, %rsi
    addq %rdi, %rdi
    jnc .__idiv_nobit_hi
    addq $1, %rsi
.__idiv_nobit_hi:
    cmpq %rcx, %rsi
    jc .__idiv_loop_hi
    subq %rcx, %rsi
    jmp .__idiv_loop_hi
.__idiv_phase2:
    xorq %rax, %rax
    movq $64, %rbx
.__idiv_loop_lo:
    cmpq $0, %rbx
    jz .__idiv_adjust
    subq $1, %rbx
    addq %rax, %rax
    addq %rsi, %rsi
    addq %r8, %r8
    jnc .__idiv_nobit_lo
    addq $1, %rsi
.__idiv_nobit_lo:
    cmpq %rcx, %rsi
    jc .__idiv_loop_lo
    subq %rcx, %rsi
    addq $1, %rax
    jmp .__idiv_loop_lo
.__idiv_adjust:
    # Quotient in %rax, remainder in %rsi
    # Negate quotient if signs of dividend and divisor differed
    cmpq $0, %r10
    jz .__idiv_rem
    negq %rax
.__idiv_rem:
    # Negate remainder if dividend was negative (x86 convention)
    movq %rsi, %rdx
    cmpq $0, %r9
    jz .__idiv_done
    negq %rdx
    jmp .__idiv_done
.__idiv_error:
    movq $-1, %rax
    xorq %rdx, %rdx
.__idiv_done:
    popq %r10
    popq %r9
    popq %r8
    popq %rdi
    popq %rsi
    popq %rcx
    popq %rbx
    popq %rbp
    retq
