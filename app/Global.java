import dao.*;
import dao.mongodb.*;
import models.*;
import play.Application;
import play.GlobalSettings;
import play.mvc.Call;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.PlayAuthenticate.Resolver;
import com.feth.play.module.pa.exceptions.AccessDeniedException;
import com.feth.play.module.pa.exceptions.AuthException;

import controllers.routes;

public class Global extends GlobalSettings {

    public void onStart(Application app) {
        PlayAuthenticate.setResolver(new Resolver() {

            @Override
            public Call login() {
                // Your login page
                return routes.Application.login();
            }

            @Override
            public Call afterAuth() {
                // The user will be redirected to this page after authentication
                // if no original URL was saved
                return routes.Application.index();
            }

            @Override
            public Call afterLogout() {
                return routes.Application.index();
            }

            @Override
            public Call auth(final String provider) {
                // You can provide your own authentication implementation,
                // however the default should be sufficient for most cases
                return com.feth.play.module.pa.controllers.routes.Authenticate
                        .authenticate(provider);
            }

            @Override
            public Call askMerge() {
                return routes.Account.askMerge();
            }

            @Override
            public Call askLink() {
                return routes.Account.askLink();
            }

            @Override
            public Call onException(final AuthException e) {
                if (e instanceof AccessDeniedException) {
                    return routes.Signup
                            .oAuthDenied(((AccessDeniedException) e)
                                    .getProviderKey());
                }

                // more custom problem handling here...
                return super.onException(e);
            }
        });

        //Init des Daos
        UserDao userDao = new MongoDbUserDao();
        userDao.init();
        User.setDao(userDao);

        EventDao eventDao = new MongoDbEventDao();
        eventDao.init();
        Event.setDao(eventDao);

        TopicDao topicDao = new MongoDbTopicDao();
        topicDao.init();
        Topic.setDao(topicDao);

        ChatMessageDao chatMessageDao = new MongoDbChatMessageDao();
        chatMessageDao.init();
        ChatMessage.setDao(chatMessageDao);

        TokenActionDao tokenActionDao = new MongoDbTokenActionDao();
        tokenActionDao.init();
        TokenAction.setDao(tokenActionDao);


    }
}