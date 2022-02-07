.org 0x8000

.data

.equ DEVICE, 1
.equ DEVICE_ADDR, 0x123
variable: .word 0
array: .word 0, 0, 0
array_size = . - array

.bss

.comm array_dest, 8

.text

    movl $0, %eax
    movsbw (%rax), %dx
    salw $3, %dx
    movw %dx, variable

    movq $array, %rsi
    movq $array_dest, %rdi
    movq array_size/2, %rcx
    cld
    movsw

    sti
    hlt


.driver DEVICE

    movw $DEVICE_ADDR, %dx
    inb %dx, %al
    iret
