/*
 * Copyright (c) 2019 IBM Corporation.
 */
package dev.galasa.zos3270.spi;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import dev.galasa.ResultArchiveStoreContentType;
import dev.galasa.SetContentType;
import dev.galasa.framework.spi.IConfidentialTextService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.zos3270.AttentionIdentification;
import dev.galasa.zos3270.IScreenUpdateListener;
import dev.galasa.zos3270.Zos3270ManagerException;
import dev.galasa.zos3270.common.screens.FieldContents;
import dev.galasa.zos3270.common.screens.TerminalField;
import dev.galasa.zos3270.common.screens.TerminalImage;
import dev.galasa.zos3270.common.screens.TerminalSize;
import dev.galasa.zos3270.internal.properties.ApplyConfidentialTextFiltering;
import dev.galasa.zos3270.internal.properties.LiveTerminalDirectory;

public class Zos3270TerminalImpl extends Terminal implements IScreenUpdateListener {

    private Log                            logger       = LogFactory.getLog(getClass());

    private final Gson                     gson         = new GsonBuilder().setPrettyPrinting().create();

    private final String                   terminalId;
    private int                            updateId;

    private final IConfidentialTextService cts;
    private final boolean                  applyCtf;

    private final ArrayList<TerminalImage> cachedImages = new ArrayList<>();

    private final Path                     terminalRasDirectory;
    private int                            rasTerminalSequence;
    private final Path                     liveTerminalDirectory;
    private int                            liveTerminalSequence;

    public Zos3270TerminalImpl(String id, String host, int port, boolean tls, IFramework framework)
            throws Zos3270ManagerException, InterruptedException {
        super(host, port, tls);
        this.terminalId = id;

        this.cts = framework.getConfidentialTextService();
        this.applyCtf = ApplyConfidentialTextFiltering.get();

        getScreen().registerScreenUpdateListener(this);

        Path storedArtifactsRoot = framework.getResultArchiveStore().getStoredArtifactsRoot();
        terminalRasDirectory = storedArtifactsRoot.resolve("zos3270").resolve("terminals").resolve(this.terminalId);

        String propLiveTerminalDirectory = LiveTerminalDirectory.get();
        if (propLiveTerminalDirectory == null) {
            liveTerminalDirectory = null;
        } else {
            try {
                Path termDir = Paths.get(propLiveTerminalDirectory, framework.getTestRunName(), this.terminalId);
                while(Files.exists(termDir)) {
                    termDir = termDir.getParent().resolve(termDir.getFileName() + "A");
                }

                Files.createDirectories(termDir);
                liveTerminalDirectory = termDir;
            } catch(Exception e) {
                throw new Zos3270ManagerException("Unable to create the live terminal directory", e);
            }
        }
    }

    @Override
    public synchronized void screenUpdated(Direction direction, AttentionIdentification aid) {
        updateId++;
        String update = terminalId + "-" + (updateId);

        String screenData = getScreen().printScreenTextWithCursor();
        if (applyCtf) {
            screenData = cts.removeConfidentialText(screenData);
        }

        String aidString;
        String aidText = null;
        if (aid != null) {
            aidString = ", " + aid.toString();
            aidText = aid.toString();
        } else {
            aidString = " update";
        }

        TerminalSize terminalSize = new TerminalSize(getScreen().getNoOfColumns(), getScreen().getNoOfRows()); // TODO
        // sort
        // out
        // alt
        // sizes
        TerminalImage terminalImage = new TerminalImage(updateId, update, direction == Direction.RECEIVED, null,
                aidText, terminalSize);
        terminalImage.getFields().addAll(buildTerminalFields(getScreen()));
        cachedImages.add(terminalImage);
        if (cachedImages.size() >= 10) {
            flushTerminalCache();
        }

        if (liveTerminalDirectory != null) {
            try {
                liveTerminalSequence++;
                String terminalFilename = String.format("%05d", liveTerminalSequence) + ".gz";
                Path terminalPath = liveTerminalDirectory.resolve(terminalFilename);

                dev.galasa.zos3270.common.screens.Terminal liveTerminal = new dev.galasa.zos3270.common.screens.Terminal(
                        this.terminalId, liveTerminalSequence, terminalSize);
                liveTerminal.getImages().add(terminalImage);

                JsonObject intermediateJson = (JsonObject) gson.toJsonTree(liveTerminal);
                stripFalseBooleans(intermediateJson);
                String tempJson = gson.toJson(intermediateJson);

                if (applyCtf) {
                    tempJson = cts.removeConfidentialText(tempJson);
                }

                try (GZIPOutputStream gos = new GZIPOutputStream(Files.newOutputStream(terminalPath, StandardOpenOption.CREATE))) {
                    IOUtils.write(tempJson, gos, StandardCharsets.UTF_8);
                }
            } catch(Exception e) {
                logger.error("Failed to write live terminal image, image lost",e);
            }
        }

        logger.debug(direction.toString() + aidString + " to 3270 terminal " + this.terminalId + ",  updateId=" + update
                + "\n" + screenData);
    }

