.org 0x800
.data
  .equ TIMER_DELAY, 0x10
  .equ TIMER_STATUS, 0x11
  .equ TIMER_IRQ, 0x12
  fired: .byte 0
.text
  main:
    sti
    movl $200, %eax
    outl %eax, $TIMER_DELAY
    outb %al, $TIMER_STATUS
   .wait:
    cmpb $0, fired
    jz .wait
   .stop:
    hlt
    jmp .stop
.driver 1
    pushq %rax
    movb $1, fired
    outb %al, $TIMER_IRQ
    outb %al, $TIMER_STATUS
    popq %rax
    iret
