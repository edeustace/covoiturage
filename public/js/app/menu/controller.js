

function MenuCtrl($scope, $http){

    $scope.open = false;

    $scope.openOrClose = function(idUser){
        if(!$scope.open){
            if(!$scope.events){
                $scope.loadEvents(idUser);
            }
            $scope.open = true;
        }else{
            $scope.open = false;
        }
    };

    $scope.loadEvents = function(idUser){
        $http.get('/rest/users/'+idUser+'/events').success(function(events) {
            $scope.events = events;
            for ( var i = 0; i < $scope.events.length; i++) {
                var event = $scope.events[i];
                event.links = buildLinks(event.links);
            }
        }).error(function(error){
            alert('error : '+error);
        });
    };

	function buildLinks(links){
        var theLinks = {};
        if(links){
            for ( var i = 0; i < links.length; i++) {
                var link = links[i];
                theLinks[link.rel] = link.href;
            }
        }
        return theLinks;
    }
}