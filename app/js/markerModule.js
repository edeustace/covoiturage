angular.module('markerModule', [], function($provide){

   $provide.factory('marker', function() {
   	var pictoStop = "/app/img/autostop.jpg";
   	var iconStop = new google.maps.MarkerImage(pictoStop,
		/* dimensions de l'image */
		new google.maps.Size(25,25)
	);
   	var pictoAuto = "/app/img/auto.jpg";
   	var iconAuto = new google.maps.MarkerImage(pictoAuto,
		/* dimensions de l'image */
		new google.maps.Size(25,25)
	);
	var pictoEvent = "/app/img/arrivee.jpg";
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

	icons = [];
	icons["CAR"] = iconAuto;
	icons["STOP"] = iconStop;
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
		currentScope.center = event.location;
		return this.getMarker(event.location.lat,event.location.lng, "EVENT", event);
	}, 	
	placeCurrentEvent : function(){
		currentScope.markers.push(currentEvent);	
	}, 
	getMarker: function(lat, lng, type, data){
		var latlng = new google.maps.LatLng(lat, lng);
		var marker = new google.maps.Marker({
			position: latlng,
			icon: icons[type],
			shadow: iconShadow,
			title: name,
			zIndex: Math.round(latlng.lat()*-100000)<<5
		});
		marker.type = type;
		marker.data = data; 
		return marker;
	}, 
	getSubscriberMarker: function(subscriber){
		return this.getMarker(subscriber.location.lat,subscriber.location.lng, subscriber.type, subscriber);
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


