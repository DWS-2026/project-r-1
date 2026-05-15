package es.codeurjc.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReviewDTO(
        Long id,
        
        Long authorId,
        String authorName,
        
        Long adviceId,
        String adviceTitle,
        
        @Min(value = 0, message = "The minimum score is 0")
        @Max(value = 10, message = "The maximum score is 10")
        int score,
        
        @NotBlank(message = "The review title is mandatory")
        @Size(max = 200, message = "The title cannot exceed 200 characters")
        String title,
        
        @NotBlank(message = "The comment is mandatory")
        @Size(max = 2000, message = "The comment cannot exceed 2000 characters")
        String comment
) {}