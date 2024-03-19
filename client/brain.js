var divCell = '<div id = "cell$coord" class = "cell $color"></div>';
var divFigure = '<div id = "figure$coord" class = "figure" style = "cursor : $cursor"><img class = "image" src = "figures/alpha/$figure" style = "visibility : $vis"></div>';
var choiceFigure = '<div id = "figure$coord" class = "figureChoice" style = "cursor : $cursor"><img class = "image" src = "figures/alpha/$figure"></div>';
var coordinatesV = '<div class = "vertical"><p>$num</p></div>';
var coordinatesH = '<div class = "horizontal"><p>$symbol</p></div>';
var gameHash = '<p class = "moves$m">$turn</p>';
var sizeOfCells = 64;
var board = [];
var side;
var flagOfMate = false;
var flagOfWait = false;
var flagOfChoose = false;
var turnAudio = document.createElement('audio');
turnAudio.src = 'turn.mp3';
var countTurns = 0;
var castlingOO = false;
var castlingOOO = false;
var flag = false;

$(function() {
	startPosition();
});

function getSearch() {
	var xmlHttp = new XMLHttpRequest();
	xmlHttp.open("POST", "/", true);  
	xmlHttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
	xmlHttp.onreadystatechange = function() {
		if(xmlHttp.readyState != 4) return;
		if(xmlHttp.status == 200) {
			var obj  = JSON.parse(xmlHttp.responseText);
			if(obj["side"] == "white") {
				side = true;
				showFigures('rnbqkbnrpppppppp11111111111111111111111111111111PPPPPPPPRNBQKBNR');
			}
			else if (obj["side"] == "black") {
				side = false;
				showFigures('RNBKQBNRPPPPPPPP11111111111111111111111111111111pppppppprnbkqbnr');
				waitTurn();
			}
		}
		addCoordinates(side);
		var searchBattle = document.getElementById('search');
		searchBattle.removeEventListener('click', getSearch);

		$('.movesW').removeClass('movesW');
		$('.movesB').removeClass('movesB');

		$('#queen').append(choiceFigure
			.replace('$coord', (side) ? "Q" : "q")
			.replace('$figure', (side) ? getChessSymbol('Q') : getChessSymbol('q'))
			.replace('$cursor', 'pointer;'));
		$('#rook').append(choiceFigure
			.replace('$coord', (side) ? "R" : "r")
			.replace('$figure', (side) ? getChessSymbol('R') : getChessSymbol('r'))
			.replace('$cursor', 'pointer;'));
		$('#knight').append(choiceFigure
			.replace('$coord', (side) ? "N" : "n")
			.replace('$figure', (side) ? getChessSymbol('N') : getChessSymbol('n'))
			.replace('$cursor', 'pointer;'));
		$('#bishop').append(choiceFigure
			.replace('$coord', (side) ? "B" : "b")
			.replace('$figure', (side) ? getChessSymbol('B') : getChessSymbol('b'))
			.replace('$cursor', 'pointer;'));
	}
	xmlHttp.send( '{ "type": "search" }' );
}

function getDraw() {
	/*var xmlHttp = new XMLHttpRequest();
	xmlHttp.open("POST", "/", true);  
	xmlHttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
	xmlHttp.onreadystatechange = function() {
		if(xmlHttp.readyState != 4) return;
		if(xmlHttp.status == 200) {
			var obj  = JSON.parse(xmlHttp.responseText);
			if(obj["draw"] == "agreement") {
				alert("Draw?");
			}
		}
		var drawBattle = document.getElementById('draw');
		drawBattle.removeEventListener('click', getDraw);
	}
	xmlHttp.send( '{ "type": "getdraw" }' );*/
	alert("You cant offer a draw");
}

function getSurrend() {
	/*var xmlHttp = new XMLHttpRequest();
	xmlHttp.open("POST", "/", true);  
	xmlHttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
	xmlHttp.onreadystatechange = function() {
		if(xmlHttp.readyState != 4) return;
		if(xmlHttp.status == 200) {
			var obj  = JSON.parse(xmlHttp.responseText);
			if(obj["surrend"] == "agreement") {
				alert("Surrend?");
			}
		}
		var surrendBattle = document.getElementById('surrend');
		surrendBattle.removeEventListener('click', getSurrend);
	}
	xmlHttp.send( '{ "type": "getsurrend" }' );*/
	alert("You cant surrend");
}

