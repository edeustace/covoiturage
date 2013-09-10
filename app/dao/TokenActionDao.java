package dao;

import models.TokenAction;
import models.User;

/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 10/09/13
 * Time: 21:35
 * To change this template use File | Settings | File Templates.
 */
public interface TokenActionDao extends Dao<TokenAction> {

    TokenAction findByToken(final String token, final TokenAction.Type type);

    void deleteByUser(final User u, final TokenAction.Type type);
}
