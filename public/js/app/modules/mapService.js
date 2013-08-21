


angular.module('mapService', [], function($provide){

   $provide.factory('mapService', function() {
	   function floatEqual(f1, f2) {
			return (Math.abs(f1 - f2) < 0.000001);
		}


	   $mapData = {
	        map :{},
	        mapOptions : {
                center: new google.maps.LatLng(46.65278, -1.424961),
                zoom: 9,
                mapTypeId: google.maps.MapTypeId.ROADMAP
            },
	        markers : new Array(),
	        bounds : new google.maps.LatLngBounds(),
	        directionsDisplay : new google.maps.DirectionsRenderer(),
            directionsService : new google.maps.DirectionsService(),
	   };

	   return {
	        init : function(map){
	            $mapData.map = map;
	        },
	        getMapOptions : function(){
	            return $mapData.mapOptions;
	        },
	        getMarker : function(indice){
                return $mapData.markers[indice];
	        },
	        getMarkers : function(callback){
	            if(callback){
	                for(var i in $mapData.markers){
	                    callback($mapData.markers[i]);
	                }
	            }else{
	                return $mapData.markers;
	            }
	        },
		    addMarkerEvent : function (event){
		    	$mapData.mapOptions.center = new google.maps.LatLng(event.address.location.lat, event.address.location.lng);
		    	
		    	var marker = new google.maps.Marker({
		            map: $mapData.map,
		            position: new google.maps.LatLng(event.address.location.lat, event.address.location.lng),
		            icon:event.picto, 
		            visible : true 
		          });
		    	marker.type = "EVENT";
		    	marker.name = event.name;
                marker.address = event.address.description;
		    	$mapData.markers.push(marker);
		    	$mapData.bounds.extend(marker.getPosition());
		    },
            setMarkerEvent : function (event){
                var current = null;
                for(var i in $mapData.markers){
                    var aMarker = $mapData.markers[i];
                    if(aMarker.type === "EVENT"){
                        aMarker.setPosition(new google.maps.LatLng(event.address.location.lat, event.address.location.lng));
                        $mapData.bounds.bounds.extend(marker.getPosition());
                    }
                }
                $mapData.map.fitBounds($mapData.bounds);
            },
		    addMarkerSubscriber : function (subscriber){
		    	var marker = this.findMarkerByUserRef($mapData.markers, subscriber.userRef);
		    	if(marker){
		    		marker.setVisible(subscriber.visible);
		    		marker.setIcon(subscriber.picto);
		    		marker.setVisible(subscriber.visible);
		    	}else{
		    		marker = this.findMarkerByLatLng($mapData.markers, subscriber.address.location.lat, subscriber.address.location.lng);
		    		if(marker){
		    			marker.setPosition(new google.maps.LatLng(subscriber.address.location.lat, subscriber.address.location.lng));
		    			marker.setVisible(subscriber.visible);
		    			marker.setIcon(subscriber.picto);
		    		}else{
			        	marker = new google.maps.Marker({
			                map: $mapData.map,
			                position: new google.maps.LatLng(subscriber.address.location.lat, subscriber.address.location.lng),
			                icon:subscriber.picto, 
			                visible : subscriber.visible
			              });
			        	marker.type = subscriber.locomotion;
			        	$mapData.markers.push(marker);
			        	$mapData.bounds.extend(marker.getPosition());
		    		}
		    	}
		    	marker.type = subscriber.locomotion;
		    	marker.subscriber = subscriber;
		    },
		    updateMarkerSubscriber : function (subscriber){
                var marker = this.findMarkerByUserRef($mapData.markers, subscriber.userRef);
                if(marker){
                        marker.setPosition(new google.maps.LatLng(subscriber.address.location.lat, subscriber.address.location.lng));
                        marker.setVisible(subscriber.visible);
                        marker.setIcon(subscriber.picto);
                }else{
                    marker = new google.maps.Marker({
                        map: $mapData.map,
                        position: new google.maps.LatLng(subscriber.address.location.lat, subscriber.address.location.lng),
                        icon:subscriber.picto,
                        visible : subscriber.visible
                      });
                    marker.type = subscriber.locomotion;
                    $mapData.markers.push(marker);
                    $mapData.bounds.extend(marker.getPosition());
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
			traceDirections : function (start, end, points){
		    	if(start && end){
					$mapData.directionsDisplay.setMap($mapData.map);
					var start = new google.maps.LatLng(start.location.lat, start.location.lng);
					var end = new google.maps.LatLng(end.location.lat, end.location.lng);
					var waypts = [];
					if(points){
					  for (var i in points) {
						  var point = points[i];
						  var loc = new google.maps.LatLng(point.location.lat, point.location.lng)
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
					  $mapData.directionsService.route(request, function(response, status) {
					    if (status == google.maps.DirectionsStatus.OK) {
					    	$mapData.directionsDisplay.setDirections(response);
					    }
					  });
		    	}else{
		    		$mapData.directionsDisplay.setMap(null);
		    		$mapData.map.fitBounds($mapData.bounds);
		    	}
		    },
		    fitBounds : function(){
		        $mapData.map.fitBounds($mapData.bounds);
		    }
	   };
   });
});





