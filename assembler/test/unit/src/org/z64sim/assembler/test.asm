hlt
nop
pushfw
pushfq
popfw
popfq
movsb
movsw
movsl
movsq
stosb
stosw
stosl
stosq
clc
clp
clz
cls
cli
cld
clo
stc
stp
stz
sts
sti
std
ret
iret
inb %dx, %al
inw %dx, %ax
inl %dx, %eax
outb %al, %dx
outw %ax, %dx
outl %eax, %dx
insb
insw
insl
outsb
outsw
outsl
orq 45*59(%r10, %rsi, 2), %r14
andw %bp, 35+15(%r11, %r13, 8)
testw 53+55(%r14, %rsi, 2), %di
subq 30+60(%rbx, %r12, 1), %r12
movq %r13, 29*20(%rdx, %r13, 8)
sbbq 33+41(%r15, %rax, 2), %r14
orq %rsp, 51-28(%rdi, %r8, 4)
adcq %rbp, 24-21(%r9, %rdi, 8)
cmpq (%rbp, %r12, 1), %rbp
subb %al, (%rdi, %rax, 8)
xorw %ax, (%r9, %rcx, 1)
subl %ebp, (%rdi, %r9, 8)
orl %edi, 15*36(%r10, %rax, 4)
xorq (, %rsi, 2), %rdx
orq %r10, (%rdx, %r12, 1)
subw (%r11, %r8, 2), %cx
movq (, %rbx, 2), %r12
andq 30+19(, %r13, 4), %rbx
orl %eax, 52*33(%r8, %r11, 1)
movw 38*15(%rbx, %rax, 2), %cx
xorq %rbx, (%r9, %r13, 2)
xorq %rsi, 53-42(%r13, %rdi, 4)
adcw (%rbp), %bx
adcq %r8, (, %rbx, 1)
xorq %r12, (%rbx, %r8, 4)
sbbw (%rbp, %r10, 2), %di
andl %esi, 28-54(, %r13, 8)
xorl %ebp, (%rdi, %rdx, 2)
adcl %eax, (%r15, %rsi, 8)
cmpq (%rax, %r12, 4), %rbp
testw %bx, 16*47(%r11, %r9, 2)
sbbq (%rbp, %r14, 8), %r9
movq (%r10, %r15, 4), %rsp
testq %rsp, 54-29(, %r10, 2)
movq (, %r14, 8), %rsi
sbbq 7*32(%r9, %r13, 2), %rcx
addq (%r14, %rdx, 1), %rax
subq %rsi, 5*12(%rbx, %r12, 2)
adcq %r8, 16*15(%rax, %r10, 2)
orw %dx, 34+20(%rcx, %r11, 2)
movl 42*6(%r10, %r12, 4), %edx
movq (%rbx, %r14, 8), %r10
addq (%rsi, %rsp, 8), %r11
adcq (, %r10, 1), %rcx
xorw 61+22(, %rbp, 1), %ax
sbbq (%rbx, %r8, 2), %rax
movq 14+56(%r11, %r12, 1), %r8
sbbq (%r9, %r13, 2), %r11
adcq 32*53(%r9, %rdi, 1), %r13
addq %rdi, (%rax, %r13, 4)
sbbq (%rcx, %rdx, 1), %rdx
movw 17-8(%r14, %r15, 4), %bp
andq 15-56(%rax, %rdx, 1), %rdx
adcq (%r13, %r9, 4), %rax
adcq %r10, (%rdx, %r10, 2)
cmpq %r15, (%rsi, %r10, 2)
xorq %rbp, (%rax, %r8, 4)
testq %rbx, (%rdi, %rdi, 4)
andq 5-22(%rsi, %r10, 8), %rbp
andq (%r12, %r15, 2), %r11
subq %rdx, 31-40(%r11, %r11, 4)
movq %r10, 29-23(%rdx, %r14, 2)
andq 36*18(%r12, %rdx, 2), %rcx
cmpq (%r10, %rsp, 2), %r12
testq %r13, 35*20(%rbx, %r9, 8)
addq %r14, (%rsp, %r14, 2)
testq (%r8, %rsp, 1), %rbx
movq %rcx, (%r9, %r10, 2)
andq 16+23(%rbp, %rdi, 2), %rbx
cmpq (%rsp, %rbx, 8), %r9
movl %ebx, (, %rcx, 8)
testq %r13, 27*8(%r15, %rsi, 8)
cmpb 10+15(%r14, %r9, 4), %cl
andb 23*51(, %rax, 4), %dl
subq %r8, (%rcx, %r12, 2)
subq 60+29(, %rsi, 8), %rbx
addw %bx, (%r13, %rdx, 4)
andq %r15, 7+27(, %r15, 2)
addq %r13, (%r8, %r14, 1)
movq %rax, 4*50(, %r11, 2)
andq (%rax, %rcx, 2), %rax
rorq (%rbp, %r10, 2)
subq 47-10(, %r8, 1), %r12
subq %rax, (%rcx)
andw (%r12, %rbp, 2), %si
addq 27*8(%r15, %rbx, 1), %rax
movq %r10, 49*51(%rdi, %r10, 1)
andq %rbp, (%rsi, %r11, 1)
adcq %r15, (%rbp, %r10, 1)
addq 58*63(%rsp, %r15, 2), %r8
testq %r12, 32+11(%rbx, %r15, 2)
adcq %rsp, 30+8(%r13, %rsp, 1)
sbbq %r14, 60+51(%r11, %r15, 4)
orl (%rdx, %r12, 2), %ecx
addq (, %r11, 8), %r10
adcw %di, (%rsi, %rbx, 1)
orq 44*34(, %rsp, 4), %rbx
movq %rbx, (, %r8, 4)
cmpq %rdi, (%r11, %r8, 8)
adcw %di, (%rbx, %r12, 4)