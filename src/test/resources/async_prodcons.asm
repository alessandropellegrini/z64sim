.org 0x800
.data
  .equ IN_STATUS, 0x10
  .equ IN_IRQ, 0x11
  .equ IN_DATA, 0x12
  .equ OUT_STATUS, 0x20
  .equ OUT_IRQ, 0x21
  .equ OUT_DATA, 0x22
  value: .quad 0
  done: .byte 0
.text
  main:
    sti
    outb %al, $IN_STATUS
   .wait:
    cmpb $0, done
    jz .wait
   .stop:
    hlt
    jmp .stop

.driver 1
    pushq %rax
    inq $IN_DATA, %rax
    movq %rax, value
    outl %eax, $OUT_DATA
    outb %al, $OUT_STATUS
    outb %al, $IN_IRQ
    popq %rax
    iret

.driver 2
    pushq %rax
    movb $1, done
    outb %al, $OUT_IRQ
    popq %rax
    iret
