package org.test;

import org.red5.server.adapter.ApplicationAdapter;
import org.red5.server.api.IConnection;

/**
 *
 */
public class Application  extends ApplicationAdapter {

    public void appDisconnect(IConnection conn) {
        super.appDisconnect(conn);
    }

    public boolean appStart() {
        return true;
    }

    public void appStop() {

    }

    public boolean appConnect(IConnection conn, Object[] params) {
        return true;
    }

    public Object echo(Object p) {
        return p;
    }
}