    public synchronized void flushTerminalCache() {
        if (cachedImages.isEmpty()) {
            return;
        }

        rasTerminalSequence++;

        try {
            TerminalSize terminalSize = new TerminalSize(getScreen().getNoOfColumns(), getScreen().getNoOfRows()); // TODO
            // sort
            // out
            // alt
            // sizes
            dev.galasa.zos3270.common.screens.Terminal rasTerminal = new dev.galasa.zos3270.common.screens.Terminal(
                    this.terminalId, rasTerminalSequence, terminalSize);
            rasTerminal.getImages().addAll(this.cachedImages);

            JsonObject intermediateJson = (JsonObject) gson.toJsonTree(rasTerminal);
            stripFalseBooleans(intermediateJson);
            String tempJson = gson.toJson(intermediateJson);

            if (applyCtf) {
                tempJson = cts.removeConfidentialText(tempJson);
            }

            String terminalFilename = this.terminalId + "-" + String.format("%05d", rasTerminalSequence) + ".gz";
            Path terminalPath = terminalRasDirectory.resolve(terminalFilename);

            try (GZIPOutputStream gos = new GZIPOutputStream(Files.newOutputStream(terminalPath, new SetContentType(new ResultArchiveStoreContentType("application/zos3270terminal")), 
                    StandardOpenOption.CREATE))) {
                IOUtils.write(tempJson, gos, "utf-8");
            }
        } catch(Exception e) {
            logger.error("Unable to write terminal cache to the RAS",e);
            rasTerminalSequence--;
            return;
        }

        this.cachedImages.clear();
    }

    private static List<TerminalField> buildTerminalFields(Screen screen) {
        ArrayList<TerminalField> terminalFields = new ArrayList<>();

        Field[] screenFields = screen.calculateFields();
        for (Field screenField : screenFields) {
            int row = screenField.getStart() / screen.getNoOfColumns();
            int column = screenField.getStart() % screen.getNoOfColumns();

            TerminalField terminalField = new TerminalField(row, column, screenField.isUnformatted(),
                    screenField.isProtected(), screenField.isNumeric(), screenField.isDisplay(),
                    screenField.isIntenseDisplay(), screenField.isSelectorPen(), screenField.isFieldModifed());

            Character[] chars = screenField.getFieldCharsWithNulls();
            terminalField.getContents().add(new FieldContents(chars)); // TODO needs to be expanded when we record
            // extended attributes
            terminalFields.add(terminalField);
        }

        return terminalFields;
    }

    public String getId() {
        return this.terminalId;
    }

    public static void stripFalseBooleans(JsonObject json) {

        ArrayList<Entry<String, JsonElement>> entries = new ArrayList<>();
        entries.addAll(json.entrySet());

        for (Entry<String, JsonElement> entry : entries) {
            JsonElement element = entry.getValue();

            if (element.isJsonPrimitive() && ((JsonPrimitive) element).isBoolean()
                    && !((JsonPrimitive) element).getAsBoolean()) {
                json.remove(entry.getKey());
            } else if (element.isJsonObject()) {
                stripFalseBooleans((JsonObject) element);
            } else if (element.isJsonArray()) {
                JsonArray array = (JsonArray) element;
                for (int i = 0; i < array.size(); i++) {
                    if (array.get(i).isJsonObject()) {
                        stripFalseBooleans((JsonObject) array.get(i));
                    }
                }
            }
        }
    }

}
