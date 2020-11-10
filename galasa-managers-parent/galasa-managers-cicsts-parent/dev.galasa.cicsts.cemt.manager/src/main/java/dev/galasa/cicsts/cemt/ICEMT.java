package dev.galasa.cicsts.cemt;

import java.util.HashMap;

import javax.validation.constraints.NotNull;

import dev.galasa.zos3270.ITerminal;

public interface ICEMT {

   
   public HashMap<String, String> inquireResource(@NotNull ITerminal cemtTerminal,
                                                  @NotNull String resourceType,
                                                  @NotNull String resourceName
                                                  ) throws CEMTException;
   
   
   public void setResource(@NotNull ITerminal cemtTerminal,
                           @NotNull String resourceType,
                                    String resourceName,
                           @NotNull String action,
                           @NotNull String searchText
                           )throws CEMTException;
   
   public void setResource(@NotNull ITerminal cemtTerminal,
                           @NotNull String resourceType,
                           @NotNull String resourceName,
                           @NotNull String action
                           )throws CEMTException;
   
   public void discardResource(@NotNull ITerminal cemtTerminal,
                              @NotNull String resourceType,
                              @NotNull String resourceName,
                              @NotNull String searchText)throws CEMTException;
   
   public boolean performSystemProperty(@NotNull ITerminal cemtTerminal,
                                        @NotNull String systemArea,
                                        @NotNull String setRequest,
                                        @NotNull String expectedResponse)throws CEMTException;
}
