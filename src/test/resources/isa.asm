.org 0x800

.text

main:
    nop
    int $0x80
    hlt
    movb $0x80, %al
    movw 0x800, %ax
    movl %eax, 0x800
    movq $1, 0x800
    mov %rax, 0x800
    mov %eax, 0x800
    mov %ax, 0x800
    mov %al, 0x800
    mov $1, %al
    mov $1, %ax
    mov $1, %eax
    mov $1, %rax
    movb (%rax, %rbx, 1), %dl
    movw (%rax, %rbx, 2), %dx
    movl (%rax, %rbx, 4), %edx
    movq (%rax, %rbx, 8), %rdx
    mov (%rax, %rbx, 1), %dl
    mov (%rax, %rbx, 2), %dx
    mov (%rax, %rbx, 4), %edx
    mov (%rax, %rbx, 8), %rdx
    movb (, %rbx, 1), %dl
    movw (, %rbx, 2), %dx
    movl (, %rbx, 4), %edx
    movq (, %rbx, 8), %rdx
    mov (, %rbx, 1), %dl
    mov (, %rbx, 2), %dx
    mov (, %rbx, 4), %edx
    mov (, %rbx, 8), %rdx
    movb (%rax), %dl
    movw (%rax), %dx
    movl (%rax), %edx
    movq (%rax), %rdx
    mov (%rax), %dl
    mov (%rax), %dx
    mov (%rax), %edx
    mov (%rax), %rdx
    leab (%rax, %rbx, 1), %dl
    leaw (%rax, %rbx, 2), %dx
    leal (%rax, %rbx, 4), %edx
    leaq (%rax, %rbx, 8), %rdx
    lea (%rax, %rbx, 1), %dl
    lea (%rax, %rbx, 2), %dx
    lea (%rax, %rbx, 4), %edx
    lea (%rax, %rbx, 8), %rdx
    leab (, %rbx, 1), %dl
    leaw (, %rbx, 2), %dx
    leal (, %rbx, 4), %edx
    leaq (, %rbx, 8), %rdx
    lea (, %rbx, 1), %dl
    lea (, %rbx, 2), %dx
    lea (, %rbx, 4), %edx
    lea (, %rbx, 8), %rdx
    leab (%rax), %dl
    leaw (%rax), %dx
    leal (%rax), %edx
    leaq (%rax), %rdx
    lea (%rax), %dl
    lea (%rax), %dx
    lea (%rax), %edx
    lea (%rax), %rdx
    movb %dl, (%rax, %rbx, 1)
    movw %dx, (%rax, %rbx, 2)
    movl %edx, (%rax, %rbx, 4)
    movq %rdx, (%rax, %rbx, 8)
    movb %dl, (, %rbx, 1)
    movw %dx, (, %rbx, 2)
    movl %edx, (, %rbx, 4)
    movq %rdx, (, %rbx, 8)
    movb %dl, (%rbx)
    movw %dx, (%rbx)
    movl %edx, (%rbx)
    movq %rdx, (%rbx)
    mov %dl, (%rax, %rbx, 1)
    mov %dx, (%rax, %rbx, 2)
    mov %edx, (%rax, %rbx, 4)
    mov %rdx, (%rax, %rbx, 8)
    mov %dl, (, %rbx, 1)
    mov %dx, (, %rbx, 2)
    mov %edx, (, %rbx, 4)
    mov %rdx, (, %rbx, 8)
    mov %dl, (%rbx)
    mov %dx, (%rbx)
    mov %edx, (%rbx)
    mov %rdx, (%rbx)
    movsbw %al, %ax
    movsbl 2048, %eax
    movsbq 2048, %rax
    movswl %ax, %eax
    movswq 2048, %rax
    movslq 2048, %rax
    movzbw %al, %ax
    movzbl 2048, %eax
    movzbq 2048, %rax
    movzwl %ax, %eax
    movzwq 2048, %rax
    movzlq 2048, %rax
    movsbw (%rax), %ax
    movsbl (%rax), %eax
    movsbq (%rax), %rax
    movswl (%rax), %eax
    movswq (%rax), %rax
    movslq (%rax), %rax
    movzbw (%rax), %ax
    movzbl (%rax), %eax
    movzbq (%rax), %rax
    movzwl (%rax), %eax
    movzwq (%rax), %rax
    movzlq (%rax), %rax
    movsbw %al, %ax
    movsbl %al, %eax
    movsbq %al, %rax
    movswl %ax, %eax
    movswq %ax, %rax
    movslq %eax, %rax
    movzbw %al, %ax
    movzbl %al, %eax
    movzbq %al, %rax
    movzwl %ax, %eax
    movzwq %ax, %rax
    movzlq %eax, %rax
    movsbw %al, %ax
    movsbl %al, %eax
    movsbq %al, %rax
    movswl %ax, %eax
    movswq %ax, %rax
    movslq %eax, %rax
    movzbw %al, %ax
    movzbl %al, %eax
    movzbq %al, %rax
    movzwl %ax, %eax
    movzwq %ax, %rax
    movzlq %eax, %rax
    movsbw (%rax, %rbx, 1), %ax
    movsbl (%rax, %rcx, 2), %eax
    movsbq (%rax, %rdx, 4), %rax
    movswl (%rax, %rsi, 8), %eax
    movswq (%rax, %rdi, 1), %rax
    movslq (%rax, %rsp, 2), %rax
    movzbw (%rax, %rbp, 4), %ax
    movzbl (%rax, %rbx, 8), %eax
    movzbq (%rax, %rbx, 1), %rax
    movzwl (%rax, %rbx, 2), %eax
    movzwq (%rax, %rbx, 4), %rax
    movzlq (%rax, %rbx, 8), %rax
    pushb $1
    pushw $1
    pushl $1
    pushq $1
    pushb %dl
    pushw %dx
    pushl %edx
    pushq %rdx
    push %dl
    push %dx
    push %edx
    push %rdx
    pushb (%rax)
    pushw (%rax)
    pushl (%rax)
    pushq (%rax)
    popb %dl
    popw %dx
    popl %edx
    popq %rdx
    popb (%rax)
    popw (%rax)
    popl (%rax)
    popq (%rax)
    pop %dl
    pop %dx
    pop %edx
    pop %rdx
    pushfw
    pushfl
    pushfq
    popfw
    popfl
    popfq
    movsb
    movsw
    movsl
    movsq
    stosb
    stosw
    stosl
    stosq
    addb $0x80, %al
    addw 0x800, %ax
    addl %eax, 0x800
    addq $1, 0x800
    add %rax, 0x800
    add %eax, 0x800
    add %ax, 0x800
    add %al, 0x800
    add $1, %al
    add $1, %ax
    add $1, %eax
    add $1, %rax
    addb (%rax, %rbx, 1), %dl
    addw (%rax, %rbx, 2), %dx
    addl (%rax, %rbx, 4), %edx
    addq (%rax, %rbx, 8), %rdx
    add (%rax, %rbx, 1), %dl
    add (%rax, %rbx, 2), %dx
    add (%rax, %rbx, 4), %edx
    add (%rax, %rbx, 8), %rdx
    addb (, %rbx, 1), %dl
    addw (, %rbx, 2), %dx
    addl (, %rbx, 4), %edx
    addq (, %rbx, 8), %rdx
    add (, %rbx, 1), %dl
    add (, %rbx, 2), %dx
    add (, %rbx, 4), %edx
    add (, %rbx, 8), %rdx
    addb (%rax), %dl
    addw (%rax), %dx
    addl (%rax), %edx
    addq (%rax), %rdx
    add (%rax), %dl
    add (%rax), %dx
    add (%rax), %edx
    add (%rax), %rdx
    addb %dl, (%rax, %rbx, 1)
    addw %dx, (%rax, %rbx, 2)
    addl %edx, (%rax, %rbx, 4)
    addq %rdx, (%rax, %rbx, 8)
    addb %dl, (, %rbx, 1)
    addw %dx, (, %rbx, 2)
    addl %edx, (, %rbx, 4)
    addq %rdx, (, %rbx, 8)
    addb %dl, (%rbx)
    addw %dx, (%rbx)
    addl %edx, (%rbx)
    addq %rdx, (%rbx)
    add %dl, (%rax, %rbx, 1)
    add %dx, (%rax, %rbx, 2)
    add %edx, (%rax, %rbx, 4)
    add %rdx, (%rax, %rbx, 8)
    add %dl, (, %rbx, 1)
    add %dx, (, %rbx, 2)
    add %edx, (, %rbx, 4)
    add %rdx, (, %rbx, 8)
    add %dl, (%rbx)
    add %dx, (%rbx)
    add %edx, (%rbx)
    add %rdx, (%rbx)
    subb $0x80, %al
    subw 0x800, %ax
    subl %eax, 0x800
    subq $1, 0x800
    sub %rax, 0x800
    sub %eax, 0x800
    sub %ax, 0x800
    sub %al, 0x800
    sub $1, %al
    sub $1, %ax
    sub $1, %eax
    sub $1, %rax
    subb (%rax, %rbx, 1), %dl
    subw (%rax, %rbx, 2), %dx
    subl (%rax, %rbx, 4), %edx
    subq (%rax, %rbx, 8), %rdx
    sub (%rax, %rbx, 1), %dl
    sub (%rax, %rbx, 2), %dx
    sub (%rax, %rbx, 4), %edx
    sub (%rax, %rbx, 8), %rdx
    subb (, %rbx, 1), %dl
    subw (, %rbx, 2), %dx
    subl (, %rbx, 4), %edx
    subq (, %rbx, 8), %rdx
    sub (, %rbx, 1), %dl
    sub (, %rbx, 2), %dx
    sub (, %rbx, 4), %edx
    sub (, %rbx, 8), %rdx
    subb (%rax), %dl
    subw (%rax), %dx
    subl (%rax), %edx
    subq (%rax), %rdx
    sub (%rax), %dl
    sub (%rax), %dx
    sub (%rax), %edx
    sub (%rax), %rdx
    subb %dl, (%rax, %rbx, 1)
    subw %dx, (%rax, %rbx, 2)
    subl %edx, (%rax, %rbx, 4)
    subq %rdx, (%rax, %rbx, 8)
    subb %dl, (, %rbx, 1)
    subw %dx, (, %rbx, 2)
    subl %edx, (, %rbx, 4)
    subq %rdx, (, %rbx, 8)
    subb %dl, (%rbx)
    subw %dx, (%rbx)
    subl %edx, (%rbx)
    subq %rdx, (%rbx)
    sub %dl, (%rax, %rbx, 1)
    sub %dx, (%rax, %rbx, 2)
    sub %edx, (%rax, %rbx, 4)
    sub %rdx, (%rax, %rbx, 8)
    sub %dl, (, %rbx, 1)
    sub %dx, (, %rbx, 2)
    sub %edx, (, %rbx, 4)
    sub %rdx, (, %rbx, 8)
    sub %dl, (%rbx)
    sub %dx, (%rbx)
    sub %edx, (%rbx)
    sub %rdx, (%rbx)
    adcb $0x80, %al
    adcw 0x800, %ax
    adcl %eax, 0x800
    adcq $1, 0x800
    adc %rax, 0x800
    adc %eax, 0x800
    adc %ax, 0x800
    adc %al, 0x800
    adc $1, %al
    adc $1, %ax
    adc $1, %eax
    adc $1, %rax
    adcb (%rax, %rbx, 1), %dl
    adcw (%rax, %rbx, 2), %dx
    adcl (%rax, %rbx, 4), %edx
    adcq (%rax, %rbx, 8), %rdx
    adc (%rax, %rbx, 1), %dl
    adc (%rax, %rbx, 2), %dx
    adc (%rax, %rbx, 4), %edx
    adc (%rax, %rbx, 8), %rdx
    adcb (, %rbx, 1), %dl
    adcw (, %rbx, 2), %dx
    adcl (, %rbx, 4), %edx
    adcq (, %rbx, 8), %rdx
    adc (, %rbx, 1), %dl
    adc (, %rbx, 2), %dx
    adc (, %rbx, 4), %edx
    adc (, %rbx, 8), %rdx
    adcb (%rax), %dl
    adcw (%rax), %dx
    adcl (%rax), %edx
    adcq (%rax), %rdx
    adc (%rax), %dl
    adc (%rax), %dx
    adc (%rax), %edx
    adc (%rax), %rdx
    adcb %dl, (%rax, %rbx, 1)
    adcw %dx, (%rax, %rbx, 2)
    adcl %edx, (%rax, %rbx, 4)
    adcq %rdx, (%rax, %rbx, 8)
    adcb %dl, (, %rbx, 1)
    adcw %dx, (, %rbx, 2)
    adcl %edx, (, %rbx, 4)
    adcq %rdx, (, %rbx, 8)
    adcb %dl, (%rbx)
    adcw %dx, (%rbx)
    adcl %edx, (%rbx)
    adcq %rdx, (%rbx)
    adc %dl, (%rax, %rbx, 1)
    adc %dx, (%rax, %rbx, 2)
    adc %edx, (%rax, %rbx, 4)
    adc %rdx, (%rax, %rbx, 8)
    adc %dl, (, %rbx, 1)
    adc %dx, (, %rbx, 2)
    adc %edx, (, %rbx, 4)
    adc %rdx, (, %rbx, 8)
    adc %dl, (%rbx)
    adc %dx, (%rbx)
    adc %edx, (%rbx)
    adc %rdx, (%rbx)
    sbbb $0x80, %al
    sbbw 0x800, %ax
    sbbl %eax, 0x800
    sbbq $1, 0x800
    sbb %rax, 0x800
    sbb %eax, 0x800
    sbb %ax, 0x800
    sbb %al, 0x800
    sbb $1, %al
    sbb $1, %ax
    sbb $1, %eax
    sbb $1, %rax
    sbbb (%rax, %rbx, 1), %dl
    sbbw (%rax, %rbx, 2), %dx
    sbbl (%rax, %rbx, 4), %edx
    sbbq (%rax, %rbx, 8), %rdx
    sbb (%rax, %rbx, 1), %dl
    sbb (%rax, %rbx, 2), %dx
    sbb (%rax, %rbx, 4), %edx
    sbb (%rax, %rbx, 8), %rdx
    sbbb (, %rbx, 1), %dl
    sbbw (, %rbx, 2), %dx
    sbbl (, %rbx, 4), %edx
    sbbq (, %rbx, 8), %rdx
    sbb (, %rbx, 1), %dl
    sbb (, %rbx, 2), %dx
    sbb (, %rbx, 4), %edx
    sbb (, %rbx, 8), %rdx
    sbbb (%rax), %dl
    sbbw (%rax), %dx
    sbbl (%rax), %edx
    sbbq (%rax), %rdx
    sbb (%rax), %dl
    sbb (%rax), %dx
    sbb (%rax), %edx
    sbb (%rax), %rdx
    sbbb %dl, (%rax, %rbx, 1)
    sbbw %dx, (%rax, %rbx, 2)
    sbbl %edx, (%rax, %rbx, 4)
    sbbq %rdx, (%rax, %rbx, 8)
    sbbb %dl, (, %rbx, 1)
    sbbw %dx, (, %rbx, 2)
    sbbl %edx, (, %rbx, 4)
    sbbq %rdx, (, %rbx, 8)
    sbbb %dl, (%rbx)
    sbbw %dx, (%rbx)
    sbbl %edx, (%rbx)
    sbbq %rdx, (%rbx)
    sbb %dl, (%rax, %rbx, 1)
    sbb %dx, (%rax, %rbx, 2)
    sbb %edx, (%rax, %rbx, 4)
    sbb %rdx, (%rax, %rbx, 8)
    sbb %dl, (, %rbx, 1)
    sbb %dx, (, %rbx, 2)
    sbb %edx, (, %rbx, 4)
    sbb %rdx, (, %rbx, 8)
    sbb %dl, (%rbx)
    sbb %dx, (%rbx)
    sbb %edx, (%rbx)
    sbb %rdx, (%rbx)
    testb $0x80, %al
    testw 0x800, %ax
    testl %eax, 0x800
    testq $1, 0x800
    test %rax, 0x800
    test %eax, 0x800
    test %ax, 0x800
    test %al, 0x800
    test $1, %al
    test $1, %ax
    test $1, %eax
    test $1, %rax
    testb (%rax, %rbx, 1), %dl
    testw (%rax, %rbx, 2), %dx
    testl (%rax, %rbx, 4), %edx
    testq (%rax, %rbx, 8), %rdx
    test (%rax, %rbx, 1), %dl
    test (%rax, %rbx, 2), %dx
    test (%rax, %rbx, 4), %edx
    test (%rax, %rbx, 8), %rdx
    testb (, %rbx, 1), %dl
    testw (, %rbx, 2), %dx
    testl (, %rbx, 4), %edx
    testq (, %rbx, 8), %rdx
    test (, %rbx, 1), %dl
    test (, %rbx, 2), %dx
    test (, %rbx, 4), %edx
    test (, %rbx, 8), %rdx
    testb (%rax), %dl
    testw (%rax), %dx
    testl (%rax), %edx
    testq (%rax), %rdx
    test (%rax), %dl
    test (%rax), %dx
    test (%rax), %edx
    test (%rax), %rdx
    testb %dl, (%rax, %rbx, 1)
    testw %dx, (%rax, %rbx, 2)
    testl %edx, (%rax, %rbx, 4)
    testq %rdx, (%rax, %rbx, 8)
    testb %dl, (, %rbx, 1)
    testw %dx, (, %rbx, 2)
    testl %edx, (, %rbx, 4)
    testq %rdx, (, %rbx, 8)
    testb %dl, (%rbx)
    testw %dx, (%rbx)
    testl %edx, (%rbx)
    testq %rdx, (%rbx)
    test %dl, (%rax, %rbx, 1)
    test %dx, (%rax, %rbx, 2)
    test %edx, (%rax, %rbx, 4)
    test %rdx, (%rax, %rbx, 8)
    test %dl, (, %rbx, 1)
    test %dx, (, %rbx, 2)
    test %edx, (, %rbx, 4)
    test %rdx, (, %rbx, 8)
    test %dl, (%rbx)
    test %dx, (%rbx)
    test %edx, (%rbx)
    test %rdx, (%rbx)
    andb $0x80, %al
    andw 0x800, %ax
    andl %eax, 0x800
    andq $1, 0x800
    and %rax, 0x800
    and %eax, 0x800
    and %ax, 0x800
    and %al, 0x800
    and $1, %al
    and $1, %ax
    and $1, %eax
    and $1, %rax
    andb (%rax, %rbx, 1), %dl
    andw (%rax, %rbx, 2), %dx
    andl (%rax, %rbx, 4), %edx
    andq (%rax, %rbx, 8), %rdx
    and (%rax, %rbx, 1), %dl
    and (%rax, %rbx, 2), %dx
    and (%rax, %rbx, 4), %edx
    and (%rax, %rbx, 8), %rdx
    andb (, %rbx, 1), %dl
    andw (, %rbx, 2), %dx
    andl (, %rbx, 4), %edx
    andq (, %rbx, 8), %rdx
    and (, %rbx, 1), %dl
    and (, %rbx, 2), %dx
    and (, %rbx, 4), %edx
    and (, %rbx, 8), %rdx
    andb (%rax), %dl
    andw (%rax), %dx
    andl (%rax), %edx
    andq (%rax), %rdx
    and (%rax), %dl
    and (%rax), %dx
    and (%rax), %edx
    and (%rax), %rdx
    andb %dl, (%rax, %rbx, 1)
    andw %dx, (%rax, %rbx, 2)
    andl %edx, (%rax, %rbx, 4)
    andq %rdx, (%rax, %rbx, 8)
    andb %dl, (, %rbx, 1)
    andw %dx, (, %rbx, 2)
    andl %edx, (, %rbx, 4)
    andq %rdx, (, %rbx, 8)
    andb %dl, (%rbx)
    andw %dx, (%rbx)
    andl %edx, (%rbx)
    andq %rdx, (%rbx)
    and %dl, (%rax, %rbx, 1)
    and %dx, (%rax, %rbx, 2)
    and %edx, (%rax, %rbx, 4)
    and %rdx, (%rax, %rbx, 8)
    and %dl, (, %rbx, 1)
    and %dx, (, %rbx, 2)
    and %edx, (, %rbx, 4)
    and %rdx, (, %rbx, 8)
    and %dl, (%rbx)
    and %dx, (%rbx)
    and %edx, (%rbx)
    and %rdx, (%rbx)
    orb $0x80, %al
    orw 0x800, %ax
    orl %eax, 0x800
    orq $1, 0x800
    or %rax, 0x800
    or %eax, 0x800
    or %ax, 0x800
    or %al, 0x800
    or $1, %al
    or $1, %ax
    or $1, %eax
    or $1, %rax
    orb (%rax, %rbx, 1), %dl
    orw (%rax, %rbx, 2), %dx
    orl (%rax, %rbx, 4), %edx
    orq (%rax, %rbx, 8), %rdx
    or (%rax, %rbx, 1), %dl
    or (%rax, %rbx, 2), %dx
    or (%rax, %rbx, 4), %edx
    or (%rax, %rbx, 8), %rdx
    orb (, %rbx, 1), %dl
    orw (, %rbx, 2), %dx
    orl (, %rbx, 4), %edx
    orq (, %rbx, 8), %rdx
    or (, %rbx, 1), %dl
    or (, %rbx, 2), %dx
    or (, %rbx, 4), %edx
    or (, %rbx, 8), %rdx
    orb (%rax), %dl
    orw (%rax), %dx
    orl (%rax), %edx
    orq (%rax), %rdx
    or (%rax), %dl
    or (%rax), %dx
    or (%rax), %edx
    or (%rax), %rdx
    orb %dl, (%rax, %rbx, 1)
    orw %dx, (%rax, %rbx, 2)
    orl %edx, (%rax, %rbx, 4)
    orq %rdx, (%rax, %rbx, 8)
    orb %dl, (, %rbx, 1)
    orw %dx, (, %rbx, 2)
    orl %edx, (, %rbx, 4)
    orq %rdx, (, %rbx, 8)
    orb %dl, (%rbx)
    orw %dx, (%rbx)
    orl %edx, (%rbx)
    orq %rdx, (%rbx)
    or %dl, (%rax, %rbx, 1)
    or %dx, (%rax, %rbx, 2)
    or %edx, (%rax, %rbx, 4)
    or %rdx, (%rax, %rbx, 8)
    or %dl, (, %rbx, 1)
    or %dx, (, %rbx, 2)
    or %edx, (, %rbx, 4)
    or %rdx, (, %rbx, 8)
    or %dl, (%rbx)
    or %dx, (%rbx)
    or %edx, (%rbx)
    or %rdx, (%rbx)
    xorb $0x80, %al
    xorw 0x800, %ax
    xorl %eax, 0x800
    xorq $1, 0x800
    xor %rax, 0x800
    xor %eax, 0x800
    xor %ax, 0x800
    xor %al, 0x800
    xor $1, %al
    xor $1, %ax
    xor $1, %eax
    xor $1, %rax
    xorb (%rax, %rbx, 1), %dl
    xorw (%rax, %rbx, 2), %dx
    xorl (%rax, %rbx, 4), %edx
    xorq (%rax, %rbx, 8), %rdx
    xor (%rax, %rbx, 1), %dl
    xor (%rax, %rbx, 2), %dx
    xor (%rax, %rbx, 4), %edx
    xor (%rax, %rbx, 8), %rdx
    xorb (, %rbx, 1), %dl
    xorw (, %rbx, 2), %dx
    xorl (, %rbx, 4), %edx
    xorq (, %rbx, 8), %rdx
    xor (, %rbx, 1), %dl
    xor (, %rbx, 2), %dx
    xor (, %rbx, 4), %edx
    xor (, %rbx, 8), %rdx
    xorb (%rax), %dl
    xorw (%rax), %dx
    xorl (%rax), %edx
    xorq (%rax), %rdx
    xor (%rax), %dl
    xor (%rax), %dx
    xor (%rax), %edx
    xor (%rax), %rdx
    xorb %dl, (%rax, %rbx, 1)
    xorw %dx, (%rax, %rbx, 2)
    xorl %edx, (%rax, %rbx, 4)
    xorq %rdx, (%rax, %rbx, 8)
    xorb %dl, (, %rbx, 1)
    xorw %dx, (, %rbx, 2)
    xorl %edx, (, %rbx, 4)
    xorq %rdx, (, %rbx, 8)
    xorb %dl, (%rbx)
    xorw %dx, (%rbx)
    xorl %edx, (%rbx)
    xorq %rdx, (%rbx)
    xor %dl, (%rax, %rbx, 1)
    xor %dx, (%rax, %rbx, 2)
    xor %edx, (%rax, %rbx, 4)
    xor %rdx, (%rax, %rbx, 8)
    xor %dl, (, %rbx, 1)
    xor %dx, (, %rbx, 2)
    xor %edx, (, %rbx, 4)
    xor %rdx, (, %rbx, 8)
    xor %dl, (%rbx)
    xor %dx, (%rbx)
    xor %edx, (%rbx)
    xor %rdx, (%rbx)
    negb %dl
    negw %dx
    negl %edx
    negq %rdx
    neg %dl
    neg %dx
    neg %edx
    neg %rdx
    negb (%rax, %rbx, 1)
    negw (%rax, %rbx, 2)
    negl (%rax, %rbx, 4)
    negq (%rax, %rbx, 8)
    negb (, %rbx, 1)
    negw (, %rbx, 2)
    negl (, %rbx, 4)
    negq (, %rbx, 8)
    negb (%rbx)
    negw (%rbx)
    negl (%rbx)
    negq (%rbx)
    notb %dl
    notw %dx
    notl %edx
    notq %rdx
    not %dl
    not %dx
    not %edx
    not %rdx
    notb (%rax, %rbx, 1)
    notw (%rax, %rbx, 2)
    notl (%rax, %rbx, 4)
    notq (%rax, %rbx, 8)
    notb (, %rbx, 1)
    notw (, %rbx, 2)
    notl (, %rbx, 4)
    notq (, %rbx, 8)
    notb (%rbx)
    notw (%rbx)
    notl (%rbx)
    notq (%rbx)
    btb $1, %dl
    btw $2, %dx
    btl $3, %edx
    btq $4, %rdx
    bt $5, %dl
    bt $6, %dx
    bt $7, %edx
    bt $8, %rdx
    btb $1, (%rax, %rbx, 1)
    btw $9, (%rax, %rbx, 2)
    btl $10, (%rax, %rbx, 4)
    btq $11, (%rax, %rbx, 8)
    btb $1, (, %rbx, 1)
    btw $12, (, %rbx, 2)
    btl $1, (, %rbx, 4)
    btq $13, (, %rbx, 8)
    btb $14, (%rbx)
    btw $15, (%rbx)
    btl $16, (%rbx)
    btq $17, (%rbx)
    shl $1, %dl
    shl $2, %dx
    shl $3, %edx
    shl $4, %rdx
    shlb $5, %dl
    shlw $6, %dx
    shll $7, %edx
    shlq $8, %rdx
    sal $1, %dl
    sal $2, %dx
    sal $3, %edx
    sal $4, %rdx
    salb $5, %dl
    salw $6, %dx
    sall $7, %edx
    salq $8, %rdx
    sar $1, %dl
    sar $2, %dx
    sar $3, %edx
    sar $4, %rdx
    sarb $5, %dl
    sarw $6, %dx
    sarl $7, %edx
    sarq $8, %rdx
    shr $1, %dl
    shr $2, %dx
    shr $3, %edx
    shr $4, %rdx
    shrb $5, %dl
    shrw $6, %dx
    shrl $7, %edx
    shrq $8, %rdx
    rcl $1, %dl
    rcl $2, %dx
    rcl $3, %edx
    rcl $4, %rdx
    rclb $5, %dl
    rclw $6, %dx
    rcll $7, %edx
    rclq $8, %rdx
    rcr $1, %dl
    rcr $2, %dx
    rcr $3, %edx
    rcr $4, %rdx
    rcrb $5, %dl
    rcrw $6, %dx
    rcrl $7, %edx
    rcrq $8, %rdx
    rol $1, %dl
    rol $2, %dx
    rol $3, %edx
    rol $4, %rdx
    rolb $5, %dl
    rolw $6, %dx
    roll $7, %edx
    rolq $8, %rdx
    ror $1, %dl
    ror $2, %dx
    ror $3, %edx
    ror $4, %rdx
    rorb $5, %dl
    rorw $6, %dx
    rorl $7, %edx
    rorq $8, %rdx
    clc
    clp
    cld
    cli
    clz
    clo
    cls
    stc
    stp
    std
    sti
    stz
    sto
    sts
    call *%rax
    jmp main
    jmp *%rax
    jmpq *%rax
    callq *%rax
    call main
    ret
    retq
    jc main
    jp 0x800
    jz 0x800
    js main
    jo main
    jnc main
    jnp main
    jnz main
    jns main
    jno main
    in $1, %al
    in $2, %ax
    in $3, %eax
    inb $1, %al
    inw $2, %ax
    inl $3, %eax
    in %dx, %al
    in %dx, %ax
    in %dx, %eax
    inb %dx, %al
    inw %dx, %ax
    inl %dx, %eax
    out %al, $1
    out %ax, $2
    out %eax, $3
    outb %al, $1
    outw %ax, $2
    outl %eax, $3
    out %al, %dx
    out %ax, %dx
    out %eax, %dx
    outb %al, %dx
    outw %ax, %dx
    outl %eax, %dx
    insb
    insw
    insl
    outsb
    outsw
    outsl

here:
.driver 1
    nop
    iret

.driver 2
    nop
    iretq
