
angular.module('markerModule', [], function($provide){

   $provide.factory('marker', function() {
	   	var basePath = "/assets/icons/";
		var pictoStop = basePath+"autostop.jpg";
	   	var iconStop = new google.maps.MarkerImage(pictoStop,
			/* dimensions de l'image */
			new google.maps.Size(25,25)
		);
	   	var pictoAuto = basePath+"auto.jpg";
	   	var iconAuto = new google.maps.MarkerImage(pictoAuto,
			/* dimensions de l'image */
			new google.maps.Size(25,25)
		);
		var pictoEvent = basePath+"arrivee.jpg";
		var iconEvent = new google.maps.MarkerImage(pictoEvent,
			/* dimensions de l'image */
			new google.maps.Size(25,25)
		);
		var iconShadow = new google.maps.MarkerImage('http://www.google.com/mapfiles/shadow50.png',
		      // The shadow image is larger in the horizontal dimension
		      // while the position and offset are the same as for the main image.
		      new google.maps.Size(37, 34),
		      new google.maps.Point(0,0),
		      new google.maps.Point(9, 34));
	
		var icons = [];
		icons["CAR"] = iconAuto;
		icons["AUTOSTOP"] = iconStop;
		icons["EVENT"] = iconEvent;
		var currentScope = null; 
		var currentEvent = null;
		return {
			pictoStop : pictoStop, 
			pictoAuto : pictoAuto, 
			pictoEvent : pictoEvent, 
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
				var marker = {type:"EVENT", latitude:event.address.location.lat, longitude:event.address.location.lng, icon: icons["EVENT"], visible:true};
				return marker;
			}, 	
			placeCurrentEvent : function(){
				currentScope.markers.push(currentEvent);	
			}, 
			getSubscriberMarker: function(subscriber){
				//subscriber.picto = icons[subscriber.type];
				var marker = {
								type:subscriber.locomotion, 
								latitude:subscriber.address.location.lat, 
								longitude:subscriber.address.location.lng, 
								icon: icons[subscriber.locomotion], 
								visible:true
							};
				marker.event = event;
				return marker;
			},  
			recordSubscriber : function(subscriber){
				var marker = this.getSubscriberMarker(subscriber);
				currentScope.markers.push(marker);
			}, 
			reinitMarker : function(){
				currentScope.markers = [];
			}};	
   	});
});

