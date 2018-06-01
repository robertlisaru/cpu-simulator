.code
add R15, 15
add R14, 20
cmp R14, R15
bpl eticheta
mov R14, 100
br end
eticheta:
mov R14, 50
end:
halt