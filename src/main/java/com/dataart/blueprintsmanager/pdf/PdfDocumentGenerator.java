package com.dataart.blueprintsmanager.pdf;

import com.dataart.blueprintsmanager.exceptions.PdfCustomApplicationException;
import com.itextpdf.io.exceptions.IOException;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.*;

import static com.dataart.blueprintsmanager.pdf.PdfUtils.millimetersToPoints;

@Slf4j
@Builder
@AllArgsConstructor
@Data
public class PdfDocumentGenerator {
    private final byte[] pdfDocumentInBytes;
    private final String GostA = "./src/main/resources/font/GOST_type_A_1.ttf";


    public PdfDocumentGenerator getFilledA4CoverDocument(DocumentDataForPdf inputStamp) {
        return getFilledDocument(getRelativeFieldAreasWithDataForCoverDocument(inputStamp));
    }

    private Map<DocumentRelativeFieldAreas, Object> getRelativeFieldAreasWithDataForCoverDocument(DocumentDataForPdf stamp) {
        Map<DocumentRelativeFieldAreas, Object> relativeFieldAreasWithData = new HashMap<>();
        if (stamp.getCompany() != null) {
            if (stamp.getCompany().getCity() != null && stamp.getReleaseDate() != null) {
                relativeFieldAreasWithData.put(DocumentRelativeFieldAreas.CoverBottom, stamp.getCompany().getCity() + " " + stamp.getReleaseDateForCover());
            }
            relativeFieldAreasWithData.put(DocumentRelativeFieldAreas.CoverCompanyName, stamp.getCompany().getName().toUpperCase(Locale.ROOT));
        }
        relativeFieldAreasWithData.put(DocumentRelativeFieldAreas.CoverProjectName, stamp.getProjectName());
        relativeFieldAreasWithData.put(DocumentRelativeFieldAreas.CoverVolumeName, stamp.getVolumeName());
        relativeFieldAreasWithData.put(DocumentRelativeFieldAreas.CoverStage, stamp.getStageForCover().toUpperCase(Locale.ROOT));
        relativeFieldAreasWithData.put(DocumentRelativeFieldAreas.CoverVolumeNumber, "Том " + stamp.getVolumeNumber());
        relativeFieldAreasWithData.put(DocumentRelativeFieldAreas.CoverVolumeCode, stamp.getCodeForCover());
        return relativeFieldAreasWithData;
    }

    private Map<DocumentRelativeFieldAreas, Object> getRelativeFieldAreasWithDataForTitleListDocument(DocumentDataForPdf stamp) {
        Map<DocumentRelativeFieldAreas, Object> relativeFieldAreasWithData = new HashMap<>();
        if (stamp.getCompany() != null) {
            relativeFieldAreasWithData.put(DocumentRelativeFieldAreas.TitleListSignerPosition, stamp.getCompany().getSignerPosition());
            relativeFieldAreasWithData.put(DocumentRelativeFieldAreas.TitleListSignerName, stamp.getCompany().getSignerName());
            relativeFieldAreasWithData.put(DocumentRelativeFieldAreas.TitleListSignerCompany, stamp.getCompany().getName());
        }
        String releaseYear = stamp.getReleaseDateForCover() + "г.";
        relativeFieldAreasWithData.put(DocumentRelativeFieldAreas.TitleListAgreeSignDate, releaseYear);
        relativeFieldAreasWithData.put(DocumentRelativeFieldAreas.TitleListApproveSignDate, releaseYear);
        relativeFieldAreasWithData.put(DocumentRelativeFieldAreas.TitleListExecutorSignDate, releaseYear);
        relativeFieldAreasWithData.put(DocumentRelativeFieldAreas.TitleListProjectName, stamp.getProjectName());
        relativeFieldAreasWithData.put(DocumentRelativeFieldAreas.TitleListVolumeCode, stamp.getCodeForCover());
        relativeFieldAreasWithData.put(DocumentRelativeFieldAreas.TitleListStage, stamp.getStageForCover().toUpperCase(Locale.ROOT));
        relativeFieldAreasWithData.put(DocumentRelativeFieldAreas.TitleListVolumeName, String.format("Том %d. %s", stamp.getVolumeNumber(), stamp.getVolumeName()));
        return relativeFieldAreasWithData;
    }

