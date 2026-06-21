package com.NgonNguLapTrinhJava.MiniSearchEngine.domain.responseDTO;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ResLoginDTO {

    @JsonProperty("access_token")
    private String accessToken;

    private UserLogin user;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class UserLogin {
        private Long id;
        private String email;
        private String name;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class UserGetAccount {
        private UserLogin user;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class UserInsideToken {
        private Long id;
        private String email;
        private String name;
    }
}
