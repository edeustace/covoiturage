
angular.module('markerModule', [], function($provide){

   $provide.factory('marker', function() {
		var currentScope = null; 
		var currentEvent = null;
		return {
			initMaps : function($scope){
				currentScope = $scope;
				angular.extend($scope, {
					center: {
						"lat": "46.65278",
						"lng": "-1.424961", 
					},
					markers: [], // an array of markers,
					zoom: 9, // the zoom level
				     });
			}, 	
			recordEvent : function(event){
				currentScope.center = event.location;
				var marker = this.getEventMarker(event);
				currentEvent = marker;
				currentScope.markers.push(marker);		
			},
			getEventMarker : function(event){
				currentScope.center = event.address.location;
				var marker = {type:"EVENT", latitude:event.address.location.lat, longitude:event.address.location.lng, icon: event.picto, visible:true};
				return marker;
			}, 	
			placeCurrentEvent : function(){
				currentScope.markers.push(currentEvent);	
			}, 
			getSubscriberMarker: function(subscriber, onClick){
				var marker = {
								type:subscriber.locomotion, 
								latitude:subscriber.address.location.lat, 
								longitude:subscriber.address.location.lng, 
								icon: subscriber.picto, 
								visible:subscriber.visible, 
								onClick: onClick
							};
				marker.subscriber = subscriber;
				return marker;
			},  
			recordSubscriber : function(subscriber, onClick){
				var markerSubsc = this.getSubscriberMarker(subscriber, onClick);
				for ( var int = 0; int < currentScope.markers.length; int++) {
					var marker = currentScope.markers[int];
					if(marker.subscriber && marker.subscriber.userRef == subscriber.userRef){
						currentScope.markers[int] =  markerSubsc;
						return;
					}
				}
				currentScope.markers.push(markerSubsc);
			}, 
			reinitMarker : function(){
				currentScope.markers = [];
			}};	
   	});
});

