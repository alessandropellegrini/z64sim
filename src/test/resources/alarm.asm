.org 0x800
.data
  .equ IN_STATUS, 0x10
  .equ IN_IRQ, 0x11
  .equ IN_DATA, 0x12
  .equ ALARM_PORT, 0x20
.text
  main:
    sti
    outb %al, $IN_STATUS
   .stop:
    hlt
    jmp .stop
.driver 1
    pushq %rax
    inq $IN_DATA, %rax
    andb $1, %al
    outb %al, $ALARM_PORT
    outb %al, $IN_IRQ
    outb %al, $IN_STATUS
    popq %rax
    iret
