var express = require('express');
var geocoder = require('geocoder');
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

RADIUS = 10;

function measure(lat1, lon1, lat2, lon2){  // generally used geo measurement function
    var R = 6378.137; // Radius of earth in KM
    var dLat = (lat2 - lat1) * Math.PI / 180;
    var dLon = (lon2 - lon1) * Math.PI / 180;
    var a = Math.sin(dLat/2) * Math.sin(dLat/2) +
    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
    Math.sin(dLon/2) * Math.sin(dLon/2);
    var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    var d = R * c;
    return d * 1000; // meters
}

function readByUsername (username, ret) {
    MongoClient.connect("mongodb://localhost:27017/Selfer", function(err, db) {
        if(!err) {
            console.log("We are connected");
            var collection = db.collection('Users');
            collection.findOne({Username : username}, function(err, item){
                if (!err)
                ret(item);
                else ret(null);
            });
        }
    });
}

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

function remove (id, region,ret) {
    MongoClient.connect("mongodb://localhost:27017/Selfer", function(err, db) {
        if(!err) {
            console.log("We are connected");
            var collection = db.collection('Locations.Regions.'+region);
            collection.remove({_id : id}, function(err, result){
                ret(!err);
            });
        }
    });
}

function insertIntoUsers(username, id, Visited, ret){
    MongoClient.connect("mongodb://localhost:27017/Selfer", function(err, db) {
        if(!err) {
            var collection = db.collection('Users');
            collection.update({Username : username}, {$addToSet : {'Visited' : Visited}}, function(err, result){
            });
            collection.update({Username : username}, {$addToSet : {'PlacesCurrentlyClaimed' : id}}, function(err, result){
            });
        }
    });
}

function insertIntoLocation (region, record, ret) {
    MongoClient.connect("mongodb://localhost:27017/Selfer", function(err, db) {
        if(!err) {
            var collection = db.collection('Locations.Regions.'+region);
            collection.insert(record, function(err, result){
                db.close();
                ret(result[0]._id);
            });
        }
    });
}  

function removeByUsername (id, username, region,ret) {
    MongoClient.connect("mongodb://localhost:27017/Selfer", function(err, db) {
        if(!err) {
            console.log("We are connected");
            var collection = db.collection('Locations.Regions.'+region);
            collection.update({Username : username}, {$pullAll : {'PlacesCurrentlyClaimed' : [id]}},function(err, item){
                ret(!err);
            });
        }
    });
}

function getNearestTargets(lat, long, region, ret){
    MongoClient.connect("mongodb://localhost:27017/Selfer", function(err, db) {
        if(!err) {
            var collection = db.collection('Locations.Regions.'+region);
            collection.find( {}, { Username: 1, Visited: 1}).toArray(function(err, docs) {
                var len = docs.length;
                if (len === 0 ){
                    ret(null);
                }
                else {
                    var returnList = [];
                    var container = {};
                    var retContainer = {};
                    for (var i = 0; i < len; i++){
                        VisitedRegionObject = docs[i].Visited;
                        dist = measure(lat, long, VisitedRegionObject.location[0], VisitedRegionObject.location[1]);
                        returnList.push(dist);
                        container[dist] = docs[i];
                    }
                    returnList.sort();
                    max = returnList.length > 6 ? 6 : returnList.length;
                    returnList = returnList.slice(0, max);

                    for (var i = 0; i < max; i++){
                        retContainer[returnList[i]] = container[returnList[i]];
                    }
                    ret(retContainer);
                }
            });
        }
    });
}



function checkLocation (clat, clong, region, ret) {
    MongoClient.connect("mongodb://localhost:27017/Selfer", function(err, db) {
        if(!err) {
            var collection = db.collection('Locations.Regions.'+region);
            collection.find( {}, { Username: 1, Visited: 1}).toArray(function(err, docs) {

                var len = docs.length;
                if (len === 0 ){
                    ret(null);
                }
                else {
                    for (var i = 0; i < len; i++){
                        VisitedRegionObject = docs[i].Visited;
                        if (measure(clat, clong, VisitedRegionObject.location[0], VisitedRegionObject.location[1]) < RADIUS){
                            ret(docs[i]);
                        }
                        else ret(null);
                    }
                }
            });
        }
    });
}



