# keyboard_terminal.asm — Echo keyboard input to the terminal.
#
# Keyboard (interrupt-driven):
#   INT_REQ @ 0x20, DATA @ 0x21
#   IVN = 1 (hardwired at registration)
#
# Terminal (busy-waiting):
#   STATUS @ 0x30, DATA @ 0x31
#
# The keyboard ISR reads the pressed character and stores it in
# a shared variable. The main loop polls that variable and, when
# a new character appears, sends it to the terminal.
.org 0x800
.data
  .equ KBD_IRQ,     0x20
  .equ KBD_DATA,    0x21
  .equ TERM_STATUS, 0x30
  .equ TERM_DATA,   0x31
  new_char: .fill 1, 1, 0     # character received from keyboard (0 = none)
.text
  main:
    sti                        # enable interrupts
  .loop:
    cmpb $0, new_char          # any new character?
    jz .loop                   # no — keep polling
    # A character is available: send it to the terminal
    movb new_char, %al         # load the character
    movb $0, new_char          # clear the flag for the next keypress
    outb %al, $TERM_DATA       # write character to terminal DATA
    outb %al, $TERM_STATUS     # start the terminal (write STATUS)
    jmp .loop                  # back to polling
# Keyboard ISR (IVN 1)
.driver 1
    pushq %rax
    pushq %rdx
    # Read the ASCII code from the keyboard DATA register
    movw $KBD_DATA, %dx
    inb  %dx, %al
    movb %al, new_char         # store for the main loop
    # Clear INT_REQ
    movw $KBD_IRQ, %dx
    outb %al, %dx
    popq %rdx
    popq %rax
    iret
