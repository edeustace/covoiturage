'use strict';

/* App Module */

var app = angular.module('covoiturage', []);

app.directive('googleplace', function() {

	return {
		require : 'ngModel',
		link : function(scope, element, attrs, model) {
			var options = {
				types : [],
				componentRestrictions : {
					country : 'fr'
				}
			};
			var autocomplete = new google.maps.places.Autocomplete(element[0]);
			scope.gPlace = autocomplete;

			model.$formatters.unshift(function(valueFromModel) {
				if (valueFromModel) {
					return valueFromModel.address;
				}
				return null;
			});

			google.maps.event.addListener(autocomplete, 'place_changed',
					function() {
						scope.$apply(function() {
							var address = {
								address : '',
								location : {
									lat : '',
									lng : ''
								}
							};
							var place = autocomplete.getPlace();
							if (!place && !place.geometry) {
								// Inform the user that a place was not
								// found and return.
								address.address = null;
							}
							var loc = place.geometry.location;
							address.address = element.val();
							if(loc){
								address.location.lat = loc.lat();
								address.location.lng = loc.lng();
							}
							model.$setViewValue(address);
						});
					});
		}
	};
});