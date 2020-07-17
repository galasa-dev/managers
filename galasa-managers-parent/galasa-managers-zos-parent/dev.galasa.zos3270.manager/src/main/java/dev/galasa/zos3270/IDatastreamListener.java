package dev.galasa.zos3270;

public interface IDatastreamListener {
    
    enum DatastreamDirection {
        INBOUND,   // server to client
        OUTBOUND   // client to server
    }
    
    void datastreamUpdate(DatastreamDirection direction, String datastreamHex);

}
