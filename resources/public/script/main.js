window.addEventListener("load", function(){
    document.getElementById("calculate").addEventListener("click", calculate);
    function calculate() {
	let expression = document.getElementById("expression").value;
	let obj = {};
	obj["expression"] = expression;
	let text = JSON.stringify(obj);
	let xhr = new XMLHttpRequest();
	xhr.onloadend = function () {
	    document.getElementById("result").innerHTML = this.responseText;
	    hljs.highlightAll();
	};
	xhr.open('post', '/calc', true);
	xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
	xhr.send(JSON.stringify(text));
    }

    document.getElementById("history").addEventListener("click", history);
    function history() {
	let xhr = new XMLHttpRequest();
	xhr.onloadend = function () {
	    document.getElementById("result").innerHTML = JSON.stringify(JSON.parse(this.responseText), null, 2);
	    hljs.highlightAll();
	};
	xhr.open('post', '/hist', true);
	xhr.send();
    }

    document.getElementById("debug").addEventListener("click", debug);
    function debug() {
	let expression = document.getElementById("expression").value;
	let obj = {};
	obj["expression"] = expression;
	let text = JSON.stringify(obj);
	let xhr = new XMLHttpRequest();
	xhr.onloadend = function () {
	    document.getElementById("result").innerHTML = JSON.stringify(JSON.parse(this.responseText), null, 2);
	    hljs.highlightAll();
	};
	xhr.open('post', '/dbug', true);
	xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
	xhr.send(JSON.stringify(text));
    }
});
