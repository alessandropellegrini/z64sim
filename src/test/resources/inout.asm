.org 0x800
.data
  .equ DEV_STATUS, 0x20
  .equ DEV_MODE,   0x21
  .equ DEV_DATA,   0x22

.text
  main:

  # Output mode test: set MODE = 1 (output)
    movw $DEV_MODE, %dx
    movb $1, %al
    outb %al, %dx

  # Write 0x12345678 to DATA
    movw $DEV_DATA, %dx
    movl $0x12345678, %eax
    outl %eax, %dx

  # Write STATUS to start the device
    movw $DEV_STATUS, %dx
    outb %al, %dx

  # Busy-wait until STATUS = 1
  output_wait:
    inb  %dx, %al
    testb %al, %al
    jz   output_wait

  # Input mode test: set MODE = 0 (input)
    movw $DEV_MODE, %dx
    movb $0, %al
    outb %al, %dx

  # Write STATUS to start the device (produces a value)
    movw $DEV_STATUS, %dx
    movb $0, %al
    outb %al, %dx

  # Busy-wait until STATUS = 1
  input_wait:
    inb  %dx, %al
    testb %al, %al
    jz   input_wait

  # Read the value from DATA into %eax
    movw $DEV_DATA, %dx
    inl  %dx, %eax

  # Store the read value at memory address 0xA00 (for verification)
    movl %eax, 0xA00

    hlt
