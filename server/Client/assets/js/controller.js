var app = angular.module("Achievements", []);

app.controller("Achievements", function($scope, $http) {
  $http.get('data/Achievements.json').
    success(function(data, status, headers, config) {
      $scope.Achievements = data;
    }).
    error(function(data, status, headers, config) {
      $scope.Achievements = "000000000000000000000000000000000000000000000000000000000000000000"
    });
});

  });
  
angular.module('Auth', [
        'ngCookies'
    ])
    .factory('Auth', ['$cookieStore', function ($cookieStore) {

        var _user = {};

        retzurn {

            user : _user,

            set: function (_user) {
                // you can retrive a user setted from another page, like login sucessful page.
                existing_cookie_user = $cookieStore.get('current.user');
                _user =  _user || existing_cookie_user;
                $cookieStore.put('current.user', _user);
            },

            remove: function () {
                $cookieStore.remove('current.user', _user);
            }
        };
    }])
;