# Simple Round-Robin Scheduler for z64sim
#
# Three "processes" run in round-robin fashion, preempted by a timer
# interrupt. The timer driver saves the full CPU context (including
# the stack pointer) of the current process and restores the context
# of the next one.
#
# Process 1: scans a small array and accumulates the sum in a loop.
# Process 2: increments a counter up to a limit, then restarts.
# Process 3: decrements a counter down to zero, then restarts.

.org 0x800

.data
  # Timer device I/O ports
  .equ TIMER_DELAY,  0x10
  .equ TIMER_STATUS, 0x11
  .equ TIMER_IRQ,    0x12

  # Scheduling constants
  .equ NUM_PROCS, 3       # total number of processes
  .equ QUANTUM,   1000    # timer quantum in milliseconds

  # Table of pointers to the three PCBs.
  # Lets us index by process number without multiplication:
  #   pcb_table[curr] -> address of current PCB.
  pcb_table: .quad pcb1, pcb2, pcb3

  # Process context blocks (PCBs).
  # Layout per process (9 quads = 72 bytes):
  # Note that we are not saving all GP registers, for simplicity.
  #   Offset  0: saved RIP
  #   Offset  8: saved RFLAGS
  #   Offset 16: saved RAX
  #   Offset 24: saved RBX
  #   Offset 32: saved RCX
  #   Offset 40: saved RDX
  #   Offset 48: saved RSI
  #   Offset 56: saved RDI
  #   Offset 64: saved RSP
  pcb1: .quad prog1, 0, 0, 0, 0, 0, 0, 0, stack1 + 256
  pcb2: .quad prog2, 0, 0, 0, 0, 0, 0, 0, stack2 + 256
  pcb3: .quad prog3, 0, 0, 0, 0, 0, 0, 0, stack3 + 256

  # Index of the currently running process (0..NUM_PROCS-1)
  curr: .quad 0

  # Per-process stack areas (256 bytes each).
  # Stacks grow downward, so the initial RSP points to the
  # end of each area.
  stack1: .fill 256, 1, 0
  stack2: .fill 256, 1, 0
  stack3: .fill 256, 1, 0

  # ------- Data for Process 1 -------
  array1:    .long 1, 2, 3, 4
  .equ ARRAY1_LEN, 4

  # ------- Data for Process 2 -------
  .equ LIMIT2, 1024

  # ------- Data for Process 3 -------
  .equ LIMIT3, 2048

.text

main:
    # ---- Configure and start the timer ----
    sti                            # enable interrupts globally
    movl  $QUANTUM, %eax
    outl  %eax, $TIMER_DELAY
    outb  %al,  $TIMER_STATUS      # start the timer

    # ---- Launch process 0 ----
    # Switch to its stack and jump to its entry point.
    movq  pcb1+64, %rsp
    xorq  %rax, %rax
    xorq  %rbx, %rbx
    xorq  %rcx, %rcx
    xorq  %rdx, %rdx
    xorq  %rsi, %rsi
    xorq  %rdi, %rdi
    jmp   prog1


# =========================================================================
# Process 1 – Scan an array and accumulate the sum; loop forever.
# Uses: %rax = pointer, %rbx = count, %rcx = accumulator
# =========================================================================
prog1:
    movq  $array1, %rax
    movl  $ARRAY1_LEN, %ebx
    xorq  %rcx, %rcx
  .p1_loop:
    cmpl  $0, %ebx
    jz    prog1                    # restart when done
    addl  (%rax), %ecx
    addq  $4, %rax                 # next .long element
    subl  $1, %ebx
    jmp   .p1_loop

# =========================================================================
# Process 2 – Increment a counter up to LIMIT2, then restart.
# Uses: %rcx = counter
# =========================================================================
prog2:
    xorq  %rcx, %rcx
  .p2_loop:
    addq  $1, %rcx
    cmpq  $LIMIT2, %rcx
    jz    prog2
    jmp   .p2_loop

