# Unsigned 64-bit division: %rdx:%rax / source
# Output: %rax = quotient, %rdx = remainder
# Source operand is on the stack at 16(%rbp).
# Binary long division (shift-and-subtract) with 128-bit dividend.
# Clobbers: only %rax and %rdx (the defined outputs).
__z64_div:
    pushq %rbp
    movq %rsp, %rbp
    pushq %rbx
    pushq %rcx
    pushq %rsi
    pushq %rdi
    pushq %r8
    movq 16(%rbp), %rcx
    # Check for division by zero
    cmpq $0, %rcx
    jz .__div_error
    # Save dividend halves: %rdi = high (%rdx), %r8 = low (%rax)
    movq %rdx, %rdi
    movq %rax, %r8
    # %rsi = running remainder
    xorq %rsi, %rsi
    movq $64, %rbx
    # Phase 1: shift bits from high word (%rdi) into remainder
.__div_loop_hi:
    cmpq $0, %rbx
    jz .__div_phase2
    subq $1, %rbx
    # Shift remainder left 1, bringing in MSB of %rdi via add
    addq %rsi, %rsi
    addq %rdi, %rdi
    jnc .__div_nobit_hi
    addq $1, %rsi
.__div_nobit_hi:
    cmpq %rcx, %rsi
    jc .__div_loop_hi
    subq %rcx, %rsi
    jmp .__div_loop_hi
.__div_phase2:
    # Phase 2: shift bits from low word (%r8) into remainder, build quotient in %rax
    xorq %rax, %rax
    movq $64, %rbx
.__div_loop_lo:
    cmpq $0, %rbx
    jz .__div_finish
    subq $1, %rbx
    # Shift quotient left
    addq %rax, %rax
    # Shift remainder left, bringing in MSB of %r8
    addq %rsi, %rsi
    addq %r8, %r8
    jnc .__div_nobit_lo
    addq $1, %rsi
.__div_nobit_lo:
    cmpq %rcx, %rsi
    jc .__div_loop_lo
    subq %rcx, %rsi
    addq $1, %rax
    jmp .__div_loop_lo
.__div_finish:
    movq %rsi, %rdx
    jmp .__div_done
.__div_error:
    movq $-1, %rax
    xorq %rdx, %rdx
.__div_done:
    popq %r8
    popq %rdi
    popq %rsi
    popq %rcx
    popq %rbx
    popq %rbp
    retq