function getCity(lat, long, ret ){
    geocoder.reverseGeocode( lat, long, function ( err, data ) {
      var city = null;
      var address = (data.results[0].formatted_address).split(",");
      var len = address.length;
      var postalCode = address[len-2].trim();
      city = postalCode.split(" ")[0];
      ret(city);
    });
}

app.post('/login', function(req, res){
    console.log(req.body);
    var username = req.body.username;
    userExists (username, function(result){
        if (result === 1){
            readByUsername(username, function(result){
                if (result != null){  
                    console.log(result.password);
                    console.log(req.body.password);
                    if (result.password == req.body.password){
                        req.session.user = username;    
                        //req.session.userDetails = result;
                        res.statusCode = 200;
                        console.log("you're in");
                        res.redirect("/main.html");
                    }
                }
                else {
                    res.statusCode = 204;
                    res.redirect("/login.html");
                }
            });
        }
        else res.redirect("/login.html");
    });
});

app.post('/updateNearest', function(req, res){
    res.statusCode = 200;
    req.body = (req.body.body.split(","));
    console.log(req.body);
    var latString = (req.body[0].split(":")[1]);
    var lat = parseFloat(latString.substring(1, latString.length-1));
    var longString = (req.body[1].split(":")[1]);
    var long = parseFloat(longString.substring(1, longString.length-1));
    console.log(lat);
    console.log(long);
    getCity(lat, long, function(region){
        getNearestTargets(lat, long, region, function(dict){
            returnStringWithInfoOnEverything = JSON.stringify(dict);
            console.log(returnStringWithInfoOnEverything);
            res.write(returnStringWithInfoOnEverything);
            res.end();
        });
    });
    
});

app.post('/pictureCapture', function(req, res){
    res.statusCode = 200;
    req.body = (req.body.body.split(","));
    console.log(req.body);
    var latString = (req.body[0].split(":")[1]);
    var lat = parseFloat(latString.substring(1, latString.length-1));
    var longString = (req.body[1].split(":")[1]);
    var long = parseFloat(longString.substring(1, longString.length-1));
    var usernameString = (req.body[2].split(":")[1]);
    var username = (usernameString.substring(1, usernameString.length-1));
    var pictureString = (req.body[3].split(":")[1]);
    var pic = (pictureString.substring(1, pictureString.length-1));
    console.log(lat);
    console.log(long);
    console.log(username);
    console.log(pic);
    getCity(lat, long, function(region){
        console.log(region);
        if (region == null){ // Handle this case
            console.log("WE'RE FUCKED, DO YOU LIVE NOT EARTH???!?!");
        }
        else {
            checkLocation(lat, long, region, function(currentCapture){
                if (currentCapture != null){ // Do this call and the next call concurrently!!!!!! (They do NOT conflict)
                    remove(currentCapture._id, region, function(success){
                        if (!success) console.log("Removing the old dude from Locations.Regions.region didn't go too well");
                    });
                    removeByUsername(currentCapture._id, currentCapture.Username, region,function(success){
                       if (!success) console.log("Removing the old dudes PlacesCurrentlyClaimed value didn't go too well ;c"); 
                    });
                    
                }
                var JSON_DOC = {
                    Username : username,
                    Visited  : {
                        picture : pic, 
                        location : [lat, long],
                        worth : 1
                    }
                };
                insertIntoLocation(region,JSON_DOC, function(id){
                    Visited  = {
                        picture : pic, 
                        location : [lat, long],
                        worth : 1
                    };
                    insertIntoUsers(username, id, Visited, function(result){
                    });
                });
            });
        }
    });
    res.end();
});


checkLocation(45,45, 'TO', function(result){
    console.log(result._id);
    console.log(result.Username);
    console.log(result.Visited);
});