'use strict';




/* Controllers */

function EventCtrl($scope, $http, marker, $location) {
    function extractFromUrl(url){
        var index = url.lastIndexOf("/")
        return url.substring(index+1, url.length);
    }
    marker.initMaps($scope);
    var id = extractFromUrl($location.absUrl());
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
				if(subscriber.type && subscriber.type==="CAR"){
					subscriber.picto = marker.pictoAuto;
				}else if(subscriber.type && subscriber.type==="STOP"){
					subscriber.picto = marker.pictoStop;			
				}else{
				}
				if(subscriber.current){
					$scope.current = subscriber;
				}
				marker.recordSubscriber(subscriber);
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
//EventCtrl.$inject = ['$scope','register'];

