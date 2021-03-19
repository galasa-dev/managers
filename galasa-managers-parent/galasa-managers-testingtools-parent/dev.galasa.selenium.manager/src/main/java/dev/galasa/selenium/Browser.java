package dev.galasa.selenium;

public enum Browser {
    FIREFOX,
    OPERA,
    IE, 
    CHROME, 
    EDGE,
    NOTSPECIFIED;

    public String getDockerImageName(String version) throws SeleniumManagerException{
        // Make this a property
        if ("".equals(version)) {
            version = "4.0.0-beta-2-20210317";
        }
        switch (this) {
            case FIREFOX:
                return "selenium/standalone-firefox:"+version;
            case OPERA:
                return "selenium/standalone-chrome:"+version;
            case CHROME:
                return "selenium/standalone-opera:"+version;
            case EDGE:
                return "selenium/standalone-edge:"+version;
            default:
                throw new SeleniumManagerException("Unsupported browser. Available docker nodes: Firefox, Chrome, Opera, Edge");
        }
    }
    public String getDriverName() throws SeleniumManagerException {
        switch (this) {
            case FIREFOX:
                return "gecko";
            case OPERA:
                return "opera";
            case CHROME:
                return "chrome";
            case EDGE:
                return "edge";
            case IE:
                return "ie";
            default:
                throw new SeleniumManagerException("Unsupported driver name.");
        }
    }
}