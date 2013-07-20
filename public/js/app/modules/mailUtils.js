
angular.module('mailUtils', [], function($provide){

   $provide.factory('mailUtils', function() {
	   return {
		   validateMail : function(email){
			   var reg = new RegExp('^[a-z0-9]+([_|\.|-]{1}[a-z0-9]+)*@[a-z0-9]+([_|\.|-]{1}[a-z0-9]+)*[\.]{1}[a-z]{2,6}$', 'i');
				if(reg.test(email)){
					return(true);
				}else{
					return(false);
				}
		   }, 
		   pushMails : function (mails, array){
			   if(!array){
					array = new Array();
				}
				if(mails.indexOf(";")>0){
					var reg=new RegExp("[ ,;]+", "g");
					var emails = mails.split(reg);
					for ( var int = 0; int < emails.length; int++) {
						var email = emails[int];
						if(this.validateMail(email)){
							array.push(email);	
						}
					}
				}else{
					if(this.validateMail(mails)){
						array.push(mails);	
					}
				}
				return array;
		   }
	   };
   });
});