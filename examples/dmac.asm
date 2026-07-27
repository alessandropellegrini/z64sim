.org 0x800
.data
  .equ DEV_DATA, 0x51
  buffer: .fill 8, 1, 0

.text
  main:
    cld
    movw $DEV_DATA, %dx
    movq $buffer, %rdi
    movq $8, %rcx
    insb
    hlt
