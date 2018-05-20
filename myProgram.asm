z dd 2048 ;alt comentariu
y dw 1024
x db 127 ;comentariu




.code
StarT:
br eticheta
mov (R1), 32(R2)
mov (R3), 128
eticheta: inc R9
nop