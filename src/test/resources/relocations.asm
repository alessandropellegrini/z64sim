# Comment
.org 0x800

.data
table: .quad .case0, .case1, .case2
var: .long 1

.text

main:
    movl var, %eax
    movq table(,%rax,8), %rax
    see: jmp *%rax

  .case0:
    movl $0, %ebx
    jmp .leave

  .case1:
    movl $1, %ebx
    jmp .leave

  .case2:
    movl $2, %ebx
    jmp .leave

  .leave:
    jz .end
    addl $1, var
    subl $1, %ebx
    jmp .end

  .end:
    hlt
