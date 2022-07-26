package npc.martin.impalabackend.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author bikathi_martin
 */

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class SystemUsersListEntity {
    private String staffId;
    private String firstName;
    private String lastName;
    private String password;
    private Boolean accountStatus;
    private List<String> staffRoles;
}
