package com.masamonoke.userstarter.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class ConfirmationToken {
	@Id
	@GeneratedValue
	private long id;
	private String token;
	@Column(nullable = false)
	private LocalDateTime createdAt;
	@Column(nullable = false)
	private LocalDateTime expiredAt;
	private LocalDateTime confirmedAt;
	@ManyToOne(fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(nullable = false, name = "user_id")
	private User user;

	public ConfirmationToken(String token, LocalDateTime createdAt, LocalDateTime expiredAt, User user) {
		this.token = token;
		this.createdAt = createdAt;
		this.expiredAt = expiredAt;
		this.user = user;
	}
}
