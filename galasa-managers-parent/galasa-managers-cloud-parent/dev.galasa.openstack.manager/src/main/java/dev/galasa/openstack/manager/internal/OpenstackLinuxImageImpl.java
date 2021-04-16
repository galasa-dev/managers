/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019,2021.
 */
package dev.galasa.openstack.manager.internal;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.ICredentials;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.ipnetwork.ICommandShell;
import dev.galasa.ipnetwork.IIpHost;
import dev.galasa.ipnetwork.IpNetworkManagerException;
import dev.galasa.linux.LinuxManagerException;
import dev.galasa.linux.spi.ILinuxProvisionedImage;
import dev.galasa.openstack.manager.OpenstackManagerException;
import dev.galasa.openstack.manager.internal.json.Floatingip;
import dev.galasa.openstack.manager.internal.json.GalasaMetadata;
import dev.galasa.openstack.manager.internal.json.Network;
import dev.galasa.openstack.manager.internal.json.Port;
import dev.galasa.openstack.manager.internal.json.Server;
import dev.galasa.openstack.manager.internal.json.ServerRequest;
import dev.galasa.openstack.manager.internal.properties.GenerateTimeout;
import dev.galasa.openstack.manager.internal.properties.LinuxCredentials;
import dev.galasa.openstack.manager.internal.properties.LinuxKeyPair;

public class OpenstackLinuxImageImpl extends OpenstackServerImpl implements ILinuxProvisionedImage {

    private static final Log          logger = LogFactory.getLog(OpenstackLinuxImageImpl.class);

    public final OpenstackManagerImpl manager;
    private final OpenstackHttpClient openstackHttpClient;
    public final String               instanceName;
    public final String               image;
    public final String               tag;

    private String                    id;

    private String                    hostname;

    private Server                    openstackServer;
    private Port                      openstackPort;
    private Floatingip                openstackFloatingip;

    private OpenstackIpHost           ipHost;

    private ICommandShell             commandShell;

    private FileSystem                fileSystem;

    private Path                      pathRoot;
    private Path                      pathTemp;
    private Path                      pathHome;

    public OpenstackLinuxImageImpl(@NotNull OpenstackManagerImpl manager,
            @NotNull OpenstackHttpClient openstackHttpClient, @NotNull String instanceName, @NotNull String image,
            @NotNull String tag) {
        this.manager = manager;
        this.openstackHttpClient = openstackHttpClient;
        this.instanceName = instanceName;
        this.image = image;
        this.tag = tag;
    }

    @Override
    public @NotNull String getImageID() {
        return this.instanceName;
    }

    @Override
    public @NotNull IIpHost getIpHost() {
        return this.ipHost;
    }

    @Override
    public @NotNull ICredentials getDefaultCredentials() throws LinuxManagerException {
        try {
            return this.manager.getFramework().getCredentialsService().getCredentials(LinuxCredentials.get(this.image));
        } catch (Exception e) {
            throw new LinuxManagerException("Unable to create credentials", e);
        }
    }

    public void discard() {
        try {
            // *** TODO delete the instance in Openstack
            if (this.openstackServer != null) {
                try {
                    deleteServer(this.openstackServer, this.openstackServer.name,
                            this.manager.getFramework().getTestRunName(), this.manager.getDSS(),
                            this.openstackHttpClient);
                } catch (Exception e) {
                    logger.warn("Failed to delete the server", e);
                }
            }

            // *** Delete the Floating IP
            if (this.openstackFloatingip != null) {
                try {
                    deleteFloatingIp(this.openstackFloatingip, this.openstackFloatingip.floating_ip_address,
                            this.manager.getFramework().getTestRunName(), this.manager.getDSS(),
                            this.openstackHttpClient);
                } catch (Exception e) {
                    logger.warn("Failed to delete the floating ip", e);
                }
            }
            logger.info("OpenStack Linux instance " + this.instanceName + " for tag " + tag + " has been discarded");
        } catch (Exception e) {
            logger.warn("Unable to discard OpenStack Linux instance " + this.instanceName, e);
        }
    }

