x db 7
y dd 268435456;comentariu comentos
z dw 1024

.code
START:
mov (R7), 123(R3)
inc R7
br et1
mov R6, 80
et1: mov R8, 120(R6)
NOP