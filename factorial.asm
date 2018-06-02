x dd 10

.code
mov r1, 6
call factorial
push r0
end:
halt

mul: ;R0 = R1*R2
push r1
push r2
mov r0, 0
repeta:
cmp r1, 0
beq gata
	add r0, r2
	sub r1, 1
br repeta
gata:
pop r2
pop r1
ret

factorial: ; R0 = R1!
push r2
mov r0, 1
repeta2:
	mov r2, r0
	call mul
	sub r1, 1
	beq gata2
br repeta2
gata2:
pop r2
ret

