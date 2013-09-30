
angular.module('errorModule', [], function($provide){

    $provide.factory('errorService', ['$q', function($q) {

        return {
            formatErrors : function(data, callback){
                if(data){
                    var someErrors = {};
                    if(data.errors){
                        someErrors = data.errors;
                    }else{
                        someErrors = data;
                    }
                    for(var anError in someErrors){
                        var msgs = someErrors[anError];
                        if(typeof msgs === 'string'){
                            callback(msgs);
                        }else{
                           for(var i in msgs){
                               if(callback){
                                   callback(msgs[i]);
                               }
                           }
                        }
                    }
                }else{
                    if(callback){
                        callback(data);
                    }
                }
            }
        };
    }]);
});