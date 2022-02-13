package com.dataart.blueprintsmanager.pdf;

import com.itextpdf.layout.properties.TextAlignment;

public enum DocumentRelativeFieldAreas {
    MainCodeInFirstPage(-125, 50, 120, 10, TextAlignment.CENTER, 7f, true, false),
    MainCodeInSecondPage(-125, 5, 120, 15, TextAlignment.CENTER, 7f, false, true),
    AdditionalCode(90, -5, 70, 7, TextAlignment.CENTER, 4f, true, true),
    PageNumberInFirstPage(-40, 20, 15, 10, TextAlignment.CENTER, 4f, true, false),
    PageNumberInSecondPage(-15, 5, 10, 8, TextAlignment.CENTER, 4f, false, true),
    TotalPagesCountInFirstPage(-25, 20, 20, 10, TextAlignment.CENTER, 4f, true, false),
    Stage(-55, 20, 15, 10, TextAlignment.CENTER, 5f, true, false),
    ProjectName(-125, 35, 120, 15, TextAlignment.CENTER, 4f, true, false),
    Address(-125, 20, 70, 15, TextAlignment.CENTER, 3.5f, true, false),
    DocumentName(-125, 5, 70, 15, TextAlignment.CENTER, 5, true, false),
    CompanyName(-55, 5, 50, 15, TextAlignment.CENTER, 4f, true, false),
    Designer(-170, 30, 20, 5, TextAlignment.LEFT, 4.0f, true, false),
    Supervisor(-170, 25, 20, 5, TextAlignment.LEFT, 4f, true, false),
    ChiefEngineer(-170, 15, 20, 5, TextAlignment.LEFT, 4f, true, false),
    Controller(-170, 10, 20, 5, TextAlignment.LEFT, 4f, true, false),
    SmallMainCodeInFirstPage(-125, 30, 120, 15, TextAlignment.CENTER, 7f, true, false),
    SmallPageNumberInFirstPage(-40, 20, 15, 5, TextAlignment.CENTER, 4f, true, false),
    SmallTotalPagesCountInFirstPage(-25, 20, 20, 5, TextAlignment.CENTER, 4f, true, false),
    SmallStage(-55, 20, 15, 5, TextAlignment.CENTER, 4f, true, false),
    SmallDocumentName(-125, 5, 70, 25, TextAlignment.CENTER, 4f, true, false),
    SmallDesigner(-170, 25, 20, 5, TextAlignment.LEFT, 4f, true, false),
    SmallSupervisor(-170, 20, 20, 5, TextAlignment.LEFT, 4f, true, false),
    DesignerDate(-135, 30, 10, 5, TextAlignment.CENTER, 4f, true, false),
    SupervisorDate(-135, 25, 10, 5, TextAlignment.CENTER, 4f, true, false),
    ChiefEngineerDate(-135, 15, 10, 5, TextAlignment.CENTER, 4f, true, false),
    ControllerDate(-135, 10, 10, 5, TextAlignment.CENTER, 4f, true, false),
    SmallDesignerDate(-135, 25, 10, 5, TextAlignment.CENTER, 4f, true, false),
    SmallSupervisorDate(-135, 20, 10, 5, TextAlignment.CENTER, 4f, true, false),
    DesignerSign(-150, 30, 15, 5, TextAlignment.CENTER, 4f, true, false),
    SupervisorSign(-150, 25, 15, 5, TextAlignment.CENTER, 4f, true, false),
    ChiefEngineerSign(-150, 15, 15, 5, TextAlignment.CENTER, 4f, true, false),
    ControllerSign(-150, 10, 15, 5, TextAlignment.CENTER, 4f, true, false),
    SmallDesignerSign(-150, 25, 15, 5, TextAlignment.CENTER, 4f, true, false),
    SmallSupervisorSign(-150, 20, 15, 5, TextAlignment.CENTER, 4f, true, false),
    CoverCompanyName(-190, 275, 180, 15, TextAlignment.CENTER, 8f, true,false),
    CoverProjectName(-190, 200, 180, 30, TextAlignment.CENTER, 8f, true,false),
    CoverVolumeName(-190, 170, 180, 30, TextAlignment.CENTER, 8f, true, false),
    CoverStage(-190,150, 180, 15, TextAlignment.CENTER, 10f, true,false),
    CoverVolumeNumber(-190,130,180,15, TextAlignment.CENTER, 10f, true,false),
    CoverVolumeCode(-190, 100, 180,20, TextAlignment.CENTER, 12f, true,false),
    CoverBottom(-190, 5, 180, 10, TextAlignment.CENTER, 5f, true,true),
    TitleListProjectName(-180, 185, 165, 30, TextAlignment.CENTER, 8f, true,false),
    TitleListStage(-180, 165, 165, 15, TextAlignment.CENTER, 8f, true,false),
    TitleListVolumeName(-180, 130, 165, 30, TextAlignment.CENTER, 8f, true,false),
    TitleListVolumeCode(-180, 110, 165, 15, TextAlignment.CENTER, 8f, true,false),
    TitleListSignerPosition(-96, 65, 90, 6, TextAlignment.CENTER, 5f, true,false),
    TitleListSignerCompany(-96, 60, 90, 6, TextAlignment.CENTER, 5f, true,false),
    TitleListSignerName(-85, 47, 70, 6, TextAlignment.RIGHT, 5f, true,false),
    TitleListExecutorSignDate(-85, 37, 70, 6, TextAlignment.RIGHT, 5f, true,false),
    TitleListApproveSignDate(-85, 256, 70, 6, TextAlignment.RIGHT, 5f, true,false),
    TitleListAgreeSignDate(-162, 37, 52, 6, TextAlignment.RIGHT, 5f, true,false);

    private final float x;
    private final float y;
    private final float width;
    private final float height;
    private final float fontSize;
    private final TextAlignment alignment;
    private final boolean visibleOnFirstPage;
    private final boolean visibleOnSecondPage;

    DocumentRelativeFieldAreas(float xInMm, float yInMm, float widthInMm, float heightInMm, TextAlignment alignment, float fontSizeInMm, boolean visibleOnFirstPage, boolean visibleOnSecondPage) {
        this.x = PdfUtils.millimetersToPoints(xInMm + PdfUtils.getFieldAreaCorrection());
        this.y = PdfUtils.millimetersToPoints(yInMm + PdfUtils.getFieldAreaCorrection());
        this.width = PdfUtils.millimetersToPoints(widthInMm - 2 * PdfUtils.getFieldAreaCorrection());
        this.height = PdfUtils.millimetersToPoints(heightInMm - 2 * PdfUtils.getFieldAreaCorrection());
        this.fontSize = PdfUtils.millimetersToPoints(fontSizeInMm);
        this.alignment = alignment;
        this.visibleOnFirstPage = visibleOnFirstPage;
        this.visibleOnSecondPage = visibleOnSecondPage;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getFontSize() {
        return fontSize;
    }

    public TextAlignment getAlignment() {
        return alignment;
    }

    public boolean isVisibleOnFirstPage() {
        return visibleOnFirstPage;
    }

    public boolean isVisibleOnSecondPage() {
        return visibleOnSecondPage;
    }
}
