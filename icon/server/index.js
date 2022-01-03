const express = require('express');
const path = require('path');
var mysql = require('mysql');
const db = require('./controller/db');


const app = express();
const port = 8080;

// sendFile will go here
app.get('/', function(req, res) {
  res.sendFile(path.join(__dirname, '/index.html'));
});

app.listen(port);
console.log('Server started at http://localhost:' + port);

// check connect with database (mysql)
db.connect(function(err) {
  if (err) throw err;
  console.log("Connected!!!")
});