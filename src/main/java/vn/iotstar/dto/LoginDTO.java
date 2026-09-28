package vn.iotstar.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginDTO {

    @NotBlank(message = "Username hoặc Email không được để trống")
    private String login;

    @NotBlank(message = "Password không được để trống")
    private String password;
}
