.org 0x800
.data
  .equ TERM_STATUS, 0x20
  .equ TERM_DATA,   0x21
  .equ MSG_LEN, 13
  hello: .ascii "Hello, World!"

.text
  main:
    movq $hello, %rsi
    movq $MSG_LEN, %rcx
  loop:
    movw $TERM_DATA, %dx
    movb (%rsi), %al
    outb %al, %dx
    movw $TERM_STATUS, %dx
    movb $0, %al
    outb %al, %dx
  wait:
    inb  %dx, %al
    testb %al, %al
    jz   wait
    addq $1, %rsi
    subq $1, %rcx
    jnz  loop
    hlt
