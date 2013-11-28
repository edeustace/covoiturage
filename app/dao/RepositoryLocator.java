package dao;

/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 27/11/13
 * Time: 20:04
 * To change this template use File | Settings | File Templates.
 */
public class RepositoryLocator {

    UserDao userDao;

    EventDao eventDao;

    TopicDao topicDao;

    ChatMessageDao chatMessageDao;

    TokenActionDao tokenActionDao;

    private static RepositoryLocator repositoryLocator;

    public UserDao getUserDao() {
        return userDao;
    }

    public EventDao getEventDao() {
        return eventDao;
    }

    public TopicDao getTopicDao() {
        return topicDao;
    }

    public ChatMessageDao getChatMessageDao() {
        return chatMessageDao;
    }

    public TokenActionDao getTokenActionDao() {
        return tokenActionDao;
    }

    public static RepositoryLocator getRepositoryLocator() {
        return repositoryLocator;
    }

    public static void load(UserDao userDao, EventDao eventDao, TopicDao topicDao, ChatMessageDao chatMessageDao, TokenActionDao tokenActionDao){
        repositoryLocator = new RepositoryLocator(userDao, eventDao, topicDao, chatMessageDao, tokenActionDao);
        repositoryLocator.userDao.init();
        repositoryLocator.eventDao.init();
        repositoryLocator.topicDao.init();
        repositoryLocator.chatMessageDao.init();
        repositoryLocator.tokenActionDao.init();
    }

    private RepositoryLocator(UserDao userDao, EventDao eventDao, TopicDao topicDao, ChatMessageDao chatMessageDao, TokenActionDao tokenActionDao) {
        this.userDao = userDao;
        this.eventDao = eventDao;
        this.topicDao = topicDao;
        this.chatMessageDao = chatMessageDao;
        this.tokenActionDao = tokenActionDao;
    }
}
