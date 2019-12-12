package dev.galasa.docker.internal;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.validation.constraints.NotNull;

import java.util.Random;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.docker.DockerManagerException;
import dev.galasa.docker.DockerProvisionException;
import dev.galasa.docker.IDockerContainer;
import dev.galasa.docker.IDockerExec;
import dev.galasa.docker.IDockerImage;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;

/**
 * Docker Container implementation used to contain all container configurations.
 * 
 * @author James Davies
 */
public class DockerContainerImpl implements IDockerContainer {

    private static final String             CONTAINER_NAME_PREFIX = "GALASA_";

    private IFramework                      framework;
    private DockerManagerImpl               dockerManager;
    private String                          tag;
    private DockerServerImpl                dockerServer;
    private DockerImageImpl                 image;
    private Boolean                         autoStartup;
    private DockerSlotImpl                  dockerSlot;
    private IDynamicStatusStoreService      dss;
    private String                          containerID;
    private String                          containerName;

    private boolean                         leaveRunning;
    private boolean                         alreadyUp;
    private boolean                         alreadyDefined;

    private Map<String, List<InetSocketAddress>> exposedPorts = new HashMap<>();
    private Random random = new Random();

    private static final Log                logger = LogFactory.getLog(DockerContainerImpl.class);

    /**
     * Registers the docker container in the DSS within the docker namespace
     * 
     * @param framework
     * @param dockerManager
     * @param tag
     * @param dockerServer
     * @param image
     * @param start
     * @param slot
     * @throws DockerProvisionException
     */
    public DockerContainerImpl(IFramework framework, DockerManagerImpl dockerManager, String tag,
            DockerServerImpl dockerServer, DockerImageImpl image, Boolean start, DockerSlotImpl slot) throws DockerProvisionException {
        this.framework              = framework;
        this.dockerManager          = dockerManager;
        this.tag                    = tag;
        this.dockerServer           = dockerServer;
        this.image                  = image;
        this.autoStartup            = start;
        this.dockerSlot             = slot;
        
        try {
            this.dss = framework.getDynamicStatusStoreService(dockerManager.NAMESPACE);
            this.containerName = getContainerName(this.dockerSlot);
        } catch (DynamicStatusStoreException e) {
            throw new DockerProvisionException("Failed to instantiate Docker container. Could not determine the container name from the DSS: ", e);
        }
    }

    /**
     * Check container observers the state of the container, and returns it to any expected state.
     * 
     * @throws DockerProvisionException
     */
    public void checkContainer() throws DockerProvisionException {
        try {
            logger.debug("Checking if container should be left running");
            checkLeaveRunning();
            logger.debug("Checking the current state of the container");
            checkContainerState();
            this.image.locateImage();

			if (!alreadyDefined) {
				this.image.pullImage();
				try {
                    JsonObject create = new JsonObject();
                    create.addProperty("Image", this.image.getFullName());

					JsonObject hostConfig = new JsonObject();
					hostConfig.addProperty("PublishAllPorts", Boolean.TRUE);
					create.add("HostConfig", hostConfig);


					logger.debug("Creating Docker Container '" + tag + "'");
					JsonObject newContainer = dockerServer.createContainer(containerName, create);
					logger.debug("Created Docker Container '" + tag + "'");
					containerID = newContainer.get("Id").getAsString();
					if (containerID == null || containerID.trim().isEmpty()) {
						throw new DockerManagerException("Container ID is missing");
					}
				} catch(DockerManagerException e) {
					throw e;
				} catch(Exception e) {
					throw new DockerManagerException("Unable to create the Docker Container '" + this.tag + "'", e);
				}
			}

			logger.info("Container '" + tag + "' created under name '" + containerName + "'");
		} catch(DockerManagerException | DynamicStatusStoreException e) {
			throw new DockerProvisionException("Unable to prepare the Docker Container '" + this.tag + "'", e);
		}
    }

    /**
     * Used to issue the "docker start" command
     */
    @Override
    public void start() throws DockerManagerException {
        startDockerContainer();

    }

    /**
     * Passed the docker start commands to the docker server if the container is not already running.
     * 
     * @throws DockerManagerException
     */
    private void startDockerContainer() throws DockerManagerException {
        if (alreadyUp) {
            logger.info("Container already running");
            return;
        }
        try {
            logger.info("Checking container before attempting start.");
            checkContainer();
            logger.debug("Starting docker container: " + tag);
            dockerServer.startContainer(containerID);
            logger.info("Started Docker container: " + tag);
            Thread.sleep(2000);
        } catch(Exception e) {
            throw new DockerManagerException("Failed to start docker container: " + tag, e);
        }

        extractContainerExposedPortsFromDockerServer();

    }

    /**
     * Issues the "docker stop" command
     */
    @Override
    public void stop() throws DockerManagerException {
        stopDockerContainer();

    }

    /**
     * Passes the docker stop command to the docker server if the container is nor already stopped
     * @throws DockerManagerException
     */
    private void stopDockerContainer() throws DockerManagerException {
        if(!isRunning()) {
            logger.info("Stop command ignored, container already stopped.");
            return;
        }
        try {
            logger.info("Stopping docker container: " + this.tag);
            killContainer();
            logger.info("Container " + this.tag + " has stopped");
            alreadyUp = false;
        } catch(DockerManagerException e) {
            throw new DockerManagerException("Unable to stop docker container: " + this.tag, e);
        }
    }

