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




app.post('/pictureCapture', function(req, res){
    res.statusCode = 200;
    var lat = req.body.latitude;
    var long = req.body.longitude;
    getCity(lat, long, function(region){
        console.log(region);
        if (region == null){ // Handle this case
            console.log("WE'RE FUCKED, DO YOU LIVE NOT EARTH???!?!");
        }
        else {
            checkLocation(lat, long, region, function(currentCapture){
                if (currentCapture == null){
                    // Enter it into the locations table as this USERS shit
                    // Enter it into BOTH the Users.VisitedPlaces and Users.CurrentPlaces
                }
                else {
                    // Pop out the old guy from Loccations.Regions
                    // Pop out the location from the current places
                    // Enter it into BOTH the Users.VisitedPlaces and Users.CurrentPlaces   
                    // Enter it into the locations table as this USERS shit
                }
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