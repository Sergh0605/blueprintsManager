package com.dataart.blueprintsmanager.pdf;

import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;

public class CustomDocumentHandler implements IEventHandler {
    int countOfNewPages;
    public CustomDocumentHandler() {
    }

    @Override
    public void handleEvent(Event event) {
        countOfNewPages++;

    }

    public int getCountOfNewPages() {
        return countOfNewPages;
    }
}
