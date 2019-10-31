/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zos3270.ui.terminal;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.google.gson.Gson;

import dev.galasa.eclipse.Activator;
import dev.galasa.zos3270.common.screens.FieldContents;
import dev.galasa.zos3270.common.screens.Terminal;
import dev.galasa.zos3270.common.screens.TerminalField;
import dev.galasa.zos3270.common.screens.TerminalImage;
import dev.galasa.zos3270.common.screens.TerminalSize;
import dev.galasa.zos3270.ui.Zos3270Activator;
import dev.galasa.zos3270.ui.preferences.PreferenceConstants;

public class TerminalView extends ViewPart implements PaintListener, IPropertyChangeListener {

    public static final String  ID                   = "dev.galasa.zos3270.ui.terminal.TerminalView";

    private Shell               shell;
    private Canvas              canvas;
    private Action              firstFrameAction;
    private Action              prevFrameAction;
    private Action              nextFrameAction;
    private Action              lastFrameAction;
    private Action              suspendLast;

    private Color               colourBackground;
    private Color               colourNormal;
    private Color               colourIntense;

    private Font                fontText;

    private boolean             loading              = true;

    private Path                cachePath;

    private ArrayList<Images>   imageFiles           = new ArrayList<>();
    private ArrayList<Image>    images               = new ArrayList<>();

    private final Gson          gson                 = new Gson();

    private int                 currentImageSequence = 0;

    private boolean             disposed             = false;
    private LiveTerminalServlet liveServlet;
    private boolean             suspend              = false;

    private String              viewId;

    @Override
    public void createPartControl(Composite parent) {
        this.cachePath = Zos3270Activator.getDefault().getLiveTerminalsPath().resolve(UUID.randomUUID().toString());
        try {
            Files.createDirectories(cachePath);
        } catch (IOException e) {
            Zos3270Activator.log(e);
        }

        this.shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

        this.canvas = new Canvas(parent, SWT.NULL);

        loadColourPreferences();
        loadFontPreferences();

        canvas.addPaintListener(this);

        Zos3270Activator.getDefault().getPreferenceStore().addPropertyChangeListener(this);

        createActions();
        createToolbar();
    }

    private void createActions() {
        firstFrameAction = new Action("First Screen") {
            public void run() {
                moveToImage(0);
            };
        };

        firstFrameAction.setImageDescriptor(getImageDescriptor("nav_first.gif"));

        prevFrameAction = new Action("Previous Screen") {
            public void run() {
                moveToImage(currentImageSequence - 1);
            };
        };

        prevFrameAction.setImageDescriptor(getImageDescriptor("nav_previous.gif"));

        suspendLast = new Action("Scroll lock", SWT.PUSH) {
            public void run() {
                suspend = !suspend;
                suspendLast.setChecked(suspend);
            };

        };

        suspendLast.setImageDescriptor(getImageDescriptor("nav_suspend.gif"));

        nextFrameAction = new Action("Next Screen") {
            public void run() {
                moveToImage(currentImageSequence + 1);
            }

        };

        nextFrameAction.setImageDescriptor(getImageDescriptor("nav_next.gif"));

        lastFrameAction = new Action("Last Screen") {
            public void run() {
                moveToImage(images.size() - 1);
            };
        };

        lastFrameAction.setImageDescriptor(getImageDescriptor("nav_last.gif"));
    }

    private void createToolbar() {
        IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();

        mgr.add(firstFrameAction);
        mgr.add(prevFrameAction);
        mgr.add(suspendLast);
        mgr.add(nextFrameAction);
        mgr.add(lastFrameAction);
    }

    private void moveToImage(int newSequence) {
        this.currentImageSequence = newSequence;
        if (this.currentImageSequence < 0) {
            this.currentImageSequence = 0;
        }
        if (this.currentImageSequence > this.images.size()) {
            this.currentImageSequence = this.images.size() - 1;
        }

        updateUI();
        new CheckCacheJob().schedule();
    };

