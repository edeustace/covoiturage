'use strict';

/* Filters */

angular.module('covoiturageFilter', []).filter('auto', function() {
  return function(subscribers, filter, markers) {
    	if(filter){
		var result = new Array();				
		for(var i=0;i<subscribers.length;i++){
			var subscriber = subscribers[i];
			var type = subscriber.locomotion;
			if(type === filter){
				result.push(subscriber);
			}
		}
		return result;
	}else{
		return subscribers;
	}
  };
});
