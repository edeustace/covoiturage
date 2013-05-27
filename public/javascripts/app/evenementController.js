'use strict';




/* Controllers */

function EventCtrl($scope, $http, marker) {
    marker.initMaps($scope);
    $http.get('/app/evenement/data.json').success(function(data) {
	    if(data && data.event){
	       $scope.event = data.event;
	       marker.reinitMarker();
	       if(data.event){
		        marker.recordEvent(data.event);
	       }
	       if(data.event.subscribers){
			$scope.subscribers = data.event.subscribers;
			var length = data.event.subscribers.length;
	       	 	for(var i=0; i<length; i++){
				var subscriber = data.event.subscribers[i];
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
