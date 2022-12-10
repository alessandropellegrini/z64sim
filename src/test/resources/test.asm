.org 0x800

.data

.equ DEVICE, 1
.equ DEVICE_ADDR, 0x123
variable: .word 0
array: .word 0, 0, 0
array_size = . - array

.bss

.comm array_dest, 8

.text

func:
    movq %rdx, 0xabc
    movq %rdx, variable
    movl $0, %ebx
    movsbw (%rbx), %dx
    salw $3, %dx
    add %rax, %rbx
    add $(3-1)*2, %rcx
    ret

main:
    movq $array, %rsi
    movq $array_dest, %rdi
    movq array_size/2, %rcx
    cld
    movsw

    call func

    sti
    hlt

.driver DEVICE
    movw $DEVICE_ADDR, %dx
    inb %dx, %al
    inb $DEVICE_ADDR, %al
    iret
