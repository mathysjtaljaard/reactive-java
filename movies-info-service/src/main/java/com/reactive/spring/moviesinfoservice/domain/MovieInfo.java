package com.reactive.spring.moviesinfoservice.domain;

import java.time.LocalDate;
import java.util.List;

import javax.validation.constraints.*;

import com.fasterxml.jackson.annotation.*;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
@Builder
public class MovieInfo {

    @Id
    private String movieInfoId;

    @NotBlank(message = "Movie Info Name cannot be blank")
    private String name;

    @NotNull(message = "Year cannot be empty")
    @Positive(message = "Year must be positive")
    private Integer year;

    @NotNull(message = "Cast cannot be null")
    @NotEmpty(message = "Cast cannot be empty")
    private List<String> cast;

    @NotNull
    @JsonAlias({ "release_date" })
    @JsonProperty("release_date")
    private LocalDate releaseDate;
}