function startPosition() {
 	for (var i = 0; i < sizeOfCells / 8; i++) {
 		board[i] = [];
 		for (var j = 0; j < sizeOfCells / 8; j++) {
 			board[i][j] = "1";
 		}
 	}

	addCells();

	$('.choiceFigures').css("visibility", "hidden");

	var searchBattle = document.getElementById('search');
	searchBattle.addEventListener("click", getSearch);

	var drawBattle = document.getElementById('draw');
	drawBattle.addEventListener("click", getDraw);

	var surrendBattle = document.getElementById('surrend');
	surrendBattle.addEventListener("click", getSurrend);
}

function getFigure(coord_y, coord_x) {
	$('#figure' + String(coord_y) + String(coord_x)).draggable( {containment: ".board"} );
}

function setFigure() {
	$('.cell').droppable({
		drop: function(event, ui) {
			var fromCoord_y = ui.draggable.attr('id').substring(6);
			var fromCoord_x = ui.draggable.attr('id').substring(7);
			var toCoord_y = this.id.substring(4);
			var toCoord_x = this.id.substring(5);

			if ((Number(toCoord_y[0]) == 0) && ((side && (board[Number(fromCoord_y[0])][Number(fromCoord_x)] == "P") ||
			(!side && (board[Number(fromCoord_y[0])][Number(fromCoord_x)] == "p"))))) {
				chooseFigure(Number(fromCoord_y[0]), Number(fromCoord_x), Number(toCoord_y[0]), Number(toCoord_x));
			}
			else {
				moveFigure(Number(fromCoord_y[0]), Number(fromCoord_x), Number(toCoord_y[0]), Number(toCoord_x), "no");
			}
		}
	});
}

function waitTurn() {
	var xmlHttp = new XMLHttpRequest();
	xmlHttp.open("POST", "/", true);
	xmlHttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
  
	xmlHttp.onreadystatechange = function() {
	  if(xmlHttp.readyState != 4) return;
	  if(xmlHttp.status == 200) {
			var obj = JSON.parse(xmlHttp.responseText);
			var fromCoord_y = (!side) ? 7 - obj["from_y"] : obj["from_y"];
			var fromCoord_x = (!side) ? 7 - obj["from_x"] : obj["from_x"];
			var toCoord_y = (!side) ? 7 - obj["to_y"] : obj["to_y"];
			var toCoord_x = (!side) ? 7 - obj["to_x"] : obj["to_x"];
			var chsFigure = obj["choose"];

			if (obj["turn"] == "true") {
				turnAudio.play();
				var figure = board[fromCoord_y][fromCoord_x];
				showFigure(fromCoord_y, fromCoord_x, '1');
				showFigure(toCoord_y, toCoord_x, figure);
			}
			else if (obj["turn"] == "takeOnThePass") {
				turnAudio.play();
				var figure = board[fromCoord_y][fromCoord_x];
				showFigure(fromCoord_y, fromCoord_x, '1');
				showFigure(toCoord_y - 1, toCoord_x, '1');
				showFigure(toCoord_y, toCoord_x, figure);
			}
			else if (obj["turn"] == "choose") {
				flagOfChoose = true;
				turnAudio.play();
				var figure = board[fromCoord_y][fromCoord_x];
				showFigure(fromCoord_y, fromCoord_x, '1');
				showFigure(toCoord_y, toCoord_x, chsFigure);
			}
			else if (obj["turn"] == "OO") {
				castlingOO = true;
				turnAudio.play();
				if (side) {
					showFigure(0, 7, '1');
					showFigure(0, 4, '1');
					showFigure(0, 5, 'r');
					showFigure(0, 6, 'k');
				}
				else {
					showFigure(0, 0, '1');
					showFigure(0, 3, '1');
					showFigure(0, 2, 'R');
					showFigure(0, 1, 'K');
				}
			}
			else if (obj["turn"] == "OOO") {
				castlingOOO = true;
				turnAudio.play();

				if (side) {
					showFigure(0, 4, '1');
					showFigure(0, 0, '1');
					showFigure(0, 3, 'r');
					showFigure(0, 2, 'k');
				}
				else {
					showFigure(0, 3, '1');
					showFigure(0, 7, '1');
					showFigure(0, 4, 'R');
					showFigure(0, 5, 'K');
				}
			}
			flagOfWait = true;

			if (side) setGameHash(fromCoord_y, fromCoord_x, toCoord_y, toCoord_x, board[toCoord_y][toCoord_x]);
			else setGameHash(7 - fromCoord_y, 7 - fromCoord_x, 7 - toCoord_y, 7 - toCoord_x, board[toCoord_y][toCoord_x]);

			if (castlingOO) castlingOO = false;
			if (castlingOOO) castlingOOO = false;
			if (flagOfChoose) flagOfChoose = false;
			flagOfWait = false;

			if (obj["mate"] == "true") {
				alert("You lose!");
				countTurns = 0;
				var searchBattle = document.getElementById('search');
				searchBattle.addEventListener('click', getSearch);
				return;
			}
			countTurns++;
	  	}
	}
	xmlHttp.send( '{ "type": "wait" }' );
}

