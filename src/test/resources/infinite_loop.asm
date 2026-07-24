.org 0x800
.text
main:
    mov $1, %rax
loop:
    add $1, %rax
    jmp loop
