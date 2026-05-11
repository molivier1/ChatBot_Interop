package fr.ensim.interop.introrest.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private String chatId;
    private Integer messageId;
    private String text;
    private String status;
    private String timestamp;
}