    public void build() throws OpenstackManagerException, ConfigurationPropertyStoreException {
        logger.info("Building OpenStack Linux instance " + this.instanceName + " with image " + this.image + " for tag "
                + this.tag);

        String flavor = "m1.medium";
        int generateTimeout = GenerateTimeout.get();
        generateTimeout = 1;

        Server server = new Server();
        server.name = this.instanceName;
        server.imageRef = this.openstackHttpClient.getImageId(this.image);
        server.flavorRef = this.openstackHttpClient.getFlavourId(flavor);
        server.availability_zone = "nova"; // TODO cps
        server.metadata = new GalasaMetadata();
        server.metadata.galasa_run = this.manager.getFramework().getTestRunName();
        server.key_name = LinuxKeyPair.get(this.image);

        if (server.imageRef == null) {
            throw new OpenstackManagerException("Image " + this.image + " is missing in OpenStack");
        }

        if (server.flavorRef == null) {
            throw new OpenstackManagerException("Flavor " + flavor + " is missing in OpenStack");
        }

        ServerRequest serverRequest = new ServerRequest();
        serverRequest.server = server;

        try {
            this.openstackServer = this.openstackHttpClient.createServer(serverRequest);
            this.id = this.openstackServer.id;

            Instant expire = Instant.now();
            expire = expire.plus(generateTimeout, ChronoUnit.MINUTES);

            String serverJson = "";
            String state = null;
            boolean up = false;
            while (expire.compareTo(Instant.now()) > 0) {
                Thread.sleep(5000);

                Server checkServer = this.openstackHttpClient.getServer(this.id);
                if (checkServer != null) {
                    serverJson = this.manager.getGson().toJson(checkServer);
                    if (checkServer.power_state != null) {
                        if (checkServer.power_state == 1) {
                            logger.info("OpenStack Linux instance " + this.instanceName
                                    + " has been built and is running, compute server id = " + this.openstackServer.id);
                            this.openstackServer = checkServer;
                            up = true;
                            break;
                        }
                        state = checkServer.task_state;
                    }
                }

                logger.trace("Still waiting for OpenStack Linux instance " + this.instanceName + " to be built, task="
                        + state);
            }

            if (!up) {
                throw new OpenstackManagerException(
                        "OpenStack failed to build the server in time, last response was:-\n" + serverJson);
            }

            // *** Get the network port details
            this.openstackPort = this.openstackHttpClient.retrievePort(this.openstackServer.id);
            if (this.openstackPort == null) {
                throw new OpenstackManagerException("OpenStack did not allocate a port for this instance");
            }

            // *** Locate the external network
            Network network = this.openstackHttpClient.findExternalNetwork(null); // TODO provide means to specify
            // network

            if (network == null) {
                throw new OpenstackManagerException("Unable to select an external network to allocate a floatingip on");
            }

            // *** Assign a floating IPv4 address
            this.openstackFloatingip = this.openstackHttpClient.allocateFloatingip(this.openstackPort, network);
            logger.info("OpenStack Linux Server " + this.instanceName + " assigned IP address "
                    + this.openstackFloatingip.floating_ip_address);

            // *** Create the DSS properties to manager the Floating IP Address
            registerFloatingIp(this.manager.getDSS(), this.manager.getFramework().getTestRunName(),
                    this.openstackFloatingip);

            // *** Default hostname to the floatingip
            this.hostname = this.openstackFloatingip.floating_ip_address;

            // *** Create the IPHost
            this.ipHost = new OpenstackIpHost(this.hostname, getDefaultCredentials());

            // *** Create the Commandshell
            this.commandShell = this.manager.getIpNetworkManager().getCommandShell(this.ipHost,
                    this.ipHost.getDefaultCredentials());

            // *** Create the filesystem
            this.fileSystem = this.manager.getIpNetworkManager().getFileSystem(this.getIpHost());

            this.pathRoot = this.fileSystem.getPath("/");
            this.pathTemp = this.fileSystem.getPath("/tmp");

            try {
                String homeDir = this.commandShell.issueCommand("pwd");
                if (homeDir == null) {
                    throw new LinuxManagerException("Unable to determine home directory, response null");
                }
                homeDir = homeDir.replaceAll("\\r\\n?|\\n", "");
                this.pathHome = this.fileSystem.getPath(homeDir);
                logger.info("Home directory for linux image tagged " + tag + " is " + homeDir);
            } catch (IpNetworkManagerException e) {
                throw new OpenstackManagerException("Unable to determine home directory", e);
            }
        } catch (OpenstackManagerException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenstackManagerException("Unable to start OpenStack Linux server", e);
        }

    }

    @Override
    public @NotNull ICommandShell getCommandShell() throws LinuxManagerException {
        return this.commandShell;
    }

    @Override
    public @NotNull Path getRoot() throws LinuxManagerException {
        return this.pathRoot;
    }

    @Override
    public @NotNull Path getHome() throws LinuxManagerException {
        return this.pathHome;
    }

    @Override
    public @NotNull Path getTmp() throws LinuxManagerException {
        return this.pathTemp;
    }

}
