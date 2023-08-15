.code
mov r1, 5
mov r2, 3
call 16 ;call mul
push r3
end:
halt

mul: ;R3 = R1*R2
push r1
push r2
mov r3, 0
repeta:
cmp r1, 0
beq gata
	add r3, r2
	sub r1, 1
br repeta
gata:
pop r2
pop r1
ret
