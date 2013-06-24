'use strict';




/* Controllers */

function EventCtrl($scope, $http, marker, $location) {
    function extractFromUrl(url){
        var index = url.lastIndexOf("/")
        return url.substring(index+1, url.length);
    }
    $scope.goInCar = function(subscriber){
		if(subscriber){
			if(!subscriber.passagers){
				subscriber.passagers = new Array();
			} 
			subscriber.passagers.push($scope.user.id);
			$scope.car = subscriber;
		}
		var subscribers = $scope.event.subscribers;
		for ( var int = 0; int < subscribers.length; int++) {
			var subsc = subscribers[int];
			if(subsc.user.id === $scope.user.id){
				subsc.car = subscriber.user.id;
			}
		}
	}
    
    marker.initMaps($scope);
    var id = extractFromUrl($location.absUrl());
    $http.get('/rest/users/current').success(function(user) {
		if(user){
			$scope.user = user;

			$http.get('/rest/events/'+id).success(function(event) {
			    if(event){
			    	$scope.event = event;
			    	marker.reinitMarker();
				   	if(event){
				        marker.recordEvent(event);
				   	}
				   	if(event.subscribers){
						$scope.subscribers = event.subscribers;
						var length = event.subscribers.length;
					   	for(var i=0; i<length; i++){
							var subscriber = event.subscribers[i];
							if(subscriber.locomotion && subscriber.locomotion==="CAR"){
								subscriber.picto = marker.pictoAuto;
							}else if(subscriber.locomotion && subscriber.locomotion==="AUTOSTOP"){
								subscriber.picto = marker.pictoStop;			
							}else{
							}
							if(subscriber.current){
								$scope.current = subscriber;
							}
							marker.recordSubscriber(subscriber);
							var idUser = $scope.user.id;
							if(idUser == subscriber.user.id){
								$scope.currentSubscriber = subscriber;
								if(subscriber.locomotion=="CAR"){
									$scope.filterUsers = "AUTOSTOP";	
								}else if(subscriber.locomotion=="AUTOSTOP"){
									$scope.filterUsers = "CAR";
								}
							}
							subscriber.goInCar = function(){
								var subscriber = this;
								if(subscriber){
									if(!subscriber.passagers){
										subscriber.passagers = new Array();
									} 
									subscriber.passagers.push($scope.user.id);
									$scope.car = subscriber;
								}
								var subscribers = $scope.event.subscribers;
								for ( var int = 0; int < subscribers.length; int++) {
									var subsc = subscribers[int];
									if(subsc.user.id === $scope.user.id){
										subsc.car = subscriber.user.id;
									}
								}
							}
				   	 	}
			       	}
					var markers = $scope.markers;
					$scope.$watch('filterUsers', function(newValue, oldValue) {
							if(newValue){ 
								for(var i=0;i<$scope.markers.length;i++){
									var marker = markers[i];
									if(!(marker.type === newValue) && (marker.type != "EVENT")){
										marker.visible = false; 
									}else{
										marker.visible = true; 
									}
								}		
							}else{
								for(var i=0;i<$scope.markers.length;i++){
									var marker = markers[i];
									marker.visible = true; 
								}		
							}	
					});
			    }
		   });
		}
	}).error(function(error){
		alert('error : '+error);
	});

    
}
//EventCtrl.$inject = ['$scope','register'];

