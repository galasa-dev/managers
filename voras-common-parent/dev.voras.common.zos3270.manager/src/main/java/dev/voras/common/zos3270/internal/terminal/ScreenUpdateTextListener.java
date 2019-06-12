package dev.voras.common.zos3270.internal.terminal;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import dev.voras.common.zos3270.IScreenUpdateListener;
import dev.voras.common.zos3270.TextNotFoundException;
import dev.voras.common.zos3270.Zos3270Exception;
import dev.voras.common.zos3270.spi.Screen;

public class ScreenUpdateTextListener implements IScreenUpdateListener {

    private final Screen screen;
    private final String searchText;
    private Semaphore textFound = new Semaphore(1, true);

    public ScreenUpdateTextListener(Screen screen, String searchText) {
        this.screen = screen;
        this.searchText = searchText;
        try {
            this.textFound.acquire();
        } catch (InterruptedException e) {//NOSONAR
            throw new UnsupportedOperationException("Shouldn't happen",e); 
        }

        screenUpdated();
        if (this.textFound.availablePermits() > 0) {
            return;
        }
        this.screen.registerScreenUpdateListener(this);
    }

    @Override
    public void screenUpdated() {
        try {
            screen.searchFieldContaining(searchText);
            this.textFound.release();            
        } catch(TextNotFoundException e) { //NOSONAR
            // IGNORE
        }

    }



    public boolean waitForText(long maxWait) throws InterruptedException, Zos3270Exception {
        if (this.textFound == null) {
            throw new Zos3270Exception("Not allowed to use this listener more than once");
        }
        boolean found = this.textFound.tryAcquire(1, maxWait, TimeUnit.MILLISECONDS);
        this.textFound = null;
        screen.unregisterScreenUpdateListener(this);        

        return found;
    }

    public static boolean waitForText(Screen screen, String searchText, int maxWait) throws InterruptedException, Zos3270Exception {
        return new ScreenUpdateTextListener(screen, searchText).waitForText(maxWait);
    }

}
