.org 0x800
.data
  .equ DEV_STATUS, 0x10
  .equ DEV_REG, 0x11
  done: .byte 0
  result: .long 0
.text
  main:
    movw $DEV_STATUS, %dx
    outb %al, %dx
   .bw:
    inb %dx, %al
    testb %al, %al
    jnz .out
    inb $DEV_STATUS, %al
    btb $0, %al
    jnc .bw
   .out:
    inl $DEV_REG, %eax
    movl %eax, result
    movb $1, done
    hlt
