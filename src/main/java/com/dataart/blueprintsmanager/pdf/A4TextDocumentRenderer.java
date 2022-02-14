package com.dataart.blueprintsmanager.pdf;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.layout.LayoutArea;
import com.itextpdf.layout.layout.LayoutResult;
import com.itextpdf.layout.layout.RootLayoutArea;
import com.itextpdf.layout.properties.AreaBreakType;
import com.itextpdf.layout.renderer.DocumentRenderer;
import com.itextpdf.layout.renderer.IRenderer;
import com.itextpdf.layout.tagging.LayoutTaggingHelper;

import java.util.HashSet;
import java.util.Set;

public class A4TextDocumentRenderer extends DocumentRenderer {
    public A4TextDocumentRenderer(Document document) {
        super(document);
    }

    public A4TextDocumentRenderer(Document document, boolean immediateFlush) {
        super(document, immediateFlush);
    }

    @Override
    protected LayoutArea updateCurrentArea(LayoutResult overflowResult) {
        this.flushWaitingDrawingElements(false);
        LayoutTaggingHelper taggingHelper = (LayoutTaggingHelper)this.getProperty(108);
        if (taggingHelper != null) {
            taggingHelper.releaseFinishedHints();
        }

        AreaBreak areaBreak = overflowResult != null && overflowResult.getAreaBreak() != null ? overflowResult.getAreaBreak() : null;
        int currentPageNumber = this.currentArea == null ? 0 : this.currentArea.getPageNumber();
        if (areaBreak != null && areaBreak.getType() == AreaBreakType.LAST_PAGE) {
            while(currentPageNumber < this.document.getPdfDocument().getNumberOfPages()) {
                this.possiblyFlushPreviousPage(currentPageNumber);
                ++currentPageNumber;
            }
        } else {
            this.possiblyFlushPreviousPage(currentPageNumber);
            ++currentPageNumber;
        }

        PageSize customPageSize;
        for(customPageSize = areaBreak != null ? areaBreak.getPageSize() : null; this.document.getPdfDocument().getNumberOfPages() >= currentPageNumber && this.document.getPdfDocument().getPage(currentPageNumber).isFlushed(); ++currentPageNumber) {
        }

        PageSize lastPageSize = this.ensureDocumentHasNPages(currentPageNumber, customPageSize);
        if (lastPageSize == null) {
            lastPageSize = new PageSize(this.document.getPdfDocument().getPage(currentPageNumber).getTrimBox());
        }

        return this.currentArea = new RootLayoutArea(currentPageNumber, this.getCurrentPageEffectiveArea(lastPageSize, currentPageNumber));
    }

    private Rectangle getCurrentPageEffectiveArea(PageSize pageSize, int currentPageNumber) {
        float leftMargin = PdfUtils.millimetersToPoints(19.755f);
        float bottomMargin;
        if (currentPageNumber == 1) {
            bottomMargin = PdfUtils.millimetersToPoints(50);
        } else {
            bottomMargin = PdfUtils.millimetersToPoints(25);
        }
        float topMargin = PdfUtils.millimetersToPoints(4.95f);
        float rightMargin = PdfUtils.millimetersToPoints(4.75f);
        return new Rectangle(pageSize.getLeft() + leftMargin, pageSize.getBottom() + bottomMargin, pageSize.getWidth() - leftMargin - rightMargin, pageSize.getHeight() - bottomMargin - topMargin);
    }

    private void possiblyFlushPreviousPage(int currentPageNumber) {
        if (this.immediateFlush && currentPageNumber > 1) {
            this.document.getPdfDocument().getPage(currentPageNumber - 1).flush();
        }

    }

    void flushWaitingDrawingElements(boolean force) {
        Set<IRenderer> flushedElements = new HashSet();

        for(int i = 0; i < this.waitingDrawingElements.size(); ++i) {
            IRenderer waitingDrawingElement = (IRenderer)this.waitingDrawingElements.get(i);
            if (!force && (null == waitingDrawingElement.getOccupiedArea() || waitingDrawingElement.getOccupiedArea().getPageNumber() >= this.currentArea.getPageNumber())) {
                if (null == waitingDrawingElement.getOccupiedArea()) {
                    flushedElements.add(waitingDrawingElement);
                }
            } else {
                this.flushSingleRenderer(waitingDrawingElement);
                flushedElements.add(waitingDrawingElement);
            }
        }

        this.waitingDrawingElements.removeAll(flushedElements);
    }
}