    /**
     * Returns the image name of this container.
     */
    @Override
    public IDockerImage getDockerImage() {
        return this.image;
    }

    /**
     * Once a container is started, this method is used to query the container information to determine opened ports
     * 
     * @throws DockerManagerException
     */
    private void extractContainerExposedPortsFromDockerServer() throws DockerManagerException {
        try {
            JsonObject containerInfo = dockerServer.getContainer(containerID);

            JsonObject ports = retrievePorts(containerInfo);
            if (ports != null){
                for (Object port : ports.entrySet()) {
                    if(port instanceof Entry) {
                        Entry<?,?> portEntry = (Entry<?,?>)port;
                        exposedPorts.put((String)portEntry.getKey(), getSocketsFromPort(portEntry));
                    }
                } 
            }
        } catch (Exception e) {
            throw new DockerManagerException("Unable to determine exposed ports in the Docker Container: " + this.tag, e);
        }
    }

    /**
     * Used by the extractContainerExposedPortsFromDockerServer() to retrieve ports from the container information.
     * 
     * @param containerInfo
     * @return Ports
     */
    private JsonObject retrievePorts(JsonObject containerInfo) {
        JsonObject networkSettings = containerInfo.get("NetworkSettings").getAsJsonObject();

        if (networkSettings != null) {
            return networkSettings.get("Ports").getAsJsonObject();
        } else return null;
    }

    /**
     * Returns a list of sockets from the JsonObect containing ports, used by the extractContainerExposedPortsFromDockerServer()
     * 
     * @param portEntry
     * @return Sockets
     */
    private ArrayList<InetSocketAddress> getSocketsFromPort(Entry<?,?> portEntry) {
        ArrayList<InetSocketAddress> sockets = new ArrayList<>();

        if(portEntry.getValue() instanceof JsonArray) {
            JsonArray hostPorts = (JsonArray)portEntry.getValue();
            for (Object hostPort : hostPorts) {
                if (hostPort instanceof JsonObject) {
                    String sHostIp;
                    int iHostPort;
                    JsonElement hostIP = ((JsonObject)hostPort).get("HostIP");
                    JsonElement hPort = ((JsonObject)hostPort).get("HostPort");

                    if (hostIP == null || hostIP.getAsString().equals("0.0.0.0")) {
                        sHostIp = dockerServer.getHost();
                        iHostPort = hPort.getAsInt();
                    } else {
                        sHostIp = hostIP.getAsString();
                        iHostPort = hPort.getAsInt();
                    }
                    InetSocketAddress socket = new InetSocketAddress(sHostIp, iHostPort);
                    sockets.add(socket);
                }
            }
        }
        return sockets;
    }

    /**
     * Returns the Map of the exposed ports
     * 
     * @throws DockerManagerException
     */
    @Override
    public Map<String, List<InetSocketAddress>> getExposedPorts() throws DockerManagerException {
        return exposedPorts;
    }

    /**
     * Retrieves the first open socket
     * `
     * @param exposed port
     * @return InetSocketAddress
     */
    @Override
    public InetSocketAddress getFirstSocketForExposedPort(String exposedPort) {
        List<InetSocketAddress> sockets = exposedPorts.get(exposedPort);
		if (sockets == null || sockets.isEmpty()) {
			return null;
		}
		return sockets.get(0);
    }

    /**
     * Retrieves a random open socket
     * 
     * @param exposed port
     * @return InetSocketAddress
     */
    @Override
    public InetSocketAddress getRandomSocketForExposedPort(String exposedPort) {
        List<InetSocketAddress> sockets = exposedPorts.get(exposedPort);
		if (sockets == null || sockets.isEmpty()) {
		    return null;
		}
		return sockets.get(random.nextInt(sockets.size()));
    }

    /**
     * Submit exec commands to be executed on the docker container.
     * 
     * @param String... commands
     * @return IDockerExec
     */
    @Override
    public IDockerExec exec(String... command) throws DockerManagerException {
        return new DockerExecImpl(framework, dockerManager, this, 10000, command);
    }

    /**
     *  Submit exec commands to be executed on the docker container, with a custom timeout.
     * 
     * @param timeout
     * @param String... commands
     * @return IDockerExec
     */
    @Override
    public IDockerExec exec(int timeout, String... command) throws DockerManagerException {
        return new DockerExecImpl(framework, dockerManager, this, timeout, command);
    }

    /**
     * Retrieves any stdOut from the container
     * 
     * @throws DockerManaerException
     */
    @Override
    public String retrieveStdOut() throws DockerManagerException {
        return dockerServer.getLog("/containers/" + containerID + "/logs?stdout=true&timestamps=true");
    }

    /**
     * Retrieves any stdErr from the container
     * 
     * @throws DockerManaerException
     */
    @Override
    public String retrieveStdErr() throws DockerManagerException {
        return dockerServer.getLog("/containers/" + containerID + "/logs?stderr=true&timestamps=true");
    }

