'use strict';

/* App Module */

angular.module('covoiturage', ['ui.map', 'ui.keypress', 'covoiturageFilter', 'mailUtils', 'ui.bootstrap', 'googleplace', 'mapService', 'eventModule', 'chatModule', 'errorModule', 'luegg.directives', 'infinite-scroll', 'ui.chat-message', 'snap'])
.controller('EventCtrl', EventCtrl);
