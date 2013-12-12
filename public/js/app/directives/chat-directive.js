
angular.module( 'ui.chat-message', [] )
.directive( 'chatMessages', [ '$compile', '$window', '$templateCache', '$position', function ( $compile, $window, $templateCache, $position) {

  return {
    restrict: 'EA',
    scope: false,

    link: function link ( scope, element, attrs ) {
        var templateUrl = 'template/chat/chat.html' ;
        var template = $templateCache.get(templateUrl);

        var chat = $compile( template )( scope );

        var transitionTimeout;
        var popupTimeout;
        var $body;
        var triggers = {
            show : 'click',
            hide: 'click'
        }
        scope.chatIsOpen = false;

        function toggleTooltipBind () {
            if ( ! scope.chatIsOpen ) {
              showTooltipBind();
            } else {
              hideTooltipBind();
            }
        }

        // Show the tooltip with delay if specified, otherwise show it immediately
        function showTooltipBind() {
            scope.$apply( show );
        }

        function hideTooltipBind () {
            scope.$apply(function () {
              hide();
            });
        }

        // Show the tooltip popup element.
        function show() {
            var position,
                ttWidth,
                ttHeight,
                ttPosition;

            // Set the initial positioning.
            chat.css({ top: 0, left: 0, display: 'block' });

            element.after( chat );

            // Get the position of the directive element.
            position = $position.position( element );

            // Get the height and width of the chat so we can center it.
            ttWidth = chat.prop( 'offsetWidth' );
            ttHeight = chat.prop( 'offsetHeight' );


            ttPosition = {
              top: position.top + position.height / 2 - ttHeight / 2,
              left: position.left - ttWidth
            };

            ttPosition.top += 'px';
            ttPosition.left += 'px';

            // Now set the calculated positioning.
            chat.css( ttPosition );

            // And show the chat.
            scope.chatIsOpen = true;
        }

        // Hide the chat popup element.
        function hide() {
            // First things first: we don't show it anymore.
            scope.chatIsOpen = false;
            chat.remove();
        }


        element.bind( 'click', toggleTooltipBind );

        // Make sure chat is destroyed and removed.
        scope.$on('$destroy', function onDestroyChatMessage() {
            if ( scope.chatIsOpen ) {
              hide();
            } else {
              chat.remove();
            }
        });

    },

  };
}]);