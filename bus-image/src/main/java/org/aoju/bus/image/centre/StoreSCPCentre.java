package org.aoju.bus.image.centre;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.image.metric.Connection;
import org.aoju.bus.image.metric.TransferCapability;
import org.aoju.bus.image.plugin.StoreSCP;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class StoreSCPCentre extends AbstractCentre {

    protected ExecutorService executor;
    protected ScheduledExecutorService scheduledExecutor;
    private StoreSCP storeSCP;

    public static StoreSCPCentre Builder() {
        return new StoreSCPCentre();
    }

    @Override
    public boolean isRunning() {
        return storeSCP.getConnection().isListening();
    }

    @Override
    public synchronized void start() {
        if (isRunning()) {
            throw new InstrumentException("Cannot start a Listener because it is already running.");
        }

        storeSCP.setStatus(0);

        Connection conn = storeSCP.getConnection();
        if (args.isBindCallingAet()) {
            args.configureBind(storeSCP.getApplicationEntity(), conn, node);
        } else {
            args.configureBind(conn, node);
        }

        args.configure(conn);
        try {
            args.configureTLS(conn, null);
        } catch (IOException e) {
            e.printStackTrace();
        }


        storeSCP.getApplicationEntity().setAcceptedCallingAETitles(args.getAcceptedCallingAETitles());

        URL transferCapabilityFile = args.getTransferCapabilityFile();
        if (transferCapabilityFile != null) {
            storeSCP.loadDefaultTransferCapability(transferCapabilityFile);
        } else {
            storeSCP.getApplicationEntity()
                    .addTransferCapability(new TransferCapability(null, "*", TransferCapability.Role.SCP, "*"));
        }

        device.start();
    }

    @Override
    public synchronized void stop() {
        if (device != null) {
            device.stop();
        }
        if (executor != null) {
            executor.shutdown();
            scheduledExecutor = null;
        }
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
            executor = null;
        }
    }

    @Override
    public StoreSCPCentre build() {
        if (ObjectUtils.isEmpty(node)) {
            throw new NullPointerException("The node cannot be null.");
        }
        if (ObjectUtils.isEmpty(args)) {
            throw new NullPointerException("The args cannot be null.");
        }
        if (ObjectUtils.isEmpty(device)) {
            throw new NullPointerException("The device cannot be null.");
        }

        executor = executerService();
        scheduledExecutor = scheduledExecuterService();

        device.setExecutor(executor);
        device.setScheduledExecutor(scheduledExecutor);
        return this;
    }

    public StoreSCP getStoreSCP() {
        return storeSCP;
    }

    public void setStoreSCP(String path) {
        this.storeSCP = new StoreSCP(path);
    }

    protected ExecutorService executerService() {
        return Executors.newCachedThreadPool();
    }

    protected ScheduledExecutorService scheduledExecuterService() {
        return Executors.newSingleThreadScheduledExecutor();
    }

}