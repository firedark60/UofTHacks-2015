var express = require('express');
var busboy = require('connect-busboy'); //middleware for form/file upload
var path = require('path');     //used for file path
var fs = require('fs-extra');
var request = require('request');
var app = express();
var bodyParser = require("body-parser");
var cookieParser = require("cookie-parser");
var session = require("express-session");
app.use(busboy());
app.use(bodyParser());  
app.use(cookieParser("This is a secret"));
app.use(session({secret: "This is a secret", key: "express.sid"}));
app.use(express.static(__dirname + "/Client/"));
var MongoClient = require('mongodb').MongoClient;
var socketRooms = {};
var server = require('http').Server(app);
var io = require('socket.io')(server);
server.listen(3000);

RADIUS = 5;

function userExists (username, ret) {
    MongoClient.connect("mongodb://localhost:27017/Selfer", function(err, db) {
        if(!err) {
            var collection = db.collection('Users');
            collection.findOne({Username : username}, function(err, item){
                if (item)
                    ret(1);
                else ret(0);
            });
        }
    });
}

