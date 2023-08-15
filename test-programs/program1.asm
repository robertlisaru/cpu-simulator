x db 1
y dw 16

.code
push 8
add R15, 15
mov (R15), 16
add R14, 20
cmp R14, R15
bpl eticheta
mov R14, 100
br end
eticheta:
mov R14, 50
end:
halt
