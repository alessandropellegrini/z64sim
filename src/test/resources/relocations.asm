.org 0x8000

.data
table: .quad .case0, .case1, .case2
var: .long 1

.text

main:
    movl var, %eax
    movq table(,%rax,8), %rax
    jmp *%rax

  .case0:
    movl $0, %ebx
    jmp .end

  .case1:
    movl $1, %ebx
    jmp .end

  .case2:
    movl $2, %ebx
    jmp .end

  .end:
    jz .hlt
    addl $1, var
    subl $1, %ebx
    jmp end

  .hlt:
    hlt