function moveFigure(fromCoord_y, fromCoord_x, toCoord_y, toCoord_x, choose) {
	var from_y = (side) ? fromCoord_y : 7 - fromCoord_y;
	var from_x  = (side) ? fromCoord_x : 7 - fromCoord_x;
	var to_y = (side) ? toCoord_y : 7 - toCoord_y;
	var to_x = (side) ? toCoord_x : 7 - toCoord_x;

	if (board[fromCoord_y][fromCoord_x] != '1') {
		if ((fromCoord_y == toCoord_y) && (fromCoord_x == toCoord_x)) {
			showFigure(fromCoord_y, fromCoord_x, board[fromCoord_y][fromCoord_x]);
			return;
		}
		var xmlHttp = new XMLHttpRequest();
		xmlHttp.open("POST", "/", true);  
		xmlHttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
		xmlHttp.onreadystatechange = function() {
			if(xmlHttp.readyState != 4) return;
			if(xmlHttp.status == 200) {
				var obj  = JSON.parse(xmlHttp.responseText);
				if (obj["turn"] == "false") {
					showFigure(fromCoord_y, fromCoord_x, board[fromCoord_y][fromCoord_x]);
					return;
				}
				if (obj["turn"] == "true") {
					turnAudio.play();
					var figure = board[fromCoord_y][fromCoord_x];
					showFigure(fromCoord_y, fromCoord_x, '1');
					showFigure(toCoord_y, toCoord_x, figure);
				}
				else if (obj["turn"] == "takeOnThePass") {
					turnAudio.play();
					var figure = board[fromCoord_y][fromCoord_x];
					showFigure(fromCoord_y, fromCoord_x, '1');
					showFigure(toCoord_y + 1, toCoord_x, '1');
					showFigure(toCoord_y, toCoord_x, figure);
				}
				else if (obj["turn"] == "choose") {
					flagOfChoose = true;
					turnAudio.play();
					var figure = board[fromCoord_y][fromCoord_x];
					showFigure(fromCoord_y, fromCoord_x, '1');
					showFigure(toCoord_y, toCoord_x, choose);
				}
				else if (obj["turn"] == "OO") {
					castlingOO = true;
					turnAudio.play();
					if (side) {
						showFigure(7, 4, '1');
						showFigure(7, 7, '1');
						showFigure(7, 5, 'R');
						showFigure(7, 6, 'K');
					}
					else {
						showFigure(7, 3, '1');
						showFigure(7, 0, '1');
						showFigure(7, 2, 'r');
						showFigure(7, 1, 'k');
					}
				}
				else if (obj["turn"] == "OOO") {
					castlingOOO = true;
					turnAudio.play();

					if (side) {
						showFigure(7, 4, '1');
						showFigure(7, 0, '1');
						showFigure(7, 3, 'R');
						showFigure(7, 2, 'K');
					}
					else {
						showFigure(7, 3, '1');
						showFigure(7, 7, '1');
						showFigure(7, 4, 'r');
						showFigure(7, 5, 'k');
					}
				}
				setGameHash(from_y, from_x, to_y, to_x, board[toCoord_y][toCoord_x]);
				if (castlingOO) castlingOO = false;
				if (castlingOOO) castlingOOO = false;
				if (flagOfChoose) flagOfChoose = false;
				countTurns++;
				if (obj["mate"] == "true") {
					alert("You win!");
					countTurns = 0;
					var searchBattle = document.getElementById('search');
					searchBattle.addEventListener('click', getSearch);
					waitTurn();
					return;
				}
				waitTurn();
			}
		}
		if (choose == "no") {
			xmlHttp.send( '{ "type": "turn" , "coord": { "from_y": ' + from_y + ',  "from_x": ' + from_x + ', "to_y": ' + to_y + ', "to_x": ' + to_x + ', "choose": "no" } }' );
		}
		else {
			xmlHttp.send( '{ "type": "turn" , "coord": { "from_y": ' + from_y + ',  "from_x": ' + from_x + ', "to_y": ' + to_y + ', "to_x": ' + to_x + ', "choose": ' + choose + ' } }' );
		}
	}
}

