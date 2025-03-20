package com.communicator.entity.chat;

import com.communicator.dto.UserDto;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class ChatsDTO {

    private String chatId;
    private List<UserDto> users;
    private boolean active = true;
    private String photo = "https://img.freepik.com/premium-vector/illustration-persons-face-outline-icon-symbolizing"
            + "-anonymity_1171540-20820.jpg";
    private LastMessageDto message;
}
