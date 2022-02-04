package persistence.entity;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class User {
    Long id;
    String lastName;
    String login;
    String password;
    Company company;
    byte[] signature;
}