function addCoordinates(color) {
	var symbols = "abcdefgh";
	if (color) {
		$('.coordinatesV').html('');
		$('.coordinatesH').html('');
		for(var coord = (sizeOfCells / 8); coord > 0; coord--) {
			$('.coordinatesV').append(coordinatesV.replace('$num', coord));
			$('.coordinatesH').append(coordinatesH.replace('$symbol', symbols[8 - coord]));
		}
	} 
	else {
		$('.coordinatesV').html('');
		$('.coordinatesH').html('');
		for(var coord = 0; coord < sizeOfCells / 8; coord++) {
			$('.coordinatesV').append(coordinatesV.replace('$num', coord + 1));
			$('.coordinatesH').append(coordinatesH.replace('$symbol', symbols[7 - coord]));
		}

	}
}

function setGameHash(fromCoord_y, fromCoord_x, toCoord_y, toCoord_x, figure) {
	if (castlingOO) {
		$('.game').append(gameHash.replace('$m', (countTurns % 2 == 0) ? 'W' : 'B').
		replace('$turn', String(countTurns + 1) + ". " + "O-O"));
	} 
	else if (castlingOOO) {
		$('.game').append(gameHash.replace('$m', (countTurns % 2 == 0) ? 'W' : 'B').
		replace('$turn', String(countTurns + 1) + ". " + "O-O-O"));
	}
	else if (flagOfChoose) {
		$('.game').append(gameHash.replace('$m', (countTurns % 2 == 0) ? 'W' : 'B').
		replace('$turn', String(countTurns + 1) + ". " + getXCoords(toCoord_x + 1) + String(8 - toCoord_y) + " - " + 
		String(figure)));
	}
	else {
		$('.game').append(gameHash.replace('$m', (countTurns % 2 == 0) ? 'W' : 'B').
		replace('$turn', String(countTurns + 1) + ". " + String(figure) +
		getXCoords(fromCoord_x + 1) + String(8 - fromCoord_y) + " -" +
		getXCoords(toCoord_x + 1) + String(8 - toCoord_y)));
	}
}

function getXCoords(coord) {
	switch (coord) {
		case 1:
			return ' a';
		case 2:
			return ' b';
		case 3:
			return ' c';
		case 4:
			return ' d';
		case 5:
			return ' e';
		case 6:
			return ' f';
		case 7:
			return ' g';
		case 8:
			return ' h';
	}
}

function addCells() {
	$('.board').html('');
	for(var coord_y = 0; coord_y < sizeOfCells / 8; coord_y++) {
		for(var coord_x = 0; coord_x < sizeOfCells / 8; coord_x++) {
			$('.board').append(divCell.replace('$coord', String(coord_y) + String(coord_x))
			.replace('$color', isBlackSquare((coord_y * 8) + coord_x) ? 'black' : 'white'));
		}
	}	
	setFigure();
}

function isBlackSquare(coord) {
	return (coord % 8 + Math.floor(coord / 8)) % 2;
}

function showFigure(coord_y, coord_x, figure) {
	board[coord_y][coord_x] = figure;
	$('#cell' + String(coord_y) + String(coord_x)).html(divFigure
		.replace('$coord', String(coord_y) + String(coord_x))
		.replace('$figure', getChessSymbol(figure))
		.replace('$vis', figure == '1' ? 'hidden;' : 'visible;')
		.replace('$cursor', figure == '1' ? 'default;' : 'pointer;'));
	if (figure != '1' && (((side) && (board[coord_y][coord_x].toUpperCase() == board[coord_y][coord_x])) || 
		((!side) && (board[coord_y][coord_x].toUpperCase() != board[coord_y][coord_x])))) {
		getFigure(coord_y, coord_x);
	}
}

