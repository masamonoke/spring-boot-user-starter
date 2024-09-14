package com.masamonoke.userstarter.model;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AuthToken {
    @Id
    @GeneratedValue
    private Long id;
    @Column(unique = true)
    private String token;
    private boolean isRevoked;
    private boolean isExpired;
    @ManyToOne(fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id")
    private User user;
}
