package dev.galasa.kubernetes;

import java.net.InetSocketAddress;

public interface IService extends IResource {

    InetSocketAddress getSocketAddressForPort(int port) throws KubernetesManagerException;

}
