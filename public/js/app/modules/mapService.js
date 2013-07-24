


angular.module('mapService', [], function($provide){

   $provide.factory('mapService', function() {
	   function floatEqual(f1, f2) {
			return (Math.abs(f1 - f2) < 0.000001);
		}
	   
	   return {
		    addMarkerEvent : function (event, $scope){
		    	$scope.mapOptions.center = new google.maps.LatLng(event.address.location.lat, event.address.location.lng);
		    	
		    	var marker = new google.maps.Marker({
		            map: $scope.myMap,
		            position: new google.maps.LatLng(event.address.location.lat, event.address.location.lng),
		            icon:event.picto, 
		            visible : true 
		          });
		    	marker.type = "EVENT";
		    	$scope.myMarkers.push(marker);
		    	$scope.bounds.extend(marker.getPosition());
		    },
            setMarkerEvent : function (event, $scope){
                var current = null;
                for(var i in $scope.myMarkers){
                    var aMarker = $scope.myMarkers[i];
                    if(aMarker.type === "EVENT"){
                        aMarker.setPosition(new google.maps.LatLng(event.address.location.lat, event.address.location.lng));
                        $scope.bounds.extend(marker.getPosition());
                    }
                }
                $scope.myMap.fitBounds($scope.bounds);
            },
		    addMarkerSubscriber : function (subscriber, $scope){
		    	var marker = this.findMarkerByUserRef($scope.myMarkers, subscriber.userRef);
		    	if(marker){
		    		marker.setVisible(subscriber.visible);
		    		marker.setIcon(subscriber.picto);
		    		marker.setVisible(subscriber.visible);
		    	}else{
		    		marker = this.findMarkerByLatLng($scope.myMarkers, subscriber.address.location.lat, subscriber.address.location.lng);
		    		if(marker){
		    			marker.setPosition(new google.maps.LatLng(subscriber.address.location.lat, subscriber.address.location.lng));
		    			marker.setVisible(subscriber.visible);
		    			marker.setIcon(subscriber.picto);
		    		}else{
			        	marker = new google.maps.Marker({
			                map: $scope.myMap,
			                position: new google.maps.LatLng(subscriber.address.location.lat, subscriber.address.location.lng),
			                icon:subscriber.picto, 
			                visible : subscriber.visible
			              });
			        	marker.type = subscriber.locomotion;
			        	$scope.myMarkers.push(marker);
			        	$scope.bounds.extend(marker.getPosition());
		    		}
		    	}
		    	marker.type = subscriber.locomotion;
		    	marker.subscriber = subscriber;
		    },
		    updateMarkerSubscriber : function (subscriber, $scope){
                var marker = this.findMarkerByUserRef($scope.myMarkers, subscriber.userRef);
                if(marker){
                        marker.setPosition(new google.maps.LatLng(subscriber.address.location.lat, subscriber.address.location.lng));
                        marker.setVisible(subscriber.visible);
                        marker.setIcon(subscriber.picto);
                }else{
                    marker = new google.maps.Marker({
                        map: $scope.myMap,
                        position: new google.maps.LatLng(subscriber.address.location.lat, subscriber.address.location.lng),
                        icon:subscriber.picto,
                        visible : subscriber.visible
                      });
                    marker.type = subscriber.locomotion;
                    $scope.myMarkers.push(marker);
                    $scope.bounds.extend(marker.getPosition());
                }
                marker.type = subscriber.locomotion;
                marker.subscriber = subscriber;
            },
		    findMarkerByLatLng : function (markers, lat, lng) {
				for ( var i = 0; i < markers.length; i++) {
					var pos = markers[i].getPosition();
					if (floatEqual(pos.lat(), lat)
							&& floatEqual(pos.lng(), lng)) {
						return markers[i];
					}
				}

				return null;
			},
			findMarkerByUserRef : function (markers, ref) {
				for ( var i = 0; i < markers.length; i++) {
					var subscriber = markers[i].subscriber;
					if(subscriber && subscriber.userRef == ref){
						return markers[i];
					}
				}
				return null;
			}, 
			traceDirections : function ($scope){
		    	var car = null;
		    	if($scope.currentSubscriber && $scope.currentSubscriber.locomotion == "CAR"){
		    		car = $scope.currentSubscriber;
		    	}else if($scope.currentSubscriber && $scope.currentSubscriber.locomotion == "AUTOSTOP"){
		    		if($scope.currentSubscriber.carRef){
		    			car = $scope.refSubscribers[$scope.currentSubscriber.carRef];	
		    		}
		    	}
		    	if(car){
					$scope.directionsDisplay.setMap($scope.myMap);
					var start = new google.maps.LatLng(car.address.location.lat, car.address.location.lng);
					var end = new google.maps.LatLng($scope.event.address.location.lat, $scope.event.address.location.lng);
					var waypts = [];
					if(car && car.car && car.car.passengers){
					  for (var i = 0; i < car.car.passengers.length; i++) {
						  var passenger = $scope.refSubscribers[car.car.passengers[i]];
						  var loc = new google.maps.LatLng(passenger.address.location.lat, passenger.address.location.lng)
					      waypts.push({
					    	  location:loc, 
					          stopover:false
					      });
					    }
					  }
					var request = {
					      origin:start,
					      destination:end,
					      waypoints: waypts,
					      optimizeWaypoints: true,
					      travelMode: google.maps.DirectionsTravelMode.DRIVING
					  };
					  $scope.directionsService.route(request, function(response, status) {
					    if (status == google.maps.DirectionsStatus.OK) {
					    	$scope.directionsDisplay.setDirections(response);
					    }
					  });
		    	}else{
		    		$scope.directionsDisplay.setMap(null);
		    		$scope.myMap.fitBounds($scope.bounds);
		    	}
		    }
	   };
   });
});





