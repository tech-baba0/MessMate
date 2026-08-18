package com.messmate.backend.scheduler;

import com.messmate.backend.entity.Mess;
import com.messmate.backend.entity.MessMember;
import com.messmate.backend.repository.MessMemberRepository;
import com.messmate.backend.repository.MessRepository;
import com.messmate.backend.repository.UserRepository;
import com.messmate.backend.service.FcmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VotingReminderTask {

    @Autowired
    private MessRepository messRepository;

    @Autowired
    private MessMemberRepository messMemberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FcmService fcmService;

    // Run every day at 8:30 AM in Asia/Kolkata timezone
    @Scheduled(cron = "0 30 8 * * ?", zone = "Asia/Kolkata")
    public void sendMorningVotingReminder() {
        System.out.println("Executing Morning Voting Reminder Task...");
        List<Mess> allMesses = messRepository.findAll();

        for (Mess mess : allMesses) {
            List<MessMember> members = messMemberRepository.findByMessId(mess.getId());
            for (MessMember member : members) {
                if ("ACTIVE".equals(member.getStatus()) || "APPROVED".equals(member.getStatus())) {
                    userRepository.findById(member.getUserId()).ifPresent(user -> {
                        if (user.getFcmToken() != null && !user.getFcmToken().isEmpty()) {
                            fcmService.sendPushNotification(
                                    user.getFcmToken(),
                                    "Morning Meal Reminder 🥗",
                                    "Don't forget to submit your Lunch and Dinner preferences before the deadline!");
                        }
                    });
                }
            }
        }
    }
}
