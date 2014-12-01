package com.zackehh.javaspaces.ui.listeners;

import net.jini.core.event.RemoteEventListener;
import net.jini.export.Exporter;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.tcp.TcpServerEndpoint;

import java.rmi.RemoteException;

/**
 * Created by iwhitfield on 01/12/14.
 */
public class GenericNotificationListener {

    protected Exporter remoteExporter;
    protected RemoteEventListener listener;

    public GenericNotificationListener() throws RemoteException {
        remoteExporter =
                new BasicJeriExporter(TcpServerEndpoint.getInstance(0),
                        new BasicILFactory(), false, true);
    }

    public RemoteEventListener getListener(){
        return listener;
    }

}