    protected void validateActions() {

        // validateGotoIndex();
        if (loading) {
            firstFrameAction.setEnabled(false);
            prevFrameAction.setEnabled(false);
            nextFrameAction.setEnabled(false);
            lastFrameAction.setEnabled(false);
            return;
        }

        if (this.currentImageSequence > 0) {
            firstFrameAction.setEnabled(true);
            prevFrameAction.setEnabled(true);
        } else {
            firstFrameAction.setEnabled(false);
            prevFrameAction.setEnabled(false);
        }

        if (this.currentImageSequence < (this.images.size() - 1)) {
            nextFrameAction.setEnabled(true);
            lastFrameAction.setEnabled(true);
        } else {
            nextFrameAction.setEnabled(false);
            lastFrameAction.setEnabled(false);
        }
    }

    @Override
    public void paintControl(PaintEvent event) {
        if (loading) {
            displayMessage(event, "Loading...");
            return;
        }

        Image image = this.images.get(currentImageSequence);
        if (image == null || image.getTerminalImage() == null) {
            displayMessage(event, "Image " + (currentImageSequence + 1) + " is missing");
            return;
        }

        displayImage(event, image);
    }

    private void displayImage(PaintEvent event, Image image) {
        Rectangle clientArea = this.canvas.getClientArea();

        event.gc.setBackground(colourBackground);
        event.gc.fillRectangle(clientArea);
        event.gc.setFont(fontText);
        FontMetrics fontMetrics = event.gc.getFontMetrics();

        int charWidth = fontMetrics.getAverageCharWidth();
        int charHeight = fontMetrics.getHeight();

        TerminalImage terminalImage = image.getTerminalImage();
        List<TerminalField> fields = terminalImage.getFields();
        TerminalSize size = image.getSize();
        int cols = size.getColumns();
        int rows = size.getRows();
        for (TerminalField field : fields) {
            int col = field.getColumn();
            int row = field.getRow();

            if (field.isFieldIntenseDisplay()) {
                event.gc.setForeground(colourIntense);
            } else {
                event.gc.setForeground(colourNormal);
            }

            for (FieldContents content : field.getContents()) {
                for (Character c : content.getChars()) {
                    if (c != null) {
                        int x = charWidth * col;
                        int y = charHeight * row;
                        event.gc.drawText(Character.toString(c), x, y, true);
                    }

                    col++;
                    if (col >= cols) {
                        col = 0;
                        row++;
                        if (row >= rows) {
                            row = 0;
                        }
                    }
                }
            }
        }

        int oiaYLine = rows * charHeight;
        int oiaXLine = (cols + 1) * charWidth;
        int oiaYText = oiaYLine + 5;

        event.gc.setForeground(colourNormal);
        event.gc.drawLine(0, oiaYLine, oiaXLine, oiaYLine);

        int pos = this.currentImageSequence + 1;
        if (this.images.isEmpty()) {
            pos = 0;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Screen ");
        sb.append(Integer.toString(pos));
        sb.append("/");
        sb.append(Integer.toString(this.images.size()));
        sb.append(" - ");

        if (terminalImage.getId() != null) {
            sb.append(terminalImage.getId());
            sb.append(" - ");
        }

        sb.append(Integer.toString(cols));
        sb.append("x");
        sb.append(Integer.toString(rows));
        sb.append(" - ");

        if (terminalImage.isInbound()) {
            sb.append("Inbound ");
        } else {
            sb.append("Outbound - ");
            sb.append(terminalImage.getAid());
        }

        event.gc.drawText(sb.toString(), 0, oiaYText, true);
    }

    private void displayMessage(PaintEvent event, String message) {
        Rectangle clientArea = this.canvas.getClientArea();

        event.gc.setBackground(colourBackground);
        event.gc.fillRectangle(clientArea);

        event.gc.setForeground(colourIntense);

        event.gc.setFont(fontText);

        event.gc.drawText(message, 10, 10);
    }

    private void loadImagePaths(ArrayList<Path> imagePaths) {
        new LoadImagesJob(this, this.cachePath, imagePaths).schedule();
    }

    @Override
    public void dispose() {
        this.disposed = true;

        Zos3270Activator.getDefault().getPreferenceStore().removePropertyChangeListener(this);

        if (this.viewId == null) {
            this.viewId = "unknown";
        }

        if (this.liveServlet != null) {
            try {
                this.liveServlet.dispose();
                Activator.getLiveUpdateServer().unregisterServlet(liveServlet);
            } catch (Exception e) {
                Zos3270Activator.log(e);
            }
        }

        if (this.cachePath != null) {
            new DeleteTerminalCache(this.viewId, this.cachePath).schedule();
        }

        super.dispose();
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        Display.getDefault().syncExec(new Runnable() {

            @Override
            public void run() {
                loadColourPreferences();
                TerminalView.this.canvas.redraw();
            }
        });
    }

    @Override
    public void setFocus() {
        this.canvas.setFocus();
    }

    private void loadFontPreferences() {

        Display display = this.shell.getDisplay();

        fontText = new Font(display, "Courier New", 12, SWT.NULL);
    }

    private void loadColourPreferences() {
        IPreferenceStore preferenceStore = Zos3270Activator.getDefault().getPreferenceStore();

        Display display = this.shell.getDisplay();

        RGB rgbBackground = convertRGB(preferenceStore.getString(PreferenceConstants.P_BACKGROUND_COLOUR));
        RGB rgbNormal = convertRGB(preferenceStore.getString(PreferenceConstants.P_NORMAL_COLOUR));
        RGB rgbIntense = convertRGB(preferenceStore.getString(PreferenceConstants.P_INTENSE_COLOUR));

        colourBackground = new Color(display, rgbBackground);
        colourNormal = new Color(display, rgbNormal);
        colourIntense = new Color(display, rgbIntense);

        canvas.setBackground(colourBackground);
    }

    private RGB convertRGB(String value) {
        int red = 255;
        int green = 255;
        int blue = 255;

        String[] split = value.split(",");
        if (split.length >= 1) {
            try {
                red = Integer.parseInt(split[0].trim());
            } catch (NumberFormatException e) {
                Zos3270Activator.log(e);
            }
        }
        if (split.length >= 2) {
            try {
                green = Integer.parseInt(split[1].trim());
            } catch (NumberFormatException e) {
                Zos3270Activator.log(e);
            }
        }
        if (split.length >= 3) {
            try {
                blue = Integer.parseInt(split[2].trim());
            } catch (NumberFormatException e) {
                Zos3270Activator.log(e);
            }
        }

        return new RGB(red, green, blue);
    }

    public static void openTerminal(String runId, String terminalId, ArrayList<Path> imagePaths) {
        Display.getDefault().syncExec(new Runnable() {

            @Override
            public void run() {
                try {
                    TerminalView view = (TerminalView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                            .getActivePage()
                            .showView(TerminalView.ID, UUID.randomUUID().toString(), IWorkbenchPage.VIEW_ACTIVATE);

                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().activate(view);
                    view.setRunTerminalIds(runId, terminalId);
                    view.loadImagePaths(imagePaths);
                } catch (PartInitException e) {
                    Zos3270Activator.log(e);
                }
            }
        });
    }

    public static void openLiveTerminal(String terminalId, String runId, LiveTerminalServlet servlet) {
        Display.getDefault().syncExec(new Runnable() {

            @Override
            public void run() {
                try {
                    TerminalView view = (TerminalView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                            .getActivePage()
                            .showView(TerminalView.ID, UUID.randomUUID().toString(), IWorkbenchPage.VIEW_ACTIVATE);

                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().activate(view);
                    view.setLiveTerminal(servlet);
                } catch (PartInitException e) {
                    Zos3270Activator.log(e);
                }
            }
        });
    }

    protected void setLiveTerminal(LiveTerminalServlet servlet) {
        this.liveServlet = servlet;
        this.liveServlet.register(this);
    }

    protected void setRunTerminalIds(String runId, String terminalId) {
        this.viewId = runId + " - " + terminalId;
        setPartName(this.viewId);
    }

    public synchronized void addTerminalImageFile(Path path, Terminal terminal) {
        if (terminal == null) {
            try (Reader reader = new InputStreamReader(new GZIPInputStream(Files.newInputStream(path)))) {
                terminal = gson.fromJson(reader, Terminal.class);
            } catch (Exception e) {
                Zos3270Activator.log(e);
                return;
            }
        }
        int terminalSequence = (int) terminal.getSequence();

        if (this.viewId == null) {
            setRunTerminalIds(terminal.getRunId(), terminal.getId());
        }

        if (terminalSequence == 0) { // Not a valid file, so ignore
            return;
        }
        terminalSequence--; // make zero based

        expandArray(this.imageFiles, terminalSequence); // Expand the array to ensure the image is placed in the
        // correct sequence

        if (this.imageFiles.get(terminalSequence) != null) { // Do we have this sequence already
            return;
        }

        Images images = new Images(path, terminal);
        this.imageFiles.set(terminalSequence, images);

        for (TerminalImage tImage : terminal.getImages()) {
            int imageSequence = (int) tImage.getSequence();
            if (imageSequence == 0) { // Is it valid
                continue;
            }
            imageSequence--; // make zero based
            expandArray(this.images, imageSequence);
            if (this.images.get(imageSequence) != null) { // Already have this sequence
                continue;
            }

            Image image = new Image(tImage, images);
            this.images.set(imageSequence, image);
        }
    }

    private void expandArray(ArrayList<?> array, long sequence) {
        while (array.size() <= sequence) {
            array.add(null);
        }
    }

    public void updateUI() {
        Display.getDefault().syncExec(new Runnable() {

            @Override
            public void run() {
                TerminalView.this.canvas.redraw();
                validateActions();
            }
        });
    }

    /**
     * Ensure the cache is pre-loaded with the current image file also +1 and -1,
     * the rest should be cleared to minimize heap usage
     */
    private synchronized void checkCache() {
        int firstImagesToCache = -1;
        int lastImagesToCache = -1;

        Image currentImage = this.images.get(currentImageSequence);
        if (currentImage != null) {
            firstImagesToCache = currentImage.getImages().getSequence() - 1;
            if (firstImagesToCache < 0) {
                firstImagesToCache = 0;
            }
            lastImagesToCache = firstImagesToCache + 2;
            if (lastImagesToCache >= this.imageFiles.size()) {
                lastImagesToCache = this.imageFiles.size() - 1;
            }
        }

        for (int i = 0; i < imageFiles.size(); i++) {
            if (firstImagesToCache <= i && i <= lastImagesToCache) {
                if (this.imageFiles.get(i).cacheImages(this.currentImageSequence)) {
                    updateUI();
                }
            } else {
                this.imageFiles.get(i).clearCache();
            }
        }
    }

    public void loadComplete() {
        this.loading = false;

        if (liveServlet != null && !suspend) {
            this.currentImageSequence = this.images.size() - 1;
        }

        checkCache();
        updateUI();
    }

    /**
     * Returns the image descriptor with the given relative path.
     */
    private ImageDescriptor getImageDescriptor(String relativePath) {
        String iconPath = "icons/";
        URL url = Zos3270Activator.getDefault().getBundle().getEntry(iconPath + relativePath);
        return ImageDescriptor.createFromURL(url);
    }

    private class CheckCacheJob extends Job {

        public CheckCacheJob() {
            super("Check cache");
            setSystem(true);
        }

        @Override
        protected IStatus run(IProgressMonitor arg0) {
            checkCache();
            return new Status(Status.OK, Zos3270Activator.PLUGIN_ID, "Terminal cache checked");
        }

    }

    public void addLiveTerminal(Terminal terminal) {
        if (disposed) {
            return;
        }

        String terminalFilename = String.format("%05d", terminal.getSequence()) + ".gz";
        Path terminalPath = cachePath.resolve(terminalFilename);

        try (OutputStreamWriter writer = new OutputStreamWriter(
                new GZIPOutputStream(Files.newOutputStream(terminalPath, StandardOpenOption.CREATE)))) {
            gson.toJson(terminal, writer);
        } catch (Exception e) {
            Zos3270Activator.log(e);
        }

        addTerminalImageFile(terminalPath, terminal);
        loadComplete();
    }

}
