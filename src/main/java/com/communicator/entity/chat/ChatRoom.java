package com.communicator.entity.chat;

import com.communicator.dto.UserDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Getter
@Setter
@Document("chats")
@NoArgsConstructor
public class ChatRoom {

    @Id
    private String chatId;

    private List<UserDto> users;

    private String photo = "https://img.freepik.com/premium-vector/illustration-persons-face-outline-icon-symbolizing"
            + "-anonymity_1171540-20820.jpg";

    private boolean active;

    private ChatMessage lastMessage;
}
