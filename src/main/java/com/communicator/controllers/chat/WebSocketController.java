//package com.communicator.controllers.chat;
//import com.communicator.repositories.UserRepository;
//import com.communicator.repositories.UndeliveredMessageRepository;

//
//@Controller
//public class WebSocketController {
//
//        private final UserRepository userRepository;
//        private final UndeliveredMessageRepository undeliveredMessageRepository;
//        private final SimpMessageSendingOperations messagingTemplate;
//
//        public WebSocketController(UserRepository userRepository,
//                                   UndeliveredMessageRepository undeliveredMessageRepository,
//                                   SimpMessageSendingOperations messagingTemplate) {
//            this.userRepository = userRepository;
//            this.undeliveredMessageRepository = undeliveredMessageRepository;
//            this.messagingTemplate = messagingTemplate;
//        }
//
//        @MessageMapping("/user-status")
//        @SendTo("/topic/user-status")
//        public void handleUserStatus(Authentication authentication) {
//            AppUser appUser = (AppUser) authentication.getPrincipal();
//            String phoneNumber = appUser.getPhoneNumber();
//
//            appUser.setActive(true);
//            userRepository.save(appUser);
//
//            // Wysyłanie zaległych wiadomości
//            List<UndeliveredMessage> undeliveredMessages = undeliveredMessageRepository.findByUserId(phoneNumber);
//            undeliveredMessages.forEach(message -> messagingTemplate.convertAndSendToUser(
//                    phoneNumber,
//                    "/" + message.getUserId(),
//                    message.getPayload()
//            ));
//            undeliveredMessageRepository.deleteByUserId(phoneNumber);
//        }
//
//}
