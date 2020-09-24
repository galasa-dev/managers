package dev.galasa.docker.internal;

/**
 * A simplified JSON parsing object to parse the docker API containers list for names and ID
 */
public class DockerContainerJSON{
    private String Id;
    private String[] Names;

    public void setId(String Id) {
        this.Id = Id;
    }
    public String getId() {
        return this.Id;
    }

    public void setNames(String[] Names) {
        this.Names = Names;
    }
    public String[] getNames() {
        return this.Names;
    }
    
}