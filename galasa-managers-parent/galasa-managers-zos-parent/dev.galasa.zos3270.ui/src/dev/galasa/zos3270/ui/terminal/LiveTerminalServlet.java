/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zos3270.ui.terminal;

import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import dev.galasa.zos3270.common.screens.Terminal;
import dev.galasa.zos3270.ui.Zos3270Activator;

public class LiveTerminalServlet extends HttpServlet {
    
    private TerminalView terminalView;

    private Gson gson = new Gson();

    public void register(TerminalView terminalView) {
        this.terminalView = terminalView;
    }
    
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (terminalView == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Terminal view closed");
            return;
        }
        
        try {
            Terminal terminal = gson.fromJson(new InputStreamReader(req.getInputStream()), Terminal.class);
            
            this.terminalView.addLiveTerminal(terminal);
            
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch(Exception e) {
            Zos3270Activator.log(e);
            throw new IOException("Unable to read live terminal",e);
        }
        
    }
    
    public void dispose() {
        this.terminalView = null;
    }
    
}
