package persistence.entity;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Document {
    private Long id;
    private Project project;
    private DocumentType documentType;
    private String name;
    private String code;
    private User designer;
    private User supervisor;
    private byte[] contentInPdf;
    private boolean reassemblyRequired;
    private byte[] documentInPdf;

}
