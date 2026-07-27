.org 0x800
.data
  .equ KBD_IRQ,  0x20
  .equ KBD_DATA, 0x21
  key_char: .fill 1, 1, 0

.text
  main:
    sti
    # Wait for a keypress (ISR will set key_char)
  .wait:
    cmpb $0, key_char
    jz .wait
  .stop:
    hlt
    jmp .stop

# ISR for the keyboard (IVN 1, hardwired at registration)
.driver 1
    pushq %rax
    pushq %rdx
    # Read the ASCII character from DATA
    movw $KBD_DATA, %dx
    inb  %dx, %al
    movb %al, key_char
    # Clear INT_REQ
    movw $KBD_IRQ, %dx
    outb %al, %dx
    popq %rdx
    popq %rax
    iret
