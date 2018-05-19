x db 7
y dd 268435456;comentariu comentos
z dw 1024

.code
START:
mov R7, (x)
inc R7
clc
mov R6, x
mov R8, 120(R6)
NOP