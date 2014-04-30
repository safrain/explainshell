trap ctrl_c INT

function ctrl_c() {
	exit
}
for i in `find localpages`
do
	python manager.py $i --log info
done
