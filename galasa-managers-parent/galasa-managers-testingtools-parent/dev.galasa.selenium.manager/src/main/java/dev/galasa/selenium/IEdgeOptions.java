package dev.galasa.selenium;

import java.io.File;
import java.util.List;

import org.openqa.selenium.edge.EdgeOptions;

public interface IEdgeOptions {

    public EdgeOptions getOptions();

    public void setCapability(String key, Object value);

    public void setCapability(String key, String value);

    public void setCapability(String key, Boolean value);
    
}