    /**
     * Polls the docker server for information about a container to see if running.
     * 
     * @return boolean isRunning?
     * @throws DockerManagerException
     */
    @Override
    public boolean isRunning() throws DockerManagerException {
        JsonObject response = dockerServer.getContainer(containerName);
		JsonObject state = response.get("State").getAsJsonObject();
		if (state == null) {
			return false;
		}
		Boolean running = state.get("Running").getAsBoolean();

		return running;
    }

    /**
     * Retrieves the exitCode from container 
     * 
     * @return exitCode
     * @throws DockerManagerException
     */
    @Override
    public long getExitCode() throws DockerManagerException {
        JsonObject response = dockerServer.getContainer(containerName);
		JsonObject state = response.get("State").getAsJsonObject();
		if (state == null) {
			return -1;
		}
		Long exitCode = state.get("ExitCode").getAsLong();
	
		return exitCode;
    }

    /**
     * Retrieves the docker slot that this container is using.
     * @return DockerSlotImpl
     */
	public DockerSlotImpl getDockerSlot() {
		return this.dockerSlot;
	}
    
    /**
     * Queries the DSS for the run name to construct and standarised name for the container
     * 
     * E.g 'GALASA_U12_database'
     * @param dockerSlot2
     * @return String - name of container 
     * @throws DynamicStatusStoreException
     */
    private String getContainerName(DockerSlotImpl dockerSlot2) throws DynamicStatusStoreException {
        String slotName = dockerSlot.getSlotName();
        String runName = dss.get("server." + dockerServer.getHost() + ".slot." + slotName);
        //  E.g 'GALASA_U12_ExampleContainerName'
        return CONTAINER_NAME_PREFIX + runName + "_" + this.tag;
    }

    /**
     * Check to see if a flag was set to leave the container running post test.
     * 
     * @throws DynamicStatusStoreException
     */
    private void checkLeaveRunning() throws DynamicStatusStoreException {
        String flag = dss.get("container."+tag+".leave.running");
			if (flag != null) {
                logger.debug("Requested leaveRunning state: " + flag);
			    leaveRunning = Boolean.parseBoolean(flag);
			} else {
                logger.debug("No state requested, setting leaveRunning to false");
			    leaveRunning = false;
			}
    }

    /**
     * Used by the checkContainer() to collect the state from the docker server. 
     * 
     * If the container is not as expected then it is attempted to be resolved.
     * 
     * @throws DockerManagerException
     */
    private void checkContainerState() throws DockerManagerException {
        JsonObject response = dockerServer.getContainer(containerName);

			if (response != null) {
				logger.debug("Docker Container '" + this.tag + "' is already defined");
				alreadyDefined = true;
				containerID = response.get("Id").getAsString();

				JsonObject state = (JsonObject) response.get("State");
				alreadyUp = state.get("Running").getAsBoolean();	
				if (alreadyUp) {
					logger.debug("Docker Container '" + this.tag + "' is already running");
				}

				if (!leaveRunning && (alreadyUp || alreadyDefined)) {
					logger.debug("Tidying up the Docker Container as leave.running is not true");
					if (alreadyUp) {
						killContainer();
					}
					if (alreadyDefined) {
						deleteContainer();
					}
					alreadyUp      = false;
					alreadyDefined = false;
				} 

				if (alreadyDefined) {
					JsonObject config = response.get("Config").getAsJsonObject();
					String imageName = config.get("Image").getAsString();
					this.image.setFullName(imageName); 
				}
			} else {
                logger.debug("No response, not defined or running");
				alreadyDefined = false;
				alreadyUp = false;
			}
    }

    /**
     * Deletes the container from the docker server
     * 
     * @throws DockerManagerException
     */
    private void deleteContainer() throws DockerManagerException {
        logger.debug("Deleting Docker Container '" + tag + "'");
		dockerServer.deleteContainer(containerID);
		logger.info("Deleted Docker Container '" + tag + "'");
    }

    /**
     * Kills the running container on the docker server
     * 
     * @throws DockerManagerException
     */
    private void killContainer() throws DockerManagerException {
        logger.debug("Killing Docker Container '" + tag + "'");
	    dockerServer.killContainer(containerID);
	    logger.info("Killed Docker Container '" + tag + "'");
    }

    /**
     * Retrieves the dockerServer the container is hosted on.
     * 
     * @return dockerServer
     */
    public DockerServerImpl getDockerServerImpl() {
        return dockerServer;
	}

    /**
     * Retrieves the docker running ID
     */
	public String getContainerId() {
		return containerID;
	}

    /**
     * Checks to see if this container should be left running.
     * 
     * If not, then cleans up both the container and the slot.
     * @throws DockerManagerException
     */
	public void discard() throws DockerManagerException {
        if (leaveRunning) {
            return;
        }

        try {
            deleteContainer();
        } catch(DockerManagerException e) {
            throw new DockerManagerException("Unable to stop container: " + tag, e);
        }

        try{
            dockerSlot.free();
        } catch (Exception e) {
            logger.warn("Unable to free slot");
        }
	} 
}