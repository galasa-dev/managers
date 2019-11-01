/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zos3270.ui.terminal;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dev.galasa.eclipse.Activator;
import dev.galasa.zos3270.ui.Zos3270Activator;

public class LiveTerminalsServlet extends HttpServlet {

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String runid = req.getHeader("zos3270-runid");
        String terminalid = req.getHeader("zos3270-terminalid");

        if (runid == null || runid.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "zos3270-runid is missing");
            return;
        }

        if (terminalid == null || terminalid.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "zos3270-terminalid is missing");
            return;
        }

        runid = runid.trim();
        terminalid = terminalid.trim();

        LiveTerminalServlet terminalServlet = new LiveTerminalServlet();
        try {
            Activator.getLiveUpdateServer().registerServlet(terminalServlet,
                    "/zos3270/liveterminals/" + runid + "/" + terminalid);

            TerminalView.openLiveTerminal(terminalid, runid, terminalServlet);
        } catch (Exception e) {
            Zos3270Activator.log(e);
        }

    }

}
