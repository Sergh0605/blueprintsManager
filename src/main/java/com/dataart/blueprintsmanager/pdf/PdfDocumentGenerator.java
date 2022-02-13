package com.dataart.blueprintsmanager.pdf;

import com.dataart.blueprintsmanager.exceptions.PdfCustomApplicationException;
import com.itextpdf.io.exceptions.IOException;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

}