    private Map<DocumentRelativeFieldAreas, Object> getRelativeFieldAreasWithDataForTextDocumentTitleBlock(DocumentDataForPdf stamp) {
        Map<DocumentRelativeFieldAreas, Object> relativeFieldAreasWithData = new HashMap<>();
        relativeFieldAreasWithData.put(DocumentRelativeFieldAreas.SmallMainCodeInFirstPage, stamp.getDocumentCode());
        relativeFieldAreasWithData.put(DocumentRelativeFieldAreas.MainCodeInSecondPage, stamp.getDocumentCode());
        relativeFieldAreasWithData.put(DocumentRelativeFieldAreas.SmallDocumentName, stamp.getDocumentName());
        relativeFieldAreasWithData.put(DocumentRelativeFieldAreas.SmallPageNumberInFirstPage, "");
        relativeFieldAreasWithData.put(DocumentRelativeFieldAreas.PageNumberInSecondPage, "");
        relativeFieldAreasWithData.put(DocumentRelativeFieldAreas.SmallTotalPagesCountInFirstPage, "");
        relativeFieldAreasWithData.put(DocumentRelativeFieldAreas.SmallDesigner, stamp.getDesignerName());
        relativeFieldAreasWithData.put(DocumentRelativeFieldAreas.Controller, stamp.getControllerName());
        relativeFieldAreasWithData.put(DocumentRelativeFieldAreas.ChiefEngineer, stamp.getChiefEngineerName());
        relativeFieldAreasWithData.put(DocumentRelativeFieldAreas.SmallSupervisor, stamp.getSupervisorName());
        relativeFieldAreasWithData.put(DocumentRelativeFieldAreas.SmallStage, stamp.getStage());
        relativeFieldAreasWithData.put(DocumentRelativeFieldAreas.CompanyName, stamp.getCompany());
        relativeFieldAreasWithData.put(DocumentRelativeFieldAreas.SmallDesignerDate, stamp.getReleaseDate());
        relativeFieldAreasWithData.put(DocumentRelativeFieldAreas.SmallSupervisorDate, stamp.getReleaseDate());
        relativeFieldAreasWithData.put(DocumentRelativeFieldAreas.ChiefEngineerDate, stamp.getReleaseDate());
        relativeFieldAreasWithData.put(DocumentRelativeFieldAreas.ControllerDate, stamp.getReleaseDate());
        relativeFieldAreasWithData.put(DocumentRelativeFieldAreas.SmallDesignerSign, stamp.getDesignerSign());
        relativeFieldAreasWithData.put(DocumentRelativeFieldAreas.SmallSupervisorSign, stamp.getSupervisorSign());
        relativeFieldAreasWithData.put(DocumentRelativeFieldAreas.ChiefEngineerSign, stamp.getChiefEngineerSign());
        relativeFieldAreasWithData.put(DocumentRelativeFieldAreas.ControllerSign, stamp.getControllerSign());
        return relativeFieldAreasWithData;
    }

    private Set<DocumentRelativeFieldAreas> getSignAreas() {
        Set<DocumentRelativeFieldAreas> fieldAreas = new HashSet<>();
        fieldAreas.add(DocumentRelativeFieldAreas.SmallSupervisorSign);
        fieldAreas.add(DocumentRelativeFieldAreas.SmallDesignerSign);
        fieldAreas.add(DocumentRelativeFieldAreas.SupervisorSign);
        fieldAreas.add(DocumentRelativeFieldAreas.DesignerSign);
        fieldAreas.add(DocumentRelativeFieldAreas.ChiefEngineerSign);
        fieldAreas.add(DocumentRelativeFieldAreas.ControllerSign);
        return fieldAreas;
    }

