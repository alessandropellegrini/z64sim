.org 0x800
.data
  .equ IN_STATUS, 0x10
  .equ IN_DATA, 0x11
  .equ OUT_STATUS, 0x20
  .equ OUT_DATA, 0x21
  value: .long 0
  done: .byte 0
.text
  main:
    outb %al, $IN_STATUS
   .wait_in:
    inb $IN_STATUS, %al
    btb $0, %al
    jnc .wait_in
    inl $IN_DATA, %eax
    movl %eax, value
    outl %eax, $OUT_DATA
    outb %al, $OUT_STATUS
   .wait_out:
    inb $OUT_STATUS, %al
    testb %al, %al
    jz .wait_out
    movb $1, done
    hlt