# =========================================================================
# Process 3 – Decrement a counter from LIMIT3 down to 0, then restart.
# Uses: %rcx = counter
# =========================================================================
prog3:
    movq  $LIMIT3, %rcx
  .p3_loop:
    subq  $1, %rcx
    cmpq  $0, %rcx
    jnz   .p3_loop
    jmp   prog3


# =========================================================================
# Timer ISR – Round-Robin context switch
#
# On entry the hardware has pushed (from handleInterrupt):
#     RSP+0 : saved RFLAGS
#     RSP+8 : saved RIP  (return address)
#
# The driver:
#  1. Saves GP registers + RSP into the current process's PCB.
#  2. Advances curr to the next process (wrapping around).
#  3. Restores the next process's context from its PCB.
#  4. Re-arms the timer and returns via iret.
# =========================================================================
.driver 1
    # ---- Save current process context ----
    # We need a scratch register (%rax) to address the PCB.
    pushq %rax

    # Look up PCB pointer: pcb_base = pcb_table[curr * 8]
    movq  curr, %rax               # %rax = curr index
    shlq  $3, %rax                 # %rax = curr * 8 (byte offset in table)
    addq  $pcb_table, %rax         # %rax = &pcb_table[curr]
    movq  (%rax), %rax             # %rax = PCB base address

    # Save GP registers (except %rax which we'll recover from the stack)
    movq  %rbx, 24(%rax)
    movq  %rcx, 32(%rax)
    movq  %rdx, 40(%rax)
    movq  %rsi, 48(%rax)
    movq  %rdi, 56(%rax)

    # Recover original %rax from the stack and save it
    popq  %rbx                     # original %rax -> %rbx
    movq  %rbx, 16(%rax)           # pcb.rax

    # Save RIP and RFLAGS from the interrupt frame on the stack.
    # After our popq, the stack is back to the interrupt frame:
    #   RSP+0 : RFLAGS
    #   RSP+8 : RIP
    movq  (%rsp), %rbx
    movq  %rbx, 8(%rax)            # pcb.rflags
    movq  8(%rsp), %rbx
    movq  %rbx, (%rax)             # pcb.rip

    # Save RSP. The pre-interrupt value is current RSP + 16
    # (to account for the RFLAGS + RIP pushed by the hardware).
    movq  %rsp, %rbx
    addq  $16, %rbx
    movq  %rbx, 64(%rax)           # pcb.rsp

    # ---- Advance to the next process (round-robin) ----
    movq  curr, %rbx
    addq  $1, %rbx
    cmpq  $NUM_PROCS, %rbx
    jnz   .no_wrap
    xorq  %rbx, %rbx
  .no_wrap:
    movq  %rbx, curr

    # ---- Restore next process context ----
    # Look up next PCB pointer.
    shlq  $3, %rbx                 # %rbx = new_curr * 8
    addq  $pcb_table, %rbx         # %rbx = &pcb_table[new_curr]
    movq  (%rbx), %rbx             # %rbx = new PCB base address

    # Switch to the new process's stack.
    movq  64(%rbx), %rsp

    # Build an interrupt return frame on the new stack.
    # iret pops: RFLAGS first, then RIP — so push RIP first (deeper).
    pushq (%rbx)                   # push saved RIP
    pushq 8(%rbx)                  # push saved RFLAGS

    # Restore GP registers from the new PCB.
    movq  16(%rbx), %rax           # pcb.rax
    movq  32(%rbx), %rcx           # pcb.rcx
    movq  40(%rbx), %rdx           # pcb.rdx
    movq  48(%rbx), %rsi           # pcb.rsi
    movq  56(%rbx), %rdi           # pcb.rdi
    movq  24(%rbx), %rbx           # pcb.rbx (last — clobbers our pointer)

    # ---- Re-arm the timer and return ----
    outb  %al, $TIMER_IRQ          # acknowledge previous interrupt
    outb  %al, $TIMER_STATUS       # restart the timer

    iret