    private PdfDocumentGenerator getFilledDocument(Map<DocumentRelativeFieldAreas, Object> relativeFieldAreasWithData) {
        try (PdfReader reader = new PdfReader(new ByteArrayInputStream(pdfDocumentInBytes));
             ByteArrayOutputStream os = new ByteArrayOutputStream();
             PdfWriter writer = new PdfWriter(os);
             PdfDocument pdfDoc = new PdfDocument(reader, writer);
             Document document = new Document(pdfDoc)) {
            List<Paragraph> filledFieldsOfDocument = new ArrayList<>();
            relativeFieldAreasWithData.forEach((x, y) -> filledFieldsOfDocument.addAll(getTitleBlockFilledFields(y, x, pdfDoc)));
            filledFieldsOfDocument.forEach(document::add);
            document.close();
            return new PdfDocumentGenerator(os.toByteArray());
        } catch (IOException | java.io.IOException e) {
            log.debug(e.getMessage());
            throw new PdfCustomApplicationException("Can't read PDF template");
        }
    }

    private Rectangle getFieldAreaInDocument(Rectangle pageSize, DocumentRelativeFieldAreas relativeFieldArea) {
        Rectangle fieldArea;
        if (relativeFieldArea.equals(DocumentRelativeFieldAreas.AdditionalCode)) {
            fieldArea = new Rectangle(
                    relativeFieldArea.getX(),
                    pageSize.getTop() + relativeFieldArea.getY(),
                    relativeFieldArea.getWidth(),
                    relativeFieldArea.getHeight());
        } else {
            fieldArea = new Rectangle(
                    pageSize.getRight() + relativeFieldArea.getX(),
                    relativeFieldArea.getY(),
                    relativeFieldArea.getWidth(),
                    relativeFieldArea.getHeight());
        }
        return fieldArea;
    }

    private Map<Rectangle, Integer> getFieldAreasInTitleBlockOnPageOfDocument(PdfDocument pdfDocument, DocumentRelativeFieldAreas relativeFieldArea) {
        Map<Rectangle, Integer> fieldAreas = new HashMap<>();
        int pageCount = pdfDocument.getNumberOfPages();
        if (pageCount > 0) {
            for (int i = 1; i <= pageCount; i++) {
                Rectangle pageSize = pdfDocument.getPage(i).getPageSize();
                if (i == 1 && relativeFieldArea.isVisibleOnFirstPage()) {
                    fieldAreas.put(getFieldAreaInDocument(pageSize, relativeFieldArea), i);
                }
                if (i > 1 && relativeFieldArea.isVisibleOnSecondPage()) {
                    fieldAreas.put(getFieldAreaInDocument(pageSize, relativeFieldArea), i);
                }
            }
        }
        return fieldAreas;
    }

    private Paragraph getParagraph(Rectangle paragraphArea, Integer correctionInMM) {
        return new Paragraph()
                .setFixedPosition(paragraphArea.getLeft() - millimetersToPoints(correctionInMM)
                        , paragraphArea.getBottom() - 2 * millimetersToPoints(correctionInMM)
                        , paragraphArea.getWidth() + 2 * millimetersToPoints(correctionInMM))
                .setHeight(paragraphArea.getHeight() + 4 * millimetersToPoints(correctionInMM))
                .setMargin(0)
                .setPadding(0)
                .setMultipliedLeading(0.8f)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
    }