function getChessSymbol(figure) {
	switch (figure) {
		case 'K' : return 'wK.png';
		case 'Q' : return 'wQ.png';
		case 'R' : return 'wR.png';
		case 'B' : return 'wB.png';
		case 'N' : return 'wN.png';
		case 'P' : return 'wP.png';
		case 'k' : return 'bK.png';
		case 'q' : return 'bQ.png';
		case 'r' : return 'bR.png';
		case 'b' : return 'bB.png';
		case 'n' : return 'bN.png';
		case 'p' : return 'bP.png';
		default  : return 'void.png';
	}
}

function showFigures(figures) {
	for (var coord_y = 0; coord_y < sizeOfCells / 8; coord_y++) {
		for (var coord_x = 0; coord_x < sizeOfCells / 8; coord_x++) {
			showFigure(coord_y, coord_x, figures[coord_y * 8 + coord_x]);
		}
	}
}

function chooseFigure(fromCoord_y, fromCoord_x, toCoord_y, toCoord_x) {
	const q = document.querySelector('#queen');
	const r = document.querySelector('#rook');
	const b = document.querySelector('#bishop');
	const k = document.querySelector('#knight');

	$('.choiceFigures').css("visibility", "visible");

	function setHookQ() {
		$('.choiceFigures').css("visibility", "hidden");
		$('#figure' + (side) ? 'Q' : 'q').css("visibility", "hidden");
		$('#figure' + (side) ? 'R' : 'r').css("visibility", "hidden");
		$('#figure' + (side) ? 'N' : 'n').css("visibility", "hidden");
		$('#figure' + (side) ? 'B' : 'b').css("visibility", "hidden");

		moveFigure(fromCoord_y, fromCoord_x, toCoord_y, toCoord_x, (side) ? 'Q' : 'q');

		if (flag) {
			q.removeEventListener('click', setHookQ);
			r.removeEventListener('click', setHookR);
			b.removeEventListener('click', setHookB);
			k.removeEventListener('click', setHookK);
		}
	}

	function setHookR() {
		$('.choiceFigures').css("visibility", "hidden");
		$('#figure' + (side) ? 'Q' : 'q').css("visibility", "hidden");
		$('#figure' + (side) ? 'R' : 'r').css("visibility", "hidden");
		$('#figure' + (side) ? 'N' : 'n').css("visibility", "hidden");
		$('#figure' + (side) ? 'B' : 'b').css("visibility", "hidden");

		moveFigure(fromCoord_y, fromCoord_x, toCoord_y, toCoord_x, (side) ? 'R' : 'r');

		if (flag) {
			q.removeEventListener('click', setHookQ);
			r.removeEventListener('click', setHookR);
			b.removeEventListener('click', setHookB);
			k.removeEventListener('click', setHookK);
		}
	}

	function setHookB() {
		$('.choiceFigures').css("visibility", "hidden");
		$('#figure' + (side) ? 'Q' : 'q').css("visibility", "hidden");
		$('#figure' + (side) ? 'R' : 'r').css("visibility", "hidden");
		$('#figure' + (side) ? 'N' : 'n').css("visibility", "hidden");
		$('#figure' + (side) ? 'B' : 'b').css("visibility", "hidden");

		moveFigure(fromCoord_y, fromCoord_x, toCoord_y, toCoord_x, (side) ? 'B' : 'b');

		if (flag) {
			q.removeEventListener('click', setHookQ);
			r.removeEventListener('click', setHookR);
			b.removeEventListener('click', setHookB);
			k.removeEventListener('click', setHookK);
		}
	}

	function setHookK() {
		$('.choiceFigures').css("visibility", "hidden");
		$('#figure' + (side) ? 'Q' : 'q').css("visibility", "hidden");
		$('#figure' + (side) ? 'R' : 'r').css("visibility", "hidden");
		$('#figure' + (side) ? 'N' : 'n').css("visibility", "hidden");
		$('#figure' + (side) ? 'B' : 'b').css("visibility", "hidden");

		moveFigure(fromCoord_y, fromCoord_x, toCoord_y, toCoord_x, (side) ? 'N' : 'n');

		if (flag) {
			q.removeEventListener('click', setHookQ);
			r.removeEventListener('click', setHookR);
			b.removeEventListener('click', setHookB);
			k.removeEventListener('click', setHookK);
		}
	}

	q.addEventListener('click', setHookQ);
	r.addEventListener('click', setHookR);
	b.addEventListener('click', setHookB);
	k.addEventListener('click', setHookK);

	flag = true;
}
