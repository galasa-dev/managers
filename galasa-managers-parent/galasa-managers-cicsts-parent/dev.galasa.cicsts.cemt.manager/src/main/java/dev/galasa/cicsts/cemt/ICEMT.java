package dev.galasa.cicsts.cemt;

import java.util.HashMap;

import javax.validation.constraints.NotNull;

import dev.galasa.zos3270.ITerminal;

public interface ICEMT {

   /** 
    * Inquire a CEMT resource using the resource type and name.
    * This does not support inquiries of multiple resources at once. 
    * Will return {@link null} if the resource is not found.
    * @param cemtTerminal an {@link ITerminal} object logged on to the CICS region and in an active CEMT session.
    * If mixed case is required, the terminal should be presented with no upper case translate status. 
    * For example, the test could first issue <code>CEOT TRANIDONLY</code>
    * @param resourceType a {@link String} of the resource type you are looking for.
    * @param resourceName a {@link String} of the name of the resource you are looking for.
    * @return a {@link HashMap} object containing all of the properties of the resource.
    * @throws CEMTException if resource not found.
    */
   public HashMap<String, String> inquireResource(@NotNull ITerminal cemtTerminal,
                                                  @NotNull String resourceType,
                                                  @NotNull String resourceName
                                                  ) throws CEMTException;
   
   /** 
    * Set the state of a CEMT resource using the resource type and name. If the searchText is not found on the terminal screen then
    * an exception will be thrown.
    * @param cemtTerminal an {@link ITerminal} object logged on to the CICS region and in an active CEMT session.
    * If mixed case is required, the terminal should be presented with no upper case translate status. 
    * For example, the test could first issue <code>CEOT TRANIDONLY</code>
    * @param resourceType a {@link String} of the type of resource you want to set.
    * @param resourceName a {@link String} of the name of the resource you want to set.
    * @param action a {@link String} of the action you want to perform.
    * @param searchText a {@link String} of the text you want to search for on the terminal screen.
    * @throws CEMTException
    */
   public void setResource(@NotNull ITerminal cemtTerminal,
                           @NotNull String resourceType,
                                    String resourceName,
                           @NotNull String action,
                           @NotNull String searchText
                           )throws CEMTException;
   
   
   /** 
    * Set the state of a CEMT resource using the resource type and name. If 'RESPONSE: NORMAL' is not found on the terminal screen then
    * an exception will be thrown.
    * @param cemtTerminal an {@link ITerminal} object logged on to the CICS region and in an active CEMT session.
    * If mixed case is required, the terminal should be presented with no upper case translate status. 
    * For example, the test could first issue <code>CEOT TRANIDONLY</code>
    * @param resourceType a {@link String} of the type of resource you want to set.
    * @param resourceName a {@link String} of the name of the resource you want to set.
    * @param action a {@link String} of the action you want to perform.
    * @throws CEMTException
    */
   public void setResource(@NotNull ITerminal cemtTerminal,
                           @NotNull String resourceType,
                           @NotNull String resourceName,
                           @NotNull String action
                           )throws CEMTException;
   
   
   /**
    * Discards a specified resource and throws an exception if the specified search text is not found on the terminal.
    * @param cemtTerminal an {@link ITerminal} object logged on to the CICS region and in an active CEMT session.
    * If mixed case is required, the terminal should be presented with no upper case translate status. 
    * For example, the test could first issue <code>CEOT TRANIDONLY</code>
    * @param resourceType a {@link String} of the type of resource you want to discard.
    * @param resourceName a {@link String} of the name of the resource you want to discard.
    * @param searchText a {@link String} of the text you want to search for on the terminal screen.
    * @throws CEMTException
    */
   public void discardResource(@NotNull ITerminal cemtTerminal,
                              @NotNull String resourceType,
                              @NotNull String resourceName,
                              @NotNull String searchText)throws CEMTException;
   
   
   /**
    * 
    * @param cemtTerminal an {@link ITerminal} object logged on to the CICS region and in an active CEMT session.
    * If mixed case is required, the terminal should be presented with no upper case translate status. 
    * For example, the test could first issue <code>CEOT TRANIDONLY</code>
    * @param systemArea a {@link String} the specifies the system area.
    * @param setRequest
    * @param expectedResponse
    * @return boolean
    * @throws CEMTException
    */
   public boolean performSystemProperty(@NotNull ITerminal cemtTerminal,
                                        @NotNull String systemArea,
                                        @NotNull String setRequest,
                                        @NotNull String expectedResponse)throws CEMTException;
}
