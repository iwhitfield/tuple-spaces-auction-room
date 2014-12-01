package com.zackehh.javaspaces.ui.listeners;

import net.jini.core.event.RemoteEventListener;
import net.jini.export.Exporter;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.tcp.TcpServerEndpoint;

import java.rmi.RemoteException;

/**
 * A generic implementation to create a RemoteEventListener
 * easily, without having to explicitly define an Exporter.
 * This is inherited by any notifiers used inside the app.
 */
public class GenericNotificationListener {

    /**
     * The exporter used to gain the listener.
     */
    protected Exporter remoteExporter;

    /**
     * Our RemoteEventListener, starting at null.
     */
    protected RemoteEventListener listener;

    /**
     * Does nothing except instantiate the Exporter. This ensures
     * that any classes inheriting from GenericNotificationListener
     * do not have to explicitly create their own Exporter.
     *
     * @throws RemoteException
     */
    public GenericNotificationListener() throws RemoteException {
        remoteExporter =
                new BasicJeriExporter(TcpServerEndpoint.getInstance(0),
                        new BasicILFactory(), false, true);
    }

    /**
     * A public accessor method to the RemoteEventListener.
     * This is what should be called before passing the listener
     * into any space#notify method calls.
     *
     * @return listener     the RemoteEventListener
     */
    public RemoteEventListener getListener(){
        return listener;
    }

}