    private List<Paragraph> getTitleBlockFilledFields(
            Object content,
            DocumentRelativeFieldAreas fieldArea,
            PdfDocument pdfDocument) {
        List<Paragraph> filledFields = new ArrayList<>();
        if (content != null) {
            String textForParagraph = "";
            Image image;
            Map<Rectangle, Integer> fieldAreasOnPageInTitleBlockOfDocument = getFieldAreasInTitleBlockOnPageOfDocument(pdfDocument, fieldArea);
            for (Map.Entry<Rectangle, Integer> areaOnPage : fieldAreasOnPageInTitleBlockOfDocument.entrySet()) {
                if (getSignAreas().contains(fieldArea)) {
                    Paragraph paragraphForSign = getParagraph(areaOnPage.getKey(), 4);
                    paragraphForSign.setPageNumber(areaOnPage.getValue())
                            .setTextAlignment(TextAlignment.CENTER);
                    if (!(content instanceof byte[] signInBytes)) throw new AssertionError();
                    image = new Image(ImageDataFactory.create(signInBytes))
                            .setAutoScale(true);
                    paragraphForSign.add(image);
                    filledFields.add(paragraphForSign);
                    continue;
                }
                Paragraph paragraph = getParagraph(areaOnPage.getKey(), 0);
                paragraph.setTextAlignment(fieldArea.getAlignment())
                        .setPageNumber(areaOnPage.getValue());
                if (fieldArea.equals(DocumentRelativeFieldAreas.CompanyName)) {
                    if (!(content instanceof CompanyDataForPdf company)) throw new AssertionError();
                    if (company.getLogo() != null) {
                        image = new Image(ImageDataFactory.create(company.getLogo()))
                                .setAutoScale(true)
                                .setHorizontalAlignment(HorizontalAlignment.CENTER);
                        paragraph.add(image);
                        filledFields.add(paragraph);
                        continue;
                    } else if (company.getName() != null) textForParagraph = company.getName();
                }
                if (content instanceof String string) textForParagraph = string;

                if (fieldArea.equals(DocumentRelativeFieldAreas.AdditionalCode)) {
                    paragraph.setRotationAngle(180 * Math.PI / 180);
                }
                if (fieldArea.equals(DocumentRelativeFieldAreas.PageNumberInFirstPage)
                        || fieldArea.equals(DocumentRelativeFieldAreas.SmallPageNumberInFirstPage)
                        || fieldArea.equals(DocumentRelativeFieldAreas.PageNumberInSecondPage)) {
                    if (pdfDocument.getNumberOfPages() > 1) {
                        textForParagraph = String.valueOf(areaOnPage.getValue());
                    }
                }
                if (fieldArea.equals(DocumentRelativeFieldAreas.TotalPagesCountInFirstPage)
                        || fieldArea.equals(DocumentRelativeFieldAreas.SmallTotalPagesCountInFirstPage)) {
                    textForParagraph = String.valueOf(pdfDocument.getNumberOfPages());
                }
                PdfFont mainFont;
                try {
                    mainFont = PdfFontFactory.createFont(GostA, "Identity-H", PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                } catch (java.io.IOException e) {
                    log.debug(e.getMessage());
                    throw new PdfCustomApplicationException("Can't find Font file");
                }
                Text text = new Text(textForParagraph)
                        .setFont(mainFont)
                        .setFontSize(fieldArea.getFontSize())
                        .setHorizontalScaling(1f)
                        .setItalic()
                        .setTextRise(millimetersToPoints(-0.3f));
                paragraph.add(text);
                filledFields.add(paragraph);
            }
        }
        return filledFields;
    }

    public PdfDocumentGenerator getFilledA4TitleListDocument(DocumentDataForPdf inputStamp) {
        return getFilledDocument(getRelativeFieldAreasWithDataForTitleListDocument(inputStamp));
    }

    public PdfDocumentGenerator getFilledContentsDocument(List<RowOfContentsDocument> rowsOfDocument, byte[] secondPageTemplate) {
        int countOfNewPages = getCountOfNewPages(getFilledTableForContentsDocument(rowsOfDocument));
        byte[] documentWithAddedPages = getDocumentWithAddedPages(countOfNewPages, secondPageTemplate);
        try (PdfReader reader = new PdfReader(new ByteArrayInputStream(documentWithAddedPages));
             ByteArrayOutputStream os = new ByteArrayOutputStream();
             PdfWriter writer = new PdfWriter(os);
             PdfDocument pdfDoc = new PdfDocument(reader, writer);
             Document document = new Document(pdfDoc)) {
            document.setRenderer(new A4TextDocumentRenderer(document));
            document.add(getFilledTableForContentsDocument(rowsOfDocument));
            document.close();
            return new PdfDocumentGenerator(os.toByteArray());
        } catch (java.io.IOException e) {
            log.debug(e.getMessage());
            throw new PdfCustomApplicationException("Can't read PDF Template");
        }
    }

    private int getCountOfNewPages(IBlockElement element) {
        try (PdfReader reader = new PdfReader(new ByteArrayInputStream(pdfDocumentInBytes));
             ByteArrayOutputStream os = new ByteArrayOutputStream();
             PdfWriter writer = new PdfWriter(os);
             PdfDocument pdfDoc = new PdfDocument(reader, writer);
             Document document = new Document(pdfDoc)) {
            if (pdfDoc.getNumberOfPages() > 0) {
                CustomDocumentHandler handler = new CustomDocumentHandler();
                pdfDoc.addEventHandler(PdfDocumentEvent.START_PAGE, handler);
                document.setRenderer(new A4TextDocumentRenderer(document));
                document.add(element);
                document.close();
                return handler.getCountOfNewPages();
            } else throw new PdfCustomApplicationException("Wrong page count");
        } catch (java.io.IOException e) {
            log.debug(e.getMessage());
            throw new PdfCustomApplicationException("Can't read PDF Template");
        }
    }

    private Table getFilledTableForContentsDocument(List<RowOfContentsDocument> rowsOfDocument) {
        float[] columnWidth = {millimetersToPoints(60), millimetersToPoints(95), millimetersToPoints(30)};
        Table table = new Table(UnitValue.createPointArray(columnWidth));
        Cell[] header = new Cell[]{
                getContentsCell("Обозначение").setMinHeight(millimetersToPoints(15)).setFontSize(millimetersToPoints(5)),
                getContentsCell("Наименование").setMinHeight(millimetersToPoints(15)).setFontSize(millimetersToPoints(5)),
                getContentsCell("Примечание").setMinHeight(millimetersToPoints(15)).setFontSize(millimetersToPoints(5))
        };
        for (Cell h : header) {
            table.addHeaderCell(h);
        }
        for (RowOfContentsDocument rowOfContentsDocument : rowsOfDocument) {
            table.addCell(getContentsCell(rowOfContentsDocument.getColumn1()));
            table.addCell(getContentsCell(rowOfContentsDocument.getColumn2()).setTextAlignment(TextAlignment.LEFT));
            table.addCell(getContentsCell(rowOfContentsDocument.getColumn3()));
        }
        return table;
    }

    private byte[] getDocumentWithAddedPages(int countOfAddedPages, byte[] pageTemplate) {
        try (PdfReader reader = new PdfReader(new ByteArrayInputStream(pdfDocumentInBytes));
             PdfReader templateReader = new PdfReader(new ByteArrayInputStream(pageTemplate));
             ByteArrayOutputStream os = new ByteArrayOutputStream();
             PdfWriter writer = new PdfWriter(os);
             PdfDocument pdfDoc = new PdfDocument(reader, writer.setSmartMode(true));
             PdfDocument templatePdfDoc = new PdfDocument(templateReader)) {
            if (pdfDoc.getNumberOfPages() == 1 && templatePdfDoc.getNumberOfPages() == 1) {
                int templatePageNumber = templatePdfDoc.getNumberOfPages();
                for (int i = 0; i < countOfAddedPages; i++) {
                    templatePdfDoc.copyPagesTo(templatePageNumber, templatePageNumber, pdfDoc);
                }
                pdfDoc.close();
                return os.toByteArray();
            } else {
                String message = "Too many pages in template PDF";
                log.debug(message);
                throw new PdfCustomApplicationException(message);
            }
        } catch (java.io.IOException e) {
            log.debug(e.getMessage());
            throw new PdfCustomApplicationException("Can't read template PDF");
        }
    }

    private Cell getContentsCell(String cellContent) {
        PdfFont localFont = null;
        try {
            localFont = PdfFontFactory.createFont(GostA, "Identity-H", PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
        } catch (java.io.IOException e) {
            log.debug(e.getMessage());
            throw new PdfCustomApplicationException("Can't find font file");
        }
        return new Cell()
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setTextAlignment(TextAlignment.CENTER)
                .setFont(localFont)
                .setFontSize(millimetersToPoints(3))
                .setMinHeight(millimetersToPoints(8))
                .setBorder(new SolidBorder(ColorConstants.BLACK, millimetersToPoints(0.25f)))
                .add(new Paragraph(cellContent).setItalic());
    }

    public PdfDocumentGenerator getFilledTextDocumentTitleBlock(DocumentDataForPdf inputStamp) {
        return getFilledDocument(getRelativeFieldAreasWithDataForTextDocumentTitleBlock(inputStamp));
    }

}
