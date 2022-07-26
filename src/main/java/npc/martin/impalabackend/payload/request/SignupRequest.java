package npc.martin.impalabackend.payload.request;

import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author bikathi_martin
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SignupRequest {
    private String staffId;
    private String firstName;
    private String lastName;
    private String password;
    private Boolean accountActive;
    private Set<String> staffRoles = new HashSet<>();
}
