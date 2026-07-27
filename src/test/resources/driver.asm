.org 0x800
.data
   flag: .byte 0
   data: .long 0
   sum: .quad 0
   .equ DEV_IRQ, 0x0
   .equ DEV_STATUS, 0x1
   .equ DEV_REG, 0x2

.text
  main:
    sti
    outb %al, $DEV_STATUS
  .wait:
    cmpb $0, flag
    jz .wait
    movzlq data, %rax
    addq %rax, sum
  .stop:
    hlt
    jmp .stop

.driver 1
    pushq %rax
    inl $DEV_REG, %eax
    movl %eax, data
    movb $1, flag
    outb %al, $DEV_IRQ
    popq %rax
    iret